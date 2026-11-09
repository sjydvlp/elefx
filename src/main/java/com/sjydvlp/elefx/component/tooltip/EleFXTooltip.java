package com.sjydvlp.elefx.component.tooltip;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.internal.ScenePopupOverlay;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.function.Function;

/**
 * Element Plus-inspired tooltip attached to a JavaFX node. It supports the
 * documented placement, trigger, delay, controlled visibility and virtual
 * triggering behaviour while keeping all content JavaFX-native.
 */
public final class EleFXTooltip {

    private static final double ARROW_SIZE = 10;

    private static final double POPUP_MARGIN = 12;

    private final ObjectProperty<Node> reference = new SimpleObjectProperty<>(this, "reference");

    private final ObjectProperty<Node> virtualRef = new SimpleObjectProperty<>(this, "virtualRef");

    private final BooleanProperty virtualTriggering = new SimpleBooleanProperty(this, "virtualTriggering", false);

    private final ObjectProperty<EleFXTooltipTrigger> trigger = new SimpleObjectProperty<>(this, "trigger",
            EleFXTooltipTrigger.HOVER);

    private final ObservableList<KeyCode> triggerKeys = FXCollections.observableArrayList(KeyCode.ENTER, KeyCode.SPACE);

    private final StringProperty content = new SimpleStringProperty(this, "content", "");

    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>(this, "contentNode");

    private final BooleanProperty rawContent = new SimpleBooleanProperty(this, "rawContent", false);

