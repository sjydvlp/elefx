package com.sjydvlp.elefx.component.tooltip;

import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.animation.FadeTransition;
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
import javafx.geometry.Bounds;
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
import javafx.util.Duration;

/**
 * Element Plus-inspired tooltip attached to a JavaFX node. It supports the
 * documented placement, trigger, delay, controlled visibility and virtual
 * triggering behaviour while keeping all content JavaFX-native.
 */
public final class EleFXTooltip {

    private static final double ARROW_SIZE = 10;

    private static final Duration FADE_DURATION = Duration.millis(150);

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

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeShow = new SimpleObjectProperty<>(this,
            "onBeforeShow");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onShow = new SimpleObjectProperty<>(this, "onShow");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onBeforeHide = new SimpleObjectProperty<>(this,
            "onBeforeHide");

    private final ObjectProperty<EventHandler<EleFXTooltipEvent>> onHide = new SimpleObjectProperty<>(this, "onHide");

    private Popup popup;

    private StackPane root;

    private Region arrow;

    private PauseTransition showTimer = new PauseTransition();

    private PauseTransition hideTimer = new PauseTransition();

    private PauseTransition autoCloseTimer = new PauseTransition();

    private Scene observedScene;

    private boolean pointerInReference;

    private boolean pointerInTooltip;

    private final EventHandler<MouseEvent> mouseEntered = e -> {
        pointerInReference = true;
        if (getTrigger() == EleFXTooltipTrigger.HOVER) {
            if (isFocusOnTarget()) activeReference().requestFocus();
            scheduleShow();
        }
    };

    private final EventHandler<MouseEvent> mouseExited = e -> {
        pointerInReference = false;
        if (getTrigger() == EleFXTooltipTrigger.HOVER) scheduleHide();
    };

    private final EventHandler<MouseEvent> mouseClicked = e -> {
        if (getTrigger() == EleFXTooltipTrigger.CLICK) toggle();
    };

    private final EventHandler<ContextMenuEvent> contextMenu = e -> {
        if (getTrigger() == EleFXTooltipTrigger.CONTEXT_MENU) {
            showAt(e.getScreenX(), e.getScreenY());
            e.consume();
        }
    };

    private final EventHandler<KeyEvent> keyPressed = e -> {
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
        requestVisibility(!isShowing());
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

    public void setTransition(String value) {
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
            if (value) hideNow();
        });
        content.addListener(o -> refresh());
        contentNode.addListener(o -> refresh());
        rawContent.addListener(o -> refresh());
        effect.addListener(o -> refresh());
        showArrow.addListener(o -> refresh());
        popperClass.addListener(o -> refresh());
        popperStyle.addListener(o -> refresh());
        ariaLabel.addListener(o -> refresh());
        placement.addListener(o -> reposition());
        offset.addListener(o -> reposition());
        arrowOffset.addListener(o -> reposition());
        showTimer.setOnFinished(e -> showNow());
        hideTimer.setOnFinished(e -> hideNow());
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
        if (isDisabled() || isShowing()) return;
        hideTimer.stop();
        showTimer.setDuration(Duration.millis(getShowAfter()));
        showTimer.playFromStart();
    }

    private void scheduleHide() {
        showTimer.stop();
        if (!isShowing()) return;
        hideTimer.setDuration(Duration.millis(getHideAfter()));
        hideTimer.playFromStart();
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
        if (isDisabled() || isShowing()) return;
        Node anchor = activeReference();
        if (anchor == null || anchor.getScene() == null || anchor.getScene().getWindow() == null) return;
        createPopup();
        refresh();
        root.applyCss();
        root.autosize();
        emit(EleFXTooltipEvent.BEFORE_SHOW, true, getOnBeforeShow());
        popup.show(anchor.getScene().getWindow(), 0, 0);
        if (Double.isFinite(contextX)) {
            popup.setX(contextX);
            popup.setY(contextY);
        } else
            reposition();
        showing.set(true);
        observe(anchor.getScene());
        emit(EleFXTooltipEvent.SHOW, true, getOnShow());
        if (!getTransition().isBlank()) {
            root.setOpacity(0);
            FadeTransition fade = new FadeTransition(FADE_DURATION, root);
            fade.setToValue(1);
            fade.play();
        }
        if (getAutoClose() > 0 && getVisible() == null) {
            autoCloseTimer.setDuration(Duration.millis(getAutoClose()));
            autoCloseTimer.playFromStart();
        }
    }

    private void hideNow() {
        stopTimers();
        if (!isShowing()) return;
        emit(EleFXTooltipEvent.BEFORE_HIDE, false, getOnBeforeHide());
        popup.hide();
        showing.set(false);
        unobserve();
        emit(EleFXTooltipEvent.HIDE, false, getOnHide());
        if (!isPersistent()) {
            popup = null;
            root = null;
            arrow = null;
        }
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
        root = new StackPane();
        root.getStyleClass().add("ele-tooltip");
        root.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        root.getStylesheets().add(EleFXThemes.TOOLTIP.toData());
        popup.getContent().add(root);
        root.setOnMouseEntered(e -> {
            pointerInTooltip = true;
            if (isEnterable()) hideTimer.stop();
        });
        root.setOnMouseExited(e -> {
            pointerInTooltip = false;
            if (isEnterable() && getTrigger() == EleFXTooltipTrigger.HOVER && !pointerInReference) scheduleHide();
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
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            value = label;
        }
        if (!value.getStyleClass().contains("ele-tooltip__content")) value.getStyleClass().add("ele-tooltip__content");
        if (isShowArrow()) {
            if (arrow == null) {
                arrow = new Region();
                arrow.getStyleClass().add("ele-tooltip__arrow");
            }
            root.getChildren().setAll(value, arrow);
        } else
            root.getChildren().setAll(value);
    }

    private void reposition() {
        Node anchor = activeReference();
        if (anchor == null || root == null || popup == null || !popup.isShowing()) return;
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds == null) return;
        root.applyCss();
        root.autosize();
        double width = root.prefWidth(-1), height = root.prefHeight(width);
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
        popup.setX(Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - width)));
        popup.setY(Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - height)));
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
        if (arrow == null) return;
        double minimum = getArrowOffset(), x = Math.max(minimum,
                Math.min(w - ARROW_SIZE - minimum, b.getMinX() + b.getWidth() / 2 - popup.getX() - ARROW_SIZE / 2));
        double y = Math.max(minimum,
                Math.min(h - ARROW_SIZE - minimum, b.getMinY() + b.getHeight() / 2 - popup.getY() - ARROW_SIZE / 2));
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
    }

    private void unobserve() {
        if (observedScene != null) {
            observedScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressed);
            observedScene = null;
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
