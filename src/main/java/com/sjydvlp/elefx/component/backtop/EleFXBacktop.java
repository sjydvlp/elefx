package com.sjydvlp.elefx.component.backtop;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A floating control that becomes available after a {@link ScrollPane} has been scrolled and
 * smoothly returns it to its top. Add this node to the same overlay parent as its target, for
 * example {@code new StackPane(scrollPane, new EleFXBacktop(scrollPane))}.
 */
public class EleFXBacktop extends StackPane implements Themable {

    private static final String STYLE_CLASS = "ele-backtop";

    private static final String SHOWING_STYLE_CLASS = "ele-backtop--showing";

    private static final double SIZE = 40;

    private final ObjectProperty<ScrollPane> target = new SimpleObjectProperty<>(this, "target");

    private final DoubleProperty visibilityHeight = new SimpleDoubleProperty(this, "visibilityHeight", 200);

    private final DoubleProperty right = new SimpleDoubleProperty(this, "right", 40);

    private final DoubleProperty bottom = new SimpleDoubleProperty(this, "bottom", 40);

    private final ObjectProperty<Duration> scrollDuration = new SimpleObjectProperty<>(this, "scrollDuration",
            Duration.millis(500));

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    private final InvalidationListener updateListener = observable -> requestUpdate();

    private ScrollPane observedTarget;

    private Timeline scrollAnimation;

    private boolean updateQueued;

    public EleFXBacktop() {
        initialize();
    }

    public EleFXBacktop(ScrollPane target) {
        setTarget(target);
        initialize();
    }

    public EleFXBacktop(ScrollPane target, Node content) {
        this(target);
        setContent(content);
    }

    /** The scroll pane that is returned to its top. */
    public ScrollPane getTarget() {
        return target.get();
    }

    public void setTarget(ScrollPane value) {
        target.set(value);
    }

    public ObjectProperty<ScrollPane> targetProperty() {
        return target;
    }

    /** The vertical pixel offset after which this control is shown. */
    public double getVisibilityHeight() {
        return visibilityHeight.get();
    }

    public void setVisibilityHeight(double value) {
        setNonNegative(visibilityHeight, value, "visibilityHeight");
    }

    public DoubleProperty visibilityHeightProperty() {
        return visibilityHeight;
    }

    /** Distance in pixels from the target viewport's right edge. */
    public double getRight() {
        return right.get();
    }

    public void setRight(double value) {
        setNonNegative(right, value, "right");
    }

    public DoubleProperty rightProperty() {
        return right;
    }

    /** Distance in pixels from the target viewport's bottom edge. */
    public double getBottom() {
        return bottom.get();
    }

    public void setBottom(double value) {
        setNonNegative(bottom, value, "bottom");
    }

    public DoubleProperty bottomProperty() {
        return bottom;
    }

    /** Duration of the smooth scroll animation. */
    public Duration getScrollDuration() {
        return scrollDuration.get();
    }

    public void setScrollDuration(Duration value) {
        if (value == null || value.lessThan(Duration.ZERO) || value.isUnknown() || value.isIndefinite()) {
            throw new IllegalArgumentException("scrollDuration must be a finite non-negative duration");
        }
        scrollDuration.set(value);
    }

    public ObjectProperty<Duration> scrollDurationProperty() {
        return scrollDuration;
    }

