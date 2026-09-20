package com.sjydvlp.elefx.component.affix;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Translate;

/**
 * Element Plus inspired viewport affix.
 *
 * <p>
 * The affix preserves its ordinary layout space and translates its content only while it must
 * remain at the configured edge of a {@link ScrollPane} viewport. The nearest ancestor
 * {@code ScrollPane} supplies scroll events. {@link #setTarget(Node)} is an optional
 * non-scrolling boundary container that keeps an affix in range.
 * </p>
 */
public class EleFXAffix extends StackPane implements Themable {

    private static final String STYLE_CLASS = "ele-affix";

    private static final String FIXED_STYLE_CLASS = "ele-affix--fixed";

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 0);

    private final ObjectProperty<EleFXAffixPosition> position = new SimpleObjectProperty<>(this, "position",
            EleFXAffixPosition.TOP);

    private final IntegerProperty zIndex = new SimpleIntegerProperty(this, "zIndex", 100);

    private final ObjectProperty<Node> target = new SimpleObjectProperty<>(this, "target");

    private final ReadOnlyBooleanWrapper fixed = new ReadOnlyBooleanWrapper(this, "fixed", false);

    private final ObjectProperty<EventHandler<EleFXAffixEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    // Node already owns onScroll for JavaFX ScrollEvent, so use an unambiguous name for the
    // Element Plus-style scroll callback.
    private final ObjectProperty<EventHandler<EleFXAffixEvent>> onAffixScroll = new SimpleObjectProperty<>(this,
            "onAffixScroll");

    private final Translate affixTranslation = new Translate();

    private ScrollPane observedScrollPane;

    private Node observedTargetContainer;

    private final InvalidationListener targetGeometryListener = observable -> requestUpdate();

    private final InvalidationListener targetScrollListener = observable -> targetScrolled();

    private boolean updateQueued;

    private boolean suppressScrollEvent;

    private double viewOrderBeforeAffix;

    public EleFXAffix() {
        initialize();
    }

    public EleFXAffix(Node content) {
        this();
        setContent(content);
    }

    public double getOffset() {
        return offset.get();
    }

    /** Distance in pixels from the selected top or bottom viewport edge. */
    public void setOffset(double value) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("offset must be non-negative");
        offset.set(value);
    }

    public DoubleProperty offsetProperty() {
        return offset;
    }

    public EleFXAffixPosition getPosition() {
        return position.get();
    }

    public void setPosition(EleFXAffixPosition value) {
        position.set(value == null ? EleFXAffixPosition.TOP : value);
    }

    /** Element Plus-compatible convenience setter accepting {@code "top"} or {@code "bottom"}. */
    public void setPosition(String value) {
        if (value == null || value.equalsIgnoreCase("top")) {
            setPosition(EleFXAffixPosition.TOP);
        } else if (value.equalsIgnoreCase("bottom")) {
            setPosition(EleFXAffixPosition.BOTTOM);
        } else {
            throw new IllegalArgumentException("position must be top or bottom");
        }
    }

    public ObjectProperty<EleFXAffixPosition> positionProperty() {
        return position;
    }

    /**
     * Stacking level used while fixed. JavaFX renders lower {@code viewOrder} values in front,
     * so this positive Element Plus-style value is internally applied as a negative view order.
     */
    public int getZIndex() {
        return zIndex.get();
    }

    public void setZIndex(int value) {
        if (value < 0) throw new IllegalArgumentException("zIndex must not be negative");
        zIndex.set(value);
    }

    public IntegerProperty zIndexProperty() {
        return zIndex;
    }

    /** Optional non-scrolling container that bounds this affix. */
    public Node getTarget() {
        return target.get();
    }

    public void setTarget(Node value) {
        target.set(value);
    }

    public ObjectProperty<Node> targetProperty() {
        return target;
    }

    public boolean isFixed() {
        return fixed.get();
    }

    public ReadOnlyBooleanProperty fixedProperty() {
        return fixed.getReadOnlyProperty();
    }

    public EventHandler<EleFXAffixEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXAffixEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXAffixEvent>> onChangeProperty() {
        return onChange;
    }

    public EventHandler<EleFXAffixEvent> getOnAffixScroll() {
        return onAffixScroll.get();
    }

    public void setOnAffixScroll(EventHandler<EleFXAffixEvent> value) {
        onAffixScroll.set(value);
    }

    /**
     * Callback for Element Plus's {@code scroll} event. It is named {@code onAffixScroll}
     * because {@link Node} reserves {@code onScroll} for JavaFX {@code ScrollEvent}s.
     */
    public ObjectProperty<EventHandler<EleFXAffixEvent>> onAffixScrollProperty() {
        return onAffixScroll;
    }

    /** Returns the sole content node, or {@code null} when the affix is empty. */
    public Node getContent() {
        return getChildren().isEmpty() ? null : getChildren().get(0);
    }

    /** Replaces the affix content. A node already owned by another parent is rejected by JavaFX. */
    public void setContent(Node value) {
        getChildren().setAll(value == null ? java.util.List.of() : java.util.List.of(value));
    }

    /** Re-evaluates placement after changes which JavaFX cannot observe automatically. */
    public void update() {
        requestUpdate();
    }

    /** Element Plus-compatible alias for {@link #update()}. */
    public void updateRoot() {
        update();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.AFFIX;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        getTransforms().add(affixTranslation);
        offset.addListener(observable -> requestUpdate());
        position.addListener(observable -> requestUpdate());
        zIndex.addListener((observable, oldValue, newValue) -> {
            if (isFixed()) setViewOrder(-newValue.doubleValue());
        });
        target.addListener((observable, oldTarget, newTarget) -> refreshTarget());
        parentProperty().addListener(observable -> refreshTarget());
        sceneProperty().addListener((observable, oldScene, newScene) -> refreshTarget());
        layoutBoundsProperty().addListener(observable -> requestUpdate());
        localToSceneTransformProperty().addListener(observable -> requestUpdate());
        sceneBuilderIntegration();
    }

    private void refreshTarget() {
        ScrollPane scrollPane = findAncestorScrollPane();
        if (observedScrollPane != scrollPane) {
            if (observedScrollPane != null) detachScrollPane(observedScrollPane);
            observedScrollPane = scrollPane;
            if (observedScrollPane != null) attachScrollPane(observedScrollPane);
        }
        if (observedTargetContainer != getTarget()) {
            if (observedTargetContainer != null) detachTargetContainer(observedTargetContainer);
            observedTargetContainer = getTarget();
            if (observedTargetContainer != null) attachTargetContainer(observedTargetContainer);
        }
        requestUpdate();
    }

    private ScrollPane findAncestorScrollPane() {
        for (Parent ancestor = getParent(); ancestor != null; ancestor = ancestor.getParent())
            if (ancestor instanceof ScrollPane scrollPane) return scrollPane;
        return null;
    }

    private void attachScrollPane(ScrollPane scrollPane) {
        scrollPane.vvalueProperty().addListener(targetScrollListener);
        scrollPane.viewportBoundsProperty().addListener(targetGeometryListener);
        scrollPane.localToSceneTransformProperty().addListener(targetGeometryListener);
        scrollPane.sceneProperty().addListener(targetGeometryListener);
    }

    private void detachScrollPane(ScrollPane scrollPane) {
        scrollPane.vvalueProperty().removeListener(targetScrollListener);
        scrollPane.viewportBoundsProperty().removeListener(targetGeometryListener);
        scrollPane.localToSceneTransformProperty().removeListener(targetGeometryListener);
        scrollPane.sceneProperty().removeListener(targetGeometryListener);
    }

    private void attachTargetContainer(Node container) {
        container.boundsInLocalProperty().addListener(targetGeometryListener);
        container.localToSceneTransformProperty().addListener(targetGeometryListener);
    }

    private void detachTargetContainer(Node container) {
        container.boundsInLocalProperty().removeListener(targetGeometryListener);
        container.localToSceneTransformProperty().removeListener(targetGeometryListener);
    }

    private void targetScrolled() {
        if (suppressScrollEvent) return;
        // ScrollPane updates its content transform before publishing vvalue, so calculate now:
        // consumers of the scroll event receive the same fixed state they can observe on the node.
        applyPlacement();
        emit(EleFXAffixEvent.SCROLL, isFixed(), scrollTop());
    }

    private void requestUpdate() {
        if (updateQueued) return;
        updateQueued = true;
        Platform.runLater(() -> {
            updateQueued = false;
            applyPlacement();
        });
    }

    private void applyPlacement() {
        ScrollPane scrollPane = observedScrollPane;
        if (scrollPane == null || getScene() == null || getWidth() <= 0 || getHeight() <= 0) {
            setPinned(false, 0);
            return;
        }
        Bounds viewport = viewportBoundsInScene(scrollPane);
        if (viewport == null || viewport.getHeight() <= 0) {
            setPinned(false, 0);
            return;
        }
        affixTranslation.setY(0);
        Bounds naturalBounds = localToScene(getLayoutBounds());
        if (naturalBounds == null) return;
        double translate;
        boolean shouldFix;
        if (getPosition() == EleFXAffixPosition.TOP) {
            double desiredTop = viewport.getMinY() + getOffset();
            shouldFix = naturalBounds.getMinY() < desiredTop;
            translate = shouldFix ? desiredTop - naturalBounds.getMinY() : 0;
            if (shouldFix && observedTargetContainer != null) {
                Bounds targetBounds = targetBoundsInScene(observedTargetContainer);
                shouldFix = targetBounds != null && targetBounds.getMaxY() > viewport.getMinY();
                translate = shouldFix
                        ? Math.min(desiredTop, targetBounds.getMaxY() - getHeight()) - naturalBounds.getMinY()
                        : 0;
            }
        } else {
            double desiredBottom = viewport.getMaxY() - getOffset();
            shouldFix = naturalBounds.getMaxY() > desiredBottom;
            translate = shouldFix ? desiredBottom - naturalBounds.getMaxY() : 0;
            if (shouldFix && observedTargetContainer != null) {
                Bounds targetBounds = targetBoundsInScene(observedTargetContainer);
                shouldFix = targetBounds != null && targetBounds.getMinY() < viewport.getMaxY();
                translate = shouldFix
                        ? Math.max(desiredBottom - getHeight(), targetBounds.getMinY()) - naturalBounds.getMinY()
                        : 0;
            }
        }
        setPinned(shouldFix, translate);
    }

    private void setPinned(boolean value, double translateY) {
        suppressScrollEvent = true;
        try {
            affixTranslation.setY(translateY);
        } finally {
            suppressScrollEvent = false;
        }
        boolean changed = fixed.get() != value;
        if (value) {
            if (changed) viewOrderBeforeAffix = getViewOrder();
            setViewOrder(-getZIndex());
        } else if (changed) {
            setViewOrder(viewOrderBeforeAffix);
        }
        getStyleClass().remove(FIXED_STYLE_CLASS);
        if (value) getStyleClass().add(FIXED_STYLE_CLASS);
        fixed.set(value);
        if (changed) emit(EleFXAffixEvent.CHANGE, value, scrollTop());
    }

    private double scrollTop() {
        if (observedScrollPane == null || observedScrollPane.getContent() == null) return 0;
        double range = Math.max(0, observedScrollPane.getContent().getLayoutBounds().getHeight()
                - observedScrollPane.getViewportBounds().getHeight());
        return observedScrollPane.getVvalue() * range;
    }

    private static Bounds viewportBoundsInScene(ScrollPane scrollPane) {
        // The public viewportBounds property has the viewport size but, depending on the skin,
        // its origin may not include ScrollPane's border and padding. Prefer the skin viewport
        // node once CSS has created it; the public value remains a safe pre-CSS fallback.
        Node viewport = scrollPane.lookup(".viewport");
        return viewport == null
                ? scrollPane.localToScene(scrollPane.getViewportBounds())
                : viewport.localToScene(viewport.getBoundsInLocal());
    }

    private static Bounds targetBoundsInScene(Node target) {
        // boundsInLocal for a Parent can include its children's transformed visual bounds. An
        // affixed child is translated while fixed, so that would incorrectly stretch its own
        // target range and make it reappear after the target has left the viewport. layoutBounds
        // describes the target's allocated layout rectangle and intentionally excludes transforms.
        return target.localToScene(target.getLayoutBounds());
    }

    private void emit(javafx.event.EventType<? extends EleFXAffixEvent> type, boolean fixed, double scrollTop) {
        EleFXAffixEvent event = new EleFXAffixEvent(this, this, type, fixed, scrollTop);
        EventHandler<EleFXAffixEvent> handler = type == EleFXAffixEvent.CHANGE ? getOnChange() : getOnAffixScroll();
        if (handler != null) handler.handle(event);
        fireEvent(event);
    }
}
