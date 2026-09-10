package com.sjydvlp.elefx.component.splitter;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Element Plus inspired resizable pane layout.
 *
 * <p>
 * Add {@link EleFXSplitterPanel} instances with {@link #getItems()}. The
 * splitter distributes unspecified panel sizes equally, and respects the panel
 * minimum/maximum sizes while a divider is dragged.
 * </p>
 */
public class EleFXSplitter extends SplitPane implements Themable {

    private static final String STYLE_CLASS = "ele-splitter";

    private final ObjectProperty<Orientation> layout = new SimpleObjectProperty<>(this, "layout",
            Orientation.HORIZONTAL);

    private final BooleanProperty lazy = new SimpleBooleanProperty(this, "lazy", false);

    private boolean applyingSizes;

    private boolean deferredApplyQueued;

    public EleFXSplitter() {
        initialize();
    }

    public EleFXSplitter(EleFXSplitterPanel... panels) {
        this();
        getItems().addAll(panels);
    }

    public Orientation getLayout() {
        return layout.get();
    }

    public ObjectProperty<Orientation> layoutProperty() {
        return layout;
    }

    public void setLayout(Orientation layout) {
        this.layout.set(layout == null ? Orientation.HORIZONTAL : layout);
    }

    /**
     * Defers programmatic panel-size reconciliation until the next JavaFX pulse.
     * Native divider dragging remains responsive in both modes.
     */
    public boolean isLazy() {
        return lazy.get();
    }

    public BooleanProperty lazyProperty() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy.set(lazy);
    }

    /** Collapses or restores a collapsible panel by zero-based index. */
    public void togglePanel(int index) {
        EleFXSplitterPanel panel = panels().get(index);
        panel.toggleCollapsed();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SPLITTER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setOrientation(getLayout());
        layout.addListener((observable, oldValue, value) -> {
            setOrientation(value == null ? Orientation.HORIZONTAL : value);
            queueApplyPanelSizes();
        });
        getItems().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                for (Node node : change.getAddedSubList()) {
                    if (!(node instanceof EleFXSplitterPanel)) {
                        throw new IllegalArgumentException("EleFXSplitter items must be EleFXSplitterPanel instances");
                    }
                    bindPanel((EleFXSplitterPanel) node);
                }
            }
            queueApplyPanelSizes();
        });
        widthProperty().addListener(observable -> queueApplyPanelSizes());
        heightProperty().addListener(observable -> queueApplyPanelSizes());
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) queueApplyPanelSizes();
        });
        getDividers().addListener((ListChangeListener<Divider>) change -> bindDividers());
        sceneBuilderIntegration();
    }

    private void bindPanel(EleFXSplitterPanel panel) {
        panel.sizeProperty().addListener(observable -> queueApplyPanelSizes());
        panel.minProperty().addListener(observable -> queueApplyPanelSizes());
        panel.maxProperty().addListener(observable -> queueApplyPanelSizes());
        panel.resizableProperty().addListener(observable -> queueApplyPanelSizes());
        panel.collapsedProperty().addListener((observable, oldValue, collapsed) -> {
            if (collapsed)
                panel.setExpandedSize(panel.getSize());
            else if (Double.isFinite(panel.expandedSize())) panel.setSize(panel.expandedSize());
            queueApplyPanelSizes();
        });
    }

    private void bindDividers() {
        for (Divider divider : getDividers()) {
            divider.positionProperty().addListener((observable, oldValue, value) -> {
                if (!applyingSizes) updatePanelSizesFromDividers();
            });
        }
    }

    private void updatePanelSizesFromDividers() {
        List<EleFXSplitterPanel> panels = panels();
        if (panels.size() < 2) return;
        double length = primaryLength();
        if (length <= 0) return;
        double previous = 0;
        for (int index = 0; index < panels.size(); index++) {
            double position = index == panels.size() - 1 ? 1 : getDividers().get(index).getPosition();
            double pixels = Math.max(0, (position - previous) * length);
            panels.get(index).sizeProperty().set(pixels);
            previous = position;
        }
    }

    private void queueApplyPanelSizes() {
        if (applyingSizes || deferredApplyQueued) return;
        if (getScene() == null) {
            requestLayout();
            return;
        }
        deferredApplyQueued = true;
        Platform.runLater(() -> {
            deferredApplyQueued = false;
            applyPanelSizes();
        });
    }

    private void applyPanelSizes() {
        List<EleFXSplitterPanel> panels = panels();
        if (panels.size() < 2 || primaryLength() <= 0) return;
        applyingSizes = true;
        try {
            double length = primaryLength();
            double dividerSpace = Math.max(0, getDividers().size() * 6);
            double available = Math.max(0, length - dividerSpace);
            double specified = panels.stream().filter(panel -> Double.isFinite(panel.getSize()) || panel.isCollapsed())
                    .mapToDouble(panel -> constrainedSize(panel, available)).sum();
            long flexible = panels.stream().filter(panel -> !Double.isFinite(panel.getSize()) && !panel.isCollapsed())
                    .count();
            double automatic = flexible == 0 ? 0 : Math.max(0, available - specified) / flexible;
            double cumulative = 0;
            double[] positions = new double[panels.size() - 1];
            for (int index = 0; index < panels.size(); index++) {
                EleFXSplitterPanel panel = panels.get(index);
                double size = panel.isCollapsed()
                        ? 0
                        : Double.isFinite(panel.getSize())
                                ? constrainedSize(panel, available)
                                : constrained(automatic, panel);
                SplitPane.setResizableWithParent(panel, panel.isResizable() && !panel.isCollapsed());
                cumulative += size;
                if (index < positions.length) positions[index] = Math.min(1, cumulative / length);
            }
            setDividerPositions(positions);
        } finally {
            applyingSizes = false;
        }
    }

    private double constrainedSize(EleFXSplitterPanel panel, double available) {
        return constrained(Double.isFinite(panel.getSize()) ? panel.getSize() : available, panel);
    }

    private static double constrained(double size, EleFXSplitterPanel panel) {
        return Math.max(panel.getMin(), Math.min(panel.getMax(), size));
    }

    private double primaryLength() {
        return getOrientation() == Orientation.HORIZONTAL ? getWidth() : getHeight();
    }

    private List<EleFXSplitterPanel> panels() {
        List<EleFXSplitterPanel> panels = new ArrayList<>();
        for (Node item : getItems())
            panels.add((EleFXSplitterPanel) item);
        return panels;
    }
}
