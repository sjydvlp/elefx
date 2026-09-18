package com.sjydvlp.elefx.component.skeleton;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Supplier;

/**
 * An Element Plus-inspired loading placeholder.
 *
 * <p>
 * Without a template, every rendered placeholder contains a 33%-wide title and the
 * requested number of text rows. Supply nodes through {@link #getTemplateChildren()} for
 * a custom template, or a {@link #setTemplateFactory(Supplier) factory} when a template
 * must be repeated by {@linkplain #getCount() count}. Add loaded UI through
 * {@link #getContentChildren()}.
 * </p>
 */
public class EleFXSkeleton extends VBox implements Themable {

    public static final int DEFAULT_ROWS = 3;

    public static final int DEFAULT_COUNT = 1;

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", true);

    private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", false);

    private final IntegerProperty rows = new SimpleIntegerProperty(this, "rows", DEFAULT_ROWS);

    private final IntegerProperty count = new SimpleIntegerProperty(this, "count", DEFAULT_COUNT);

    private final ObjectProperty<EleFXSkeletonThrottle> throttle = new SimpleObjectProperty<>(this, "throttle",
            EleFXSkeletonThrottle.NONE);

    private final VBox template = new VBox();

    private final VBox content = new VBox();

    private final VBox rendered = new VBox();

    private Supplier<Node> templateFactory;

    private Timeline animation;

    private PauseTransition throttleTransition;

    private boolean showingSkeleton;

    private boolean initializing;

    private boolean prefWidthConstraintManaged;

    public EleFXSkeleton() {
        initialize();
    }

    /** Creates a Skeleton with Element Plus-style visibility throttling. */
    public EleFXSkeleton(EleFXSkeletonThrottle throttle) {
        setThrottle(throttle);
        initialize();
    }

    public EleFXSkeleton(Node... content) {
        this();
        getContentChildren().addAll(content);
    }

