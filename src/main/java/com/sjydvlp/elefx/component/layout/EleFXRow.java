package com.sjydvlp.elefx.component.layout;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Element Plus 风格的 24 栏栅格行，与 {@link EleFXCol} 配合使用。
 *
 * <p>
 * 列的 span 与 offset 合计超过 24 时自动换行，push/pull 只改变显示位置。
 * gutter 在每列外侧左右各保留一半间距，列的实际宽度为栅格宽度减去 gutter。
 * 背景和边框可直接设置在列上；行的两端也各保留半个 gutter，不使用负外边距。
 * 响应式断点采用 Scene 宽度，未加入 Scene 时采用行宽。
 * </p>
 *
 * <p>
 * 可通过 children 添加列，支持 JavaFX 属性绑定和 FXML。
 * 普通 Node 子节点按占满 24 栏处理；managed=false 的节点不参与布局。
 * </p>
 */
public class EleFXRow extends Pane implements Themable {

    public static final int COLUMN_COUNT = 24;

    private static final String STYLE_CLASS = "ele-row";

    private final DoubleProperty gutter = new SimpleDoubleProperty(this, "gutter", 0);

    private final ObjectProperty<EleFXRowJustify> justify = new SimpleObjectProperty<>(this, "justify",
            EleFXRowJustify.START);

    private final ObjectProperty<EleFXRowAlign> align = new SimpleObjectProperty<>(this, "align",
            EleFXRowAlign.STRETCH);

    private final InvalidationListener viewportListener = observable -> requestLayout();

    public EleFXRow() {
        initialize();
    }

    public EleFXRow(Node... children) {
        this();
        getChildren().addAll(children);
    }

    public EleFXRow(double gutter, Node... children) {
        this(children);
        setGutter(gutter);
    }

    public double getGutter() {
        return gutter.get();
    }

    /** 绑定值不是非负有限数时，布局按 0 处理。 */
    public DoubleProperty gutterProperty() {
        return gutter;
    }

    public void setGutter(double gutter) {
        if (!Double.isFinite(gutter) || gutter < 0) {
            throw new IllegalArgumentException("Gutter must be a non-negative finite number");
        }
        this.gutter.set(gutter);
    }

    public EleFXRowJustify getJustify() {
        return justify.get();
    }

    public ObjectProperty<EleFXRowJustify> justifyProperty() {
        return justify;
    }

    public void setJustify(EleFXRowJustify justify) {
        this.justify.set(justify == null ? EleFXRowJustify.START : justify);
    }

    public EleFXRowAlign getAlign() {
        return align.get();
    }

    public ObjectProperty<EleFXRowAlign> alignProperty() {
        return align;
    }

    public void setAlign(EleFXRowAlign align) {
        this.align.set(align == null ? EleFXRowAlign.STRETCH : align);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.LAYOUT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        double width = 0;
        double viewport = viewportWidth(getWidth());
        for (Node child : getManagedChildren()) {
            int span = child instanceof EleFXCol col ? col.resolveSize(viewport).getSpan() : COLUMN_COUNT;
            if (span > 0) {
                double spacing = child instanceof EleFXCol ? resolvedGutter() : 0;
                width = Math.max(width, (child.prefWidth(-1) + spacing) * COLUMN_COUNT / span);
            }
        }
        return snappedLeftInset() + width + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeHeight(width, true);
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeHeight(width, false);
    }