    private final ObjectProperty<EleFXTooltipPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXTooltipPlacement.BOTTOM);

    private final ObservableList<EleFXTooltipPlacement> fallbackPlacements = FXCollections.observableArrayList();

    private final ObjectProperty<EleFXTooltipEffect> effect = new SimpleObjectProperty<>(this, "effect",
            EleFXTooltipEffect.DARK);

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 12);

    private final DoubleProperty arrowOffset = new SimpleDoubleProperty(this, "arrowOffset", 5);

    private final BooleanProperty showArrow = new SimpleBooleanProperty(this, "showArrow", true);

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    /** A null value means uncontrolled; set true or false for controlled visibility. */
    private final ObjectProperty<Boolean> visible = new SimpleObjectProperty<>(this, "visible", null);

    private final IntegerProperty showAfter = new SimpleIntegerProperty(this, "showAfter", 0);

    private final IntegerProperty hideAfter = new SimpleIntegerProperty(this, "hideAfter", 200);

    private final IntegerProperty autoClose = new SimpleIntegerProperty(this, "autoClose", 0);

    private final BooleanProperty enterable = new SimpleBooleanProperty(this, "enterable", true);

    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent", true);

    private final BooleanProperty teleported = new SimpleBooleanProperty(this, "teleported", true);

    private final BooleanProperty focusOnTarget = new SimpleBooleanProperty(this, "focusOnTarget", false);

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "");

    private final StringProperty transition = new SimpleStringProperty(this, "transition", "el-fade-in-linear");

    private final StringProperty popperClass = new SimpleStringProperty(this, "popperClass", "");

    private final StringProperty popperStyle = new SimpleStringProperty(this, "popperStyle", "");

    private final StringProperty arrowStyle = new SimpleStringProperty(this, "arrowStyle", "");

    private final StringProperty contentStyle = new SimpleStringProperty(this, "contentStyle", "");

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeShow = new SimpleObjectProperty<>(this,
            "onBeforeShow");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onShow = new SimpleObjectProperty<>(this, "onShow");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeHide = new SimpleObjectProperty<>(this,
            "onBeforeHide");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onHide = new SimpleObjectProperty<>(this, "onHide");

    private Popup popup;

    private ScenePopupOverlay sceneOverlay;

    private StackPane root;

    private StackPane popupContainer;

    private Region arrow;

    private PauseTransition showTimer = new PauseTransition();

    private PauseTransition hideTimer = new PauseTransition();

    private PauseTransition autoCloseTimer = new PauseTransition();

    private Scene observedScene;

    private Window observedOwner;

    private boolean suspendedForOwner;

    private final ChangeListener<Boolean> ownerStateChanged = (o, oldValue, newValue) -> updateOwnerState();

    private boolean pointerInReference;

    private boolean pointerInTooltip;

    private boolean hoverHidePending;

    private Animation activeAnimation;

    private boolean hiding;

    private boolean targetVisible;

    private long animationGeneration;

    private double basePopupX;

    private double basePopupY;

    private final DoubleProperty transitionOffsetX = new SimpleDoubleProperty();

    private final DoubleProperty transitionOffsetY = new SimpleDoubleProperty();

    private final EventHandler<MouseEvent> mouseEntered = e -> {
        pointerInReference = true;
        if (getTrigger() == EleFXTooltipTrigger.HOVER) {
            if (isFocusOnTarget()) activeReference().requestFocus();
            scheduleShow();
        }
    };

    private final EventHandler<MouseEvent> mouseExited = e -> {
        pointerInReference = false;
        if (getTrigger() == EleFXTooltipTrigger.HOVER) scheduleHoverHide();
    };

    private final EventHandler<MouseEvent> mouseClicked = e -> {
        if (getVisible() != null) return;
        if (getTrigger() == EleFXTooltipTrigger.CLICK) toggle();
    };

    private final EventHandler<ContextMenuEvent> contextMenu = e -> {
        if (getVisible() == null && getTrigger() == EleFXTooltipTrigger.CONTEXT_MENU) {
            showAt(e.getScreenX(), e.getScreenY());
            e.consume();
        }
    };

    private final EventHandler<KeyEvent> keyPressed = e -> {
        if (getVisible() != null) return;
        if (e.getCode() == KeyCode.ESCAPE)
            hide();
        else if (getTrigger() != EleFXTooltipTrigger.MANUAL && triggerKeys.contains(e.getCode())) {
            toggle();
            e.consume();
        }
    };

    private final ChangeListener<Boolean> focusChanged = (o, oldValue, focused) -> {
        if (getTrigger() == EleFXTooltipTrigger.FOCUS) {
            if (focused)
                scheduleShow();
            else
                scheduleHide();
        }
    };

    private final EventHandler<MouseEvent> outsidePressed = e -> {
        if (isShowing() && getVisible() == null && !isVirtualTriggering() && !isInside(e.getTarget())) hide();
    };

    public EleFXTooltip() {
        initialize();
    }

    public EleFXTooltip(Node reference, String content) {
        this();
        setReference(reference);
        setContent(content);
    }

    public void show() {
        requestVisibility(true);
    }

    public void hide() {
        requestVisibility(false);
    }

    public void toggle() {
        requestVisibility(!targetVisible);
    }

    /** Alias for Element Plus's exposed onOpen API. */
    public void onOpen() {
        show();
    }

    /** Alias for Element Plus's exposed onClose API. */
    public void onClose() {
        hide();
    }

    /** Recalculates the popup position after its reference or content changes. */
    public void updatePopper() {
        reposition();
    }

    /** Native popup handle; background hover content is hosted in the owner's Scene instead. */
    public Popup getPopperRef() {
        return popup;
    }

    public Node getContentRef() {
        return root;
    }

    public boolean isFocusInsideContent() {
        if (root == null || root.getScene() == null) return false;
        Node focused = root.getScene().getFocusOwner();
        for (Node node = focused; node != null; node = node.getParent())
            if (node == root) return true;
        return false;
    }

    public boolean isShowing() {
        return showing.get();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    public Node getReference() {
        return reference.get();
    }

    public void setReference(Node value) {
        reference.set(value);
    }

    public ObjectProperty<Node> referenceProperty() {
        return reference;
    }

    public Node getVirtualRef() {
        return virtualRef.get();
    }

    public void setVirtualRef(Node value) {
        virtualRef.set(value);
    }

    public ObjectProperty<Node> virtualRefProperty() {
        return virtualRef;
    }

    public boolean isVirtualTriggering() {
        return virtualTriggering.get();
    }

    public void setVirtualTriggering(boolean value) {
        virtualTriggering.set(value);
    }

    public BooleanProperty virtualTriggeringProperty() {
        return virtualTriggering;
    }

    public EleFXTooltipTrigger getTrigger() {
        return trigger.get();
    }

    public void setTrigger(EleFXTooltipTrigger value) {
        trigger.set(value == null ? EleFXTooltipTrigger.HOVER : value);
    }

    public ObjectProperty<EleFXTooltipTrigger> triggerProperty() {
        return trigger;
    }

    public ObservableList<KeyCode> getTriggerKeys() {
        return triggerKeys;
    }

    public String getContent() {
        return content.get();
    }

    public void setContent(String value) {
        content.set(value == null ? "" : value);
    }

    public StringProperty contentProperty() {
        return content;
    }

    public Node getContentNode() {
        return contentNode.get();
    }

    public void setContentNode(Node value) {
        contentNode.set(value);
    }

    public ObjectProperty<Node> contentNodeProperty() {
        return contentNode;
    }

    /** JavaFX has no HTML renderer; use {@link #setContentNode(Node)} for rich, safe content. */
    public boolean isRawContent() {
        return rawContent.get();
    }

    public void setRawContent(boolean value) {
        rawContent.set(value);
    }

    public BooleanProperty rawContentProperty() {
        return rawContent;
    }

    public EleFXTooltipPlacement getPlacement() {
        return placement.get();
    }

    public void setPlacement(EleFXTooltipPlacement value) {
        placement.set(value == null ? EleFXTooltipPlacement.BOTTOM : value);
    }

    public ObjectProperty<EleFXTooltipPlacement> placementProperty() {
        return placement;
    }

    public ObservableList<EleFXTooltipPlacement> getFallbackPlacements() {
        return fallbackPlacements;
    }

    public EleFXTooltipEffect getEffect() {
        return effect.get();
    }

    public void setEffect(EleFXTooltipEffect value) {
        effect.set(value == null ? EleFXTooltipEffect.DARK : value);
    }

    public ObjectProperty<EleFXTooltipEffect> effectProperty() {
        return effect;
    }

    public double getOffset() {
        return offset.get();
    }

    public void setOffset(double value) {
        nonNegative(value, "offset");
        offset.set(value);
    }

    public DoubleProperty offsetProperty() {
        return offset;
    }

    public double getArrowOffset() {
        return arrowOffset.get();
    }

    public void setArrowOffset(double value) {
        nonNegative(value, "arrowOffset");
        arrowOffset.set(value);
    }

    public DoubleProperty arrowOffsetProperty() {
        return arrowOffset;
    }

    public boolean isShowArrow() {
        return showArrow.get();
    }

    public void setShowArrow(boolean value) {
        showArrow.set(value);
    }

    public BooleanProperty showArrowProperty() {
        return showArrow;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean value) {
        disabled.set(value);
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public Boolean getVisible() {
        return visible.get();
    }

    public void setVisible(Boolean value) {
        visible.set(value);
    }

    public ObjectProperty<Boolean> visibleProperty() {
        return visible;
    }

    public int getShowAfter() {
        return showAfter.get();
    }

    public void setShowAfter(int value) {
        nonNegative(value, "showAfter");
        showAfter.set(value);
    }

    public IntegerProperty showAfterProperty() {
        return showAfter;
    }

    public int getHideAfter() {
        return hideAfter.get();
    }

    public void setHideAfter(int value) {
        nonNegative(value, "hideAfter");
        hideAfter.set(value);
    }

    public IntegerProperty hideAfterProperty() {
        return hideAfter;
    }

    public int getAutoClose() {
        return autoClose.get();
    }

    public void setAutoClose(int value) {
        nonNegative(value, "autoClose");
        autoClose.set(value);
    }

    public IntegerProperty autoCloseProperty() {
        return autoClose;
    }

    public boolean isEnterable() {
        return enterable.get();
    }

    public void setEnterable(boolean value) {
        enterable.set(value);
    }

    public BooleanProperty enterableProperty() {
        return enterable;
    }

    public boolean isPersistent() {
        return persistent.get();
    }

    public void setPersistent(boolean value) {
        persistent.set(value);
    }

    public BooleanProperty persistentProperty() {
        return persistent;
    }

    /** JavaFX Popup is always an overlay; retained for Element Plus API parity. */
    public boolean isTeleported() {
        return teleported.get();
    }

    public void setTeleported(boolean value) {
        teleported.set(value);
    }

    public BooleanProperty teleportedProperty() {
        return teleported;
    }

    public boolean isFocusOnTarget() {
        return focusOnTarget.get();
    }

    public void setFocusOnTarget(boolean value) {
        focusOnTarget.set(value);
    }

    public BooleanProperty focusOnTargetProperty() {
        return focusOnTarget;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String value) {
        ariaLabel.set(value == null ? "" : value);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public String getTransition() {
        return transition.get();
    }

    /** Selects a registered animation name; null or blank disables enter/leave animations. */
    public void setTransition(String value) {
        EleFXTooltipTransitions.resolve(value);
        transition.set(value == null ? "" : value);
    }

    public StringProperty transitionProperty() {
        return transition;
    }

    public String getPopperClass() {
        return popperClass.get();
    }

    public void setPopperClass(String value) {
        popperClass.set(value == null ? "" : value.trim());
    }

    public StringProperty popperClassProperty() {
        return popperClass;
    }

    public String getPopperStyle() {
        return popperStyle.get();
    }

    public void setPopperStyle(String value) {
        popperStyle.set(value == null ? "" : value);
    }

    public StringProperty popperStyleProperty() {
        return popperStyle;
    }

    public String getArrowStyle() {
        return arrowStyle.get();
    }

    /** Inline JavaFX CSS for the tooltip arrow. */
    public void setArrowStyle(String value) {
        arrowStyle.set(value == null ? "" : value);
    }

    public StringProperty arrowStyleProperty() {
        return arrowStyle;
    }

    public String getContentStyle() {
        return contentStyle.get();
    }

    /** Inline CSS for the built-in text label; style custom content nodes directly. */
    public void setContentStyle(String value) {
        contentStyle.set(value == null ? "" : value);
    }

    public StringProperty contentStyleProperty() {
        return contentStyle;
    }

    public EventHandler<EleFXTooltipEvent> getOnBeforeShow() {
        return onBeforeShow.get();
    }

    public void setOnBeforeShow(EventHandler<EleFXTooltipEvent> value) {
        onBeforeShow.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeShowProperty() {
        return onBeforeShow;
    }

    public EventHandler<EleFXTooltipEvent> getOnShow() {
        return onShow.get();
    }

    public void setOnShow(EventHandler<EleFXTooltipEvent> value) {
        onShow.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTooltipEvent>> onShowProperty() {
        return onShow;
    }

    public EventHandler<EleFXTooltipEvent> getOnBeforeHide() {
        return onBeforeHide.get();
    }

    public void setOnBeforeHide(EventHandler<EleFXTooltipEvent> value) {
        onBeforeHide.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeHideProperty() {
        return onBeforeHide;
    }

    public EventHandler<EleFXTooltipEvent> getOnHide() {
        return onHide.get();
    }

    public void setOnHide(EventHandler<EleFXTooltipEvent> value) {
        onHide.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTooltipEvent>> onHideProperty() {
        return onHide;
    }

    private void initialize() {
        reference.addListener((o, oldNode, newNode) -> {
            if (!isVirtualTriggering()) {
                detach(oldNode);
                attach(newNode);
            }
        });
        virtualRef.addListener((o, oldNode, newNode) -> {
            if (isVirtualTriggering()) {
                detach(oldNode);
                attach(newNode);
            }
        });
        virtualTriggering.addListener((o, wasVirtual, virtual) -> {
            detach(wasVirtual ? getVirtualRef() : getReference());
            attach(activeReference());
        });
        visible.addListener((o, oldValue, value) -> {
            if (value != null) {
                stopTimers();
                if (value)
                    showNow();
                else
                    hideNow();
            }
        });
        disabled.addListener((o, oldValue, value) -> {
            if (value) hideNow(false);
        });
        transitionOffsetX.addListener(o -> updateAnimatedPosition());
        transitionOffsetY.addListener(o -> updateAnimatedPosition());
        content.addListener(o -> refresh());
        contentNode.addListener(o -> refresh());
        rawContent.addListener(o -> refresh());
        effect.addListener(o -> refresh());
        showArrow.addListener(o -> refresh());
        popperClass.addListener(o -> refresh());
        popperStyle.addListener(o -> refresh());
        arrowStyle.addListener(o -> refresh());
        contentStyle.addListener(o -> refresh());
        ariaLabel.addListener(o -> refresh());
        placement.addListener(o -> reposition());
        offset.addListener(o -> reposition());
        arrowOffset.addListener(o -> reposition());
        showTimer.setOnFinished(e -> showNow());
        hideTimer.setOnFinished(e -> {
            if (hoverHidePending && (pointerInReference || (isEnterable() && pointerInTooltip))) return;
            hideNow();
        });
        autoCloseTimer.setOnFinished(e -> hide());
    }

    private Node activeReference() {
        return isVirtualTriggering() ? getVirtualRef() : getReference();
    }

    private void attach(Node node) {
        if (node == null) return;
        node.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEntered);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExited);
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClicked);
        node.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenu);
        node.addEventHandler(KeyEvent.KEY_PRESSED, keyPressed);
        node.focusedProperty().addListener(focusChanged);
    }

    private void detach(Node node) {
        if (node == null) return;
        node.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEntered);
        node.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExited);
        node.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClicked);
        node.removeEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenu);
        node.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressed);
        node.focusedProperty().removeListener(focusChanged);
    }

    private void requestVisibility(boolean value) {
        if (getVisible() != null)
            setVisible(value);
        else if (value)
            scheduleShow();
        else
            scheduleHide();
    }

    private void scheduleShow() {
        hideTimer.stop();
        if (getVisible() != null || isDisabled()) return;
        if (hiding) {
            showNow();
            return;
        }
        if (isShowing()) return;
        showTimer.setDuration(Duration.millis(getShowAfter()));
        showTimer.playFromStart();
    }

    private void scheduleHide() {
        if (getVisible() != null) return;
        hoverHidePending = false;
        showTimer.stop();
        if (!isShowing()) return;
        hideTimer.setDuration(Duration.millis(getHideAfter()));
        hideTimer.playFromStart();
    }

    private void scheduleHoverHide() {
        if (pointerInReference || (isEnterable() && pointerInTooltip)) return;
        scheduleHide();
        hoverHidePending = true;
    }

    private void showAt(double x, double y) {
        if (isDisabled()) return;
        stopTimers();
        showNow(x, y);
    }

    private void showNow() {
        showNow(Double.NaN, Double.NaN);
    }

    private void showNow(double contextX, double contextY) {
        if (isDisabled() || (isShowing() && !hiding)) return;
        Node anchor = activeReference();
        if (anchor == null || anchor.getScene() == null || anchor.getScene().getWindow() == null) return;
        Window owner = anchor.getScene().getWindow();
        if (!owner.isShowing()) return;
        // Hover does not require keyboard focus: an exposed part of an inactive
        // application can still receive a fresh mouse-enter event.
        if (!owner.isFocused() && !(getTrigger() == EleFXTooltipTrigger.HOVER && pointerInReference)) {
            if (Boolean.TRUE.equals(getVisible())) {
                observe(anchor.getScene());
                suspendedForOwner = true;
            }
            return;
        }
        EleFXTooltipTransitions.Definition definition = EleFXTooltipTransitions.resolve(getTransition());
        boolean wasShowing = isShowing();
        boolean interrupted = activeAnimation != null;
        cancelAnimation();
        long generation = animationGeneration;
        hiding = false;
        targetVisible = true;
        createPopup();
        refresh();
        layoutPopup();
        if (!wasShowing) resetAnimationValues();
        emit(EleFXTooltipEvent.BEFORE_SHOW, true, getOnBeforeShow());
        if (generation != animationGeneration || !targetVisible) return;
        if (!wasShowing) {
            if (owner.isFocused()) {
                popup.show(owner, 0, 0);
            } else {
                popup.getContent().remove(popupContainer);
                sceneOverlay = new ScenePopupOverlay(anchor.getScene(), popupContainer);
            }
        }
        if (Double.isFinite(contextX)) {
            basePopupX = contextX - POPUP_MARGIN;
            basePopupY = contextY - POPUP_MARGIN;
            updateAnimatedPosition();
            layoutPopup();
            Bounds bounds = anchor.localToScreen(anchor.getLayoutBounds());
            if (bounds != null) positionArrow(root.getWidth(), root.getHeight(), bounds, getPlacement());
        } else
            reposition();
        showing.set(true);
        observe(anchor.getScene());
        playAnimation(definition.enter(), interrupted, () -> {
            emit(EleFXTooltipEvent.SHOW, true, getOnShow());
            if (generation == animationGeneration && targetVisible && getAutoClose() > 0 && getVisible() == null) {
                autoCloseTimer.setDuration(Duration.millis(getAutoClose()));
                autoCloseTimer.playFromStart();
            }
        });
    }

    private void hideNow() {
        // Do not leave an animated native popup above the foreground window
        // after the user leaves a background hover target.
        hideNow(observedOwner == null || observedOwner.isFocused());
    }

    private void hideNow(boolean animate) {
        stopTimers();
        targetVisible = false;
        if (animate && hiding) return;
        boolean interrupted = activeAnimation != null;
        boolean alreadyHiding = hiding;
        cancelAnimation();
        long generation = animationGeneration;
        if (!isShowing()) {
            unobserve();
            return;
        }
        hiding = true;
        if (!alreadyHiding) emit(EleFXTooltipEvent.BEFORE_HIDE, false, getOnBeforeHide());
        if (generation != animationGeneration || targetVisible) return;
        if (animate) {
            playAnimation(EleFXTooltipTransitions.resolve(getTransition()).leave(), interrupted, this::finishHide);
        } else {
            finishHide();
        }
    }

    private void finishHide() {
        showing.set(false);
        hiding = false;
        targetVisible = false;
        if (sceneOverlay != null) {
            sceneOverlay.hide();
            sceneOverlay = null;
            popup.getContent().add(popupContainer);
        }
        popup.hide();
        pointerInTooltip = false;
        unobserve();
        resetAnimationValues();
        if (!isPersistent()) {
            popup = null;
            root = null;
            popupContainer = null;
            arrow = null;
        }
        emit(EleFXTooltipEvent.HIDE, false, getOnHide());
    }

    private void cancelAnimation() {
        animationGeneration++;
        if (activeAnimation != null) {
            activeAnimation.setOnFinished(null);
            activeAnimation.stop();
            activeAnimation = null;
        }
    }

    private void playAnimation(Function<EleFXTooltipTransitionContext, Animation> factory,
                               boolean interrupted, Runnable finished) {
        long generation = animationGeneration;
        Animation animation;
        try {
            animation = factory.apply(new EleFXTooltipTransitionContext(root,
                    transitionOffsetX, transitionOffsetY, interrupted));
            if (animation != null && (animation.getTotalDuration().isIndefinite()
                    || animation.getTotalDuration().isUnknown())) {
                throw new IllegalArgumentException("Tooltip animations must have a finite duration");
            }
        } catch (RuntimeException failure) {
            if (generation == animationGeneration && root != null) {
                resetAnimationValues();
                finished.run();
            }
            throw failure;
        }
        if (generation != animationGeneration) return;
        if (animation == null) {
            finished.run();
            return;
        }
        activeAnimation = animation;
        EventHandler<ActionEvent> callback = animation.getOnFinished();
        Runnable complete = () -> {
            if (generation != animationGeneration || activeAnimation != animation) return;
            activeAnimation = null;
            try {
                if (callback != null) callback.handle(new ActionEvent(animation, null));
            } finally {
                if (generation == animationGeneration) finished.run();
            }
        };
        animation.setOnFinished(e -> complete.run());
        animation.playFromStart();
        if (animation.getTotalDuration().lessThanOrEqualTo(Duration.ZERO)) complete.run();
    }

    private void resetAnimationValues() {
        root.setOpacity(1);
        transitionOffsetX.set(0);
        transitionOffsetY.set(0);
    }

    private void updateAnimatedPosition() {
        if (isLayerShowing()) {
            popup.setX(basePopupX + transitionOffsetX.get());
            popup.setY(basePopupY + transitionOffsetY.get());
            if (sceneOverlay != null) sceneOverlay.moveTo(popup.getX(), popup.getY());
        }
    }

    private boolean isLayerShowing() {
        return popup != null && (popup.isShowing() || (sceneOverlay != null && sceneOverlay.isShowing()));
    }

    private void stopTimers() {
        showTimer.stop();
        hideTimer.stop();
        autoCloseTimer.stop();
    }

    private void createPopup() {
        if (popup != null) return;
        popup = new Popup();
        popup.setAutoFix(false);
        popup.setAutoHide(false);
        popup.setOnHidden(e -> {
            if (isShowing()) {
                stopTimers();
                cancelAnimation();
                finishHide();
            }
        });
        root = new StackPane();
        root.getStyleClass().add("ele-tooltip");
        root.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        root.getStylesheets().add(EleFXThemes.TOOLTIP.toData());
        popupContainer = new StackPane(root);
        popupContainer.setPickOnBounds(false);
        popupContainer.setPadding(new Insets(POPUP_MARGIN));
        popup.getContent().add(popupContainer);
        popupContainer.setOnMouseEntered(e -> {
            pointerInTooltip = true;
            if (isEnterable()) {
                hideTimer.stop();
                if (getVisible() == null && hiding && getTrigger() == EleFXTooltipTrigger.HOVER) showNow();
            }
        });
        popupContainer.setOnMouseExited(e -> {
            pointerInTooltip = false;
            if (isEnterable() && getTrigger() == EleFXTooltipTrigger.HOVER) scheduleHoverHide();
        });
    }

    private void refresh() {
        if (root == null) return;
        root.getStyleClass().removeIf(s -> s.startsWith("ele-tooltip--") || s.equals(getPopperClass()));
        root.getStyleClass().add("ele-tooltip--" + getEffect().name().toLowerCase());
        if (!getPopperClass().isBlank()) root.getStyleClass().add(getPopperClass());
        root.setStyle(getPopperStyle());
        root.setAccessibleText(getAriaLabel());
        Node value = getContentNode();
        if (value == null) {
            Label label = new Label(getContent());
            label.setStyle(getContentStyle());
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            value = label;
        }
        if (!value.getStyleClass().contains("ele-tooltip__content")) value.getStyleClass().add("ele-tooltip__content");
        if (isShowArrow()) {
            if (arrow == null) {
                arrow = new Region();
                arrow.setManaged(false);
                arrow.setMouseTransparent(true);
                arrow.getStyleClass().add("ele-tooltip__arrow");
            }
            arrow.setStyle(getArrowStyle());
            root.getChildren().setAll(value, arrow);
        } else
            root.getChildren().setAll(value);
        if (isLayerShowing()) reposition();
    }

    private void layoutPopup() {
        popupContainer.applyCss();
        popupContainer.autosize();
        popupContainer.layout();
    }

    private void reposition() {
        Node anchor = activeReference();
        if (anchor == null || root == null || !isLayerShowing()) return;
        Bounds bounds = anchor.localToScreen(anchor.getLayoutBounds());
        if (bounds == null) return;
        layoutPopup();
        double width = root.getWidth(), height = root.getHeight();
        EleFXTooltipPlacement resolved = resolvePlacement(bounds, width, height);
        double x, y;
        if (isHorizontal(resolved)) {
            x = resolved.name().endsWith("_START")
                    ? bounds.getMinX()
                    : resolved.name().endsWith("_END")
                            ? bounds.getMaxX() - width
                            : bounds.getMinX() + (bounds.getWidth() - width) / 2;
            y = resolved.name().startsWith("TOP")
                    ? bounds.getMinY() - height - getOffset()
                    : bounds.getMaxY() + getOffset();
        } else {
            y = resolved.name().endsWith("_START")
                    ? bounds.getMinY()
                    : resolved.name().endsWith("_END")
                            ? bounds.getMaxY() - height
                            : bounds.getMinY() + (bounds.getHeight() - height) / 2;
            x = resolved.name().startsWith("LEFT")
                    ? bounds.getMinX() - width - getOffset()
                    : bounds.getMaxX() + getOffset();
        }
        Rectangle2D screen = Screen
                .getScreensForRectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight())
                .get(0).getVisualBounds();
        basePopupX = Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - width)) - POPUP_MARGIN;
        basePopupY = Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - height)) - POPUP_MARGIN;
        updateAnimatedPosition();
        positionArrow(width, height, bounds, resolved);
    }

    private EleFXTooltipPlacement resolvePlacement(Bounds b, double w, double h) {
        ObservableList<EleFXTooltipPlacement> candidates = FXCollections.observableArrayList();
        candidates.add(getPlacement());
        candidates.addAll(getFallbackPlacements());
        Rectangle2D screen = Screen.getScreensForRectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()).get(0)
                .getVisualBounds();
        for (EleFXTooltipPlacement candidate : candidates) {
            double x = candidate.name().startsWith("LEFT")
                    ? b.getMinX() - w - getOffset()
                    : candidate.name().startsWith("RIGHT")
                            ? b.getMaxX() + getOffset()
                            : b.getMinX() + (b.getWidth() - w) / 2;
            double y = candidate.name().startsWith("TOP")
                    ? b.getMinY() - h - getOffset()
                    : candidate.name().startsWith("BOTTOM")
                            ? b.getMaxY() + getOffset()
                            : b.getMinY() + (b.getHeight() - h) / 2;
            if (x >= screen.getMinX() && y >= screen.getMinY() && x + w <= screen.getMaxX()
                    && y + h <= screen.getMaxY())
                return candidate;
        }
        return getPlacement();
    }

    private static boolean isHorizontal(EleFXTooltipPlacement p) {
        return p.name().startsWith("TOP") || p.name().startsWith("BOTTOM");
    }

    private void positionArrow(double w, double h, Bounds b, EleFXTooltipPlacement p) {
        if (arrow == null || !isShowArrow()) return;
        arrow.getStyleClass().removeIf(s -> s.startsWith("ele-tooltip__arrow--"));
        String side = p.name().startsWith("TOP")
                ? "bottom"
                : p.name().startsWith("BOTTOM")
                        ? "top"
                        : p.name().startsWith("LEFT") ? "right" : "left";
        arrow.getStyleClass().add("ele-tooltip__arrow--" + side);
        double minimum = getArrowOffset(), x = Math.max(minimum,
                Math.min(w - ARROW_SIZE - minimum,
                        b.getMinX() + b.getWidth() / 2 - basePopupX - POPUP_MARGIN - ARROW_SIZE / 2));
        double y = Math.max(minimum,
                Math.min(h - ARROW_SIZE - minimum,
                        b.getMinY() + b.getHeight() / 2 - basePopupY - POPUP_MARGIN - ARROW_SIZE / 2));
        if (p.name().startsWith("TOP"))
            arrow.resizeRelocate(x, h - ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
        else if (p.name().startsWith("BOTTOM"))
            arrow.resizeRelocate(x, -ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
        else if (p.name().startsWith("LEFT"))
            arrow.resizeRelocate(w - ARROW_SIZE / 2, y, ARROW_SIZE, ARROW_SIZE);
        else
            arrow.resizeRelocate(-ARROW_SIZE / 2, y, ARROW_SIZE, ARROW_SIZE);
    }

    private void observe(Scene scene) {
        unobserve();
        observedScene = scene;
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressed);
        observedOwner = scene.getWindow();
        if (observedOwner != null) {
            observedOwner.focusedProperty().addListener(ownerStateChanged);
            observedOwner.showingProperty().addListener(ownerStateChanged);
        }
    }

    private void unobserve() {
        suspendedForOwner = false;
        if (observedOwner != null) {
            observedOwner.focusedProperty().removeListener(ownerStateChanged);
            observedOwner.showingProperty().removeListener(ownerStateChanged);
            observedOwner = null;
        }
        if (observedScene != null) {
            observedScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressed);
            observedScene = null;
        }
    }

    private void updateOwnerState() {
        Window owner = observedOwner;
        if (owner == null) return;
        if (!owner.isFocused()) pointerInReference = false;
        if (!owner.isShowing()) {
            hideNow(false);
        } else if (!owner.isFocused() && !suspendedForOwner) {
            Scene scene = observedScene;
            boolean restore = Boolean.TRUE.equals(getVisible());
            // Deactivation must bypass hideAfter and the leave animation.
            hideNow(false);
            if (restore && Boolean.TRUE.equals(getVisible()) && owner.isShowing()) {
                observe(scene);
                suspendedForOwner = true;
                if (owner.isFocused()) updateOwnerState();
            }
        } else if (owner.isFocused() && suspendedForOwner) {
            suspendedForOwner = false;
            if (Boolean.TRUE.equals(getVisible()))
                showNow();
            else
                unobserve();
        }
    }

    private boolean isInside(Object target) {
        if (!(target instanceof Node current)) return false;
        for (Node n = current; n != null; n = n.getParent())
            if (n == activeReference() || n == root) return true;
        return false;
    }

    private void emit(javafx.event.EventType<EleFXTooltipEvent> type, boolean state,
                      EventHandler<EleFXTooltipEvent> handler) {
        if (handler != null) handler.handle(new EleFXTooltipEvent(this, null, type, state, getTrigger()));
    }

    private static void nonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0)
            throw new IllegalArgumentException(name + " must be finite and non-negative");
    }

    private static void nonNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " must not be negative");
    }
}