    /** Whether the placeholder is visible instead of the loaded content. */
    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean value) {
        loading.set(value);
    }

    /** Whether the placeholder uses the Element Plus-style loading pulse. */
    public boolean isAnimated() {
        return animated.get();
    }

    public BooleanProperty animatedProperty() {
        return animated;
    }

    public void setAnimated(boolean value) {
        animated.set(value);
    }

    /** Number of body lines in each default template. A title is rendered in addition. */
    public int getRows() {
        return rows.get();
    }

    public IntegerProperty rowsProperty() {
        return rows;
    }

    public void setRows(int value) {
        if (value < 0) throw new IllegalArgumentException("rows must not be negative");
        rows.set(value);
    }

    /** Number of placeholder templates rendered while loading. */
    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public void setCount(int value) {
        if (value < 1) throw new IllegalArgumentException("count must be at least 1");
        count.set(value);
    }

    /** Controls delayed display (leading) and removal (trailing) of the placeholder. */
    public EleFXSkeletonThrottle getThrottle() {
        return throttle.get();
    }

    public ObjectProperty<EleFXSkeletonThrottle> throttleProperty() {
        return throttle;
    }

    public void setThrottle(EleFXSkeletonThrottle value) {
        throttle.set(value == null ? EleFXSkeletonThrottle.NONE : value);
    }

    /** Equivalent to Element Plus's numeric {@code throttle} attribute. */
    public void setThrottle(long milliseconds) {
        setThrottle(new EleFXSkeletonThrottle(milliseconds));
    }

    /** Custom skeleton nodes, equivalent to Element Plus's {@code template} slot. */
    public VBox getTemplate() {
        return template;
    }

    public ObservableList<Node> getTemplateChildren() {
        return template.getChildren();
    }

    /** Loaded nodes, equivalent to Element Plus's default slot. */
    public VBox getContent() {
        return content;
    }

    public ObservableList<Node> getContentChildren() {
        return content.getChildren();
    }

    /**
     * Creates one fresh custom template for every requested count. This takes precedence
     * over {@link #getTemplateChildren()} because JavaFX nodes can have only one parent.
     */
    public Supplier<Node> getTemplateFactory() {
        return templateFactory;
    }

    public void setTemplateFactory(Supplier<Node> value) {
        templateFactory = value;
        refresh();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SKELETON;
    }

    private void initialize() {
        getStyleClass().add("ele-skeleton");
        rendered.getStyleClass().add("ele-skeleton__rendered");
        template.getStyleClass().add("ele-skeleton__template");
        content.getStyleClass().add("ele-skeleton__content");
        getChildren().add(rendered);
        loading.addListener(o -> requestLoadingDisplay());
        throttle.addListener(o -> requestLoadingDisplay());
        rows.addListener(o -> refresh());
        count.addListener(o -> refresh());
        animated.addListener(o -> updateAnimation());
        prefWidthProperty()
                .addListener((o, oldWidth, newWidth) -> synchronizePreferredWidthConstraint(newWidth.doubleValue()));
        template.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> refresh());
        content.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> refresh());
        initializing = true;
        requestLoadingDisplay();
        initializing = false;
        sceneBuilderIntegration();
    }

    private void refresh() {
        stopAnimation();
        rendered.getChildren().clear();
        if (showingSkeleton) {
            for (int index = 0; index < getCount(); index++) {
                rendered.getChildren().add(createTemplate(index));
            }
        } else {
            rendered.getChildren().add(content);
        }
        updateAnimation();
    }

    private Node createTemplate(int index) {
        if (templateFactory != null) {
            Node node = templateFactory.get();
            if (node == null) throw new IllegalStateException("templateFactory must not return null");
            return node;
        }
        if (!template.getChildren().isEmpty() && index == 0) return template;
        if (!template.getChildren().isEmpty()) return createDefaultTemplate();
        return createDefaultTemplate();
    }

    private VBox createDefaultTemplate() {
        VBox box = new VBox();
        box.getStyleClass().add("ele-skeleton__default");
        box.setFillWidth(true);
        EleFXSkeletonItem title = new EleFXSkeletonItem(EleFXSkeletonItemVariant.P);
        title.getStyleClass().add("ele-skeleton__title");
        title.prefWidthProperty().bind(box.widthProperty().divide(3));
        title.maxWidthProperty().bind(box.widthProperty().divide(3));
        box.getChildren().add(title);
        for (int index = 0; index < getRows(); index++) {
            EleFXSkeletonItem row = new EleFXSkeletonItem(EleFXSkeletonItemVariant.TEXT);
            row.getStyleClass().add("ele-skeleton__row");
            if (index == getRows() - 1) {
                row.getStyleClass().add("ele-skeleton__row--last");
                row.prefWidthProperty().bind(box.widthProperty().multiply(0.61));
                row.maxWidthProperty().bind(box.widthProperty().multiply(0.61));
            }
            box.getChildren().add(row);
        }
        return box;
    }

    private void updateAnimation() {
        if (!showingSkeleton || !isAnimated()) {
            stopAnimation();
            rendered.setOpacity(1);
            return;
        }
        if (animation == null) {
            animation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(rendered.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(800), new KeyValue(rendered.opacityProperty(), 0.55)),
                    new KeyFrame(Duration.millis(1600), new KeyValue(rendered.opacityProperty(), 1.0)));
            animation.setCycleCount(Timeline.INDEFINITE);
        }
        animation.play();
    }

    private void stopAnimation() {
        if (animation != null) animation.stop();
    }

    private void requestLoadingDisplay() {
        boolean target = isLoading();
        long delay = target ? getThrottle().getLeading() : getThrottle().getTrailing();
        if (initializing && target && getThrottle().isInitialLoading()) delay = 0;
        if (delay == 0) {
            if (throttleTransition != null) throttleTransition.stop();
            applyLoadingDisplay(target);
            return;
        }
        if (throttleTransition == null) throttleTransition = new PauseTransition();
        throttleTransition.stop();
        throttleTransition.setDuration(Duration.millis(delay));
        throttleTransition.setOnFinished(event -> {
            if (isLoading() == target) applyLoadingDisplay(target);
        });
        throttleTransition.playFromStart();
    }

    private void applyLoadingDisplay(boolean value) {
        if (showingSkeleton == value && !rendered.getChildren().isEmpty()) return;
        showingSkeleton = value;
        refresh();
    }

    /**
     * JavaFX VBox layouts expand resizable children to the available width. Element Plus
     * treats an explicit width as a real width constraint, so mirror that behavior when
     * callers use {@link #setPrefWidth(double)}.
     */
    private void synchronizePreferredWidthConstraint(double width) {
        if (Double.isFinite(width) && width >= 0) {
            setMaxWidth(width);
            prefWidthConstraintManaged = true;
        } else if (prefWidthConstraintManaged) {
            setMaxWidth(Region.USE_COMPUTED_SIZE);
            prefWidthConstraintManaged = false;
        }
    }
}