    public boolean isShowing() {
        return showing.get();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    public EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /** Returns the sole content node. Set it to replace the default upward caret. */
    public Node getContent() {
        return getChildren().isEmpty() ? null : getChildren().get(0);
    }

    public void setContent(Node value) {
        getChildren().setAll(value == null ? java.util.List.of() : java.util.List.of(value));
    }

    /** Recalculates visibility and floating position after external layout changes. */
    public void update() {
        requestUpdate();
    }

    /** Animates the configured target back to its top and emits an action event. */
    public void scrollToTop() {
        ScrollPane pane = activeTarget();
        if (pane == null || pane.isDisabled()) return;
        if (scrollAnimation != null) scrollAnimation.stop();
        Duration duration = getScrollDuration();
        if (duration.lessThanOrEqualTo(Duration.ZERO)) {
            pane.setVvalue(0);
        } else {
            scrollAnimation = new Timeline(new KeyFrame(duration,
                    new KeyValue(pane.vvalueProperty(), 0, Interpolator.EASE_BOTH)));
            scrollAnimation.play();
        }
        ActionEvent event = new ActionEvent(this, this);
        EventHandler<ActionEvent> handler = getOnAction();
        if (handler != null) handler.handle(event);
        fireEvent(event);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.BACKTOP;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setManaged(false);
        setMinSize(SIZE, SIZE);
        setPrefSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);
        setContent(new EleFXIcon(EleFXIconType.CARET_TOP, 18));
        setOnMouseClicked(event -> {
            if (!isDisabled() && isShowing()) scrollToTop();
        });
        target.addListener((observable, oldTarget, newTarget) -> refreshTarget());
        parentProperty().addListener(observable -> refreshTarget());
        visibilityHeight.addListener(updateListener);
        right.addListener(updateListener);
        bottom.addListener(updateListener);
        sceneProperty().addListener(observable -> requestUpdate());
        updateShowing(false);
        refreshTarget();
        sceneBuilderIntegration();
    }

    private void refreshTarget() {
        ScrollPane resolved = activeTarget();
        if (observedTarget == resolved) {
            requestUpdate();
            return;
        }
        if (observedTarget != null) detach(observedTarget);
        observedTarget = resolved;
        if (observedTarget != null) attach(observedTarget);
        requestUpdate();
    }

    private ScrollPane activeTarget() {
        if (getTarget() != null) return getTarget();
        for (Parent parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrollPane pane) return pane;
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof ScrollPane pane && child != this) return pane;
            }
        }
        return null;
    }

    private void attach(ScrollPane pane) {
        pane.vvalueProperty().addListener(updateListener);
        pane.viewportBoundsProperty().addListener(updateListener);
        pane.localToSceneTransformProperty().addListener(updateListener);
        pane.sceneProperty().addListener(updateListener);
    }

    private void detach(ScrollPane pane) {
        pane.vvalueProperty().removeListener(updateListener);
        pane.viewportBoundsProperty().removeListener(updateListener);
        pane.localToSceneTransformProperty().removeListener(updateListener);
        pane.sceneProperty().removeListener(updateListener);
    }

    private void requestUpdate() {
        if (updateQueued) return;
        updateQueued = true;
        javafx.application.Platform.runLater(() -> {
            updateQueued = false;
            updateState();
        });
    }

    private void updateState() {
        ScrollPane pane = activeTarget();
        if (pane == null || getParent() == null || getScene() == null) {
            updateShowing(false);
            return;
        }
        double range = pane.getContent() == null
                ? 0
                : Math.max(0,
                        pane.getContent().getLayoutBounds().getHeight() - pane.getViewportBounds().getHeight());
        updateShowing(pane.getVvalue() * range >= getVisibilityHeight() && range > 0 && !isDisabled());
        Bounds viewport = viewportBoundsInScene(pane);
        Bounds parentBounds = getParent().sceneToLocal(viewport);
        if (parentBounds != null) resizeRelocate(parentBounds.getMaxX() - getRight() - SIZE,
                parentBounds.getMaxY() - getBottom() - SIZE, SIZE, SIZE);
    }

    private void updateShowing(boolean value) {
        showing.set(value);
        setVisible(value);
        setMouseTransparent(!value);
        getStyleClass().remove(SHOWING_STYLE_CLASS);
        if (value) getStyleClass().add(SHOWING_STYLE_CLASS);
    }

    private static Bounds viewportBoundsInScene(ScrollPane pane) {
        Node viewport = pane.lookup(".viewport");
        return viewport == null
                ? pane.localToScene(pane.getViewportBounds())
                : viewport.localToScene(viewport.getBoundsInLocal());
    }

    private static void setNonNegative(DoubleProperty property, double value, String name) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be non-negative");
        property.set(value);
    }
}