    @Override
    protected void layoutChildren() {
        double width = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
        double height = Math.max(0, getHeight() - snappedTopInset() - snappedBottomInset());
        double viewport = viewportWidth(getWidth());
        for (Node child : getChildren()) {
            if (child instanceof EleFXCol col) {
                boolean hidden = col.isManaged() && col.resolveSize(viewport).getSpan() == 0;
                col.setGridHidden(hidden);
                if (hidden) {
                    col.resizeRelocate(snappedLeftInset(), snappedTopInset(), 0, 0);
                }
            }
        }

        List<Line> lines = createLines(width, viewport, false);
        double preferredHeight = lines.stream().mapToDouble(line -> line.height).sum();
        // 与 flex 的 align-content: stretch 一致，将多余高度平分给各行。
        double extra = lines.isEmpty() ? 0 : Math.max(0, height - preferredHeight) / lines.size();
        double y = snappedTopInset();
        EleFXRowAlign resolvedAlign = getAlign() == null ? EleFXRowAlign.STRETCH : getAlign();
        for (Line line : lines) {
            double lineHeight = line.height + extra;
            for (Item item : line.items) {
                double childHeight = resolvedAlign == EleFXRowAlign.STRETCH
                        ? Math.max(item.node.minHeight(item.width),
                                Math.min(lineHeight, item.node.maxHeight(item.width)))
                        : item.height;
                double childY = y + switch (resolvedAlign) {
                    case MIDDLE -> (lineHeight - childHeight) / 2;
                    case BOTTOM -> lineHeight - childHeight;
                    default -> 0;
                };
                double top = snapPositionY(childY);
                double bottom = snapPositionY(childY + childHeight);
                item.node.resizeRelocate(item.x, top, item.width, Math.max(0, bottom - top));
            }
            y += lineHeight;
        }
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateJustifyStyleClass(null, getJustify());
        updateAlignStyleClass(null, getAlign());
        gutter.addListener(observable -> requestLayout());
        justify.addListener((observable, oldValue, newValue) -> {
            updateJustifyStyleClass(oldValue, newValue);
            requestLayout();
        });
        align.addListener((observable, oldValue, newValue) -> {
            updateAlignStyleClass(oldValue, newValue);
            requestLayout();
        });
        getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                for (Node removed : change.getRemoved()) {
                    if (removed instanceof EleFXCol col) {
                        col.setGridHidden(false);
                    }
                }
            }
            requestLayout();
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.widthProperty().removeListener(viewportListener);
            }
            if (newScene != null) {
                newScene.widthProperty().addListener(viewportListener);
            }
            requestLayout();
        });
        sceneBuilderIntegration();
    }

    private double resolvedGutter() {
        double value = getGutter();
        return Double.isFinite(value) && value >= 0 ? value : 0;
    }

    private void updateJustifyStyleClass(EleFXRowJustify oldValue, EleFXRowJustify newValue) {
        getStyleClass().remove((oldValue == null ? EleFXRowJustify.START : oldValue).styleClass());
        getStyleClass().add((newValue == null ? EleFXRowJustify.START : newValue).styleClass());
    }

    private void updateAlignStyleClass(EleFXRowAlign oldValue, EleFXRowAlign newValue) {
        getStyleClass().remove((oldValue == null ? EleFXRowAlign.STRETCH : oldValue).styleClass());
        getStyleClass().add((newValue == null ? EleFXRowAlign.STRETCH : newValue).styleClass());
    }

    private double viewportWidth(double fallback) {
        return getScene() != null && getScene().getWidth() > 0 ? getScene().getWidth() : Math.max(0, fallback);
    }

    private double computeHeight(double width, boolean minimum) {
        double resolvedWidth = width < 0 ? computePrefWidth(-1) : width;
        double contentWidth = Math.max(0, resolvedWidth - snappedLeftInset() - snappedRightInset());
        return snappedTopInset() + createLines(contentWidth, viewportWidth(resolvedWidth), minimum).stream()
                .mapToDouble(line -> line.height).sum() + snappedBottomInset();
    }

    private List<Line> createLines(double width, double viewport, boolean minimum) {
        List<Line> lines = new ArrayList<>();
        Line line = new Line();
        for (Node child : getManagedChildren()) {
            EleFXColSize size = child instanceof EleFXCol col
                    ? col.resolveSize(viewport)
                    : new EleFXColSize(COLUMN_COUNT, 0, 0, 0);
            int span = size.getSpan();
            if (span == 0) {
                continue;
            }
            int occupied = span + size.getOffset();
            if (!line.items.isEmpty() && line.columns + occupied > COLUMN_COUNT) {
                lines.add(line);
                line = new Line();
            }
            double slotWidth = width * span / COLUMN_COUNT;
            line.items.add(new Item(child, slotWidth, size.getOffset(),
                    size.getPush() - size.getPull()));
            line.columns += occupied;
        }
        if (!line.items.isEmpty()) {
            lines.add(line);
        }
        for (Line measuredLine : lines) {
            measureLine(measuredLine, width, minimum);
        }
        return lines;
    }

    private void measureLine(Line line, double width, boolean minimum) {
        EleFXRowJustify resolvedJustify = getJustify() == null ? EleFXRowJustify.START : getJustify();
        double freeWidth = Math.max(0, width - width * line.columns / COLUMN_COUNT);
        int count = line.items.size();
        double gap = switch (resolvedJustify) {
            case SPACE_BETWEEN -> count > 1 ? freeWidth / (count - 1) : 0;
            case SPACE_AROUND -> freeWidth / count;
            case SPACE_EVENLY -> freeWidth / (count + 1);
            default -> 0;
        };
        double x = snappedLeftInset() + switch (resolvedJustify) {
            case CENTER -> freeWidth / 2;
            case END -> freeWidth;
            case SPACE_AROUND -> gap / 2;
            case SPACE_EVENLY -> gap;
            default -> 0;
        };
        for (Item item : line.items) {
            x += width * item.offset / COLUMN_COUNT;
            double spacing = item.node instanceof EleFXCol ? Math.min(resolvedGutter(), item.slotWidth) : 0;
            double childX = x + width * item.shift / COLUMN_COUNT + spacing / 2;
            item.x = snapPositionX(childX);
            item.width = Math.max(0, snapPositionX(childX + item.slotWidth - spacing) - item.x);
            // Measure with the exact width that will be assigned, including pixel snapping.
            item.height = Math.max(0, minimum
                    ? item.node.minHeight(item.width)
                    : Math.max(item.node.minHeight(item.width),
                            Math.min(item.node.prefHeight(item.width), item.node.maxHeight(item.width))));
            line.height = Math.max(line.height, item.height);
            x += item.slotWidth + gap;
        }
    }

    private static class Line {

        private final List<Item> items = new ArrayList<>();

        private int columns;

        private double height;
    }

    private static class Item {

        private final Node node;

        private final double slotWidth;

        private final int offset;

        private final int shift;

        private double x;

        private double width;

        private double height;

        private Item(Node node, double slotWidth, int offset, int shift) {
            this.node = node;
            this.slotWidth = slotWidth;
            this.offset = offset;
            this.shift = shift;
        }
    }
}
