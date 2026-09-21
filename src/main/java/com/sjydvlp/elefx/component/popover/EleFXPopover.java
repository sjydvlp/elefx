package com.sjydvlp.elefx.component.popover;

import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Element Plus-inspired floating content panel. A popover can display text or
 * arbitrary JavaFX content next to a reference node, including virtual anchors.
 */
public final class EleFXPopover {

    private static final double ARROW_SIZE = 10;

    private final ObjectProperty<Node> reference = new SimpleObjectProperty<>(this, "reference");

    private final ObjectProperty<Node> virtualRef = new SimpleObjectProperty<>(this, "virtualRef");

    private final BooleanProperty virtualTriggering = new SimpleBooleanProperty(this, "virtualTriggering", false);

    private final ObjectProperty<EleFXPopoverTrigger> trigger = new SimpleObjectProperty<>(this, "trigger",
            EleFXPopoverTrigger.HOVER);

    private final ObservableList<KeyCode> triggerKeys = FXCollections.observableArrayList(KeyCode.ENTER, KeyCode.SPACE);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty content = new SimpleStringProperty(this, "content", "");

    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>(this, "contentNode");

    private final ObjectProperty<EleFXPopoverPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXPopoverPlacement.BOTTOM);

    private final ObjectProperty<EleFXPopoverEffect> effect = new SimpleObjectProperty<>(this, "effect",
            EleFXPopoverEffect.LIGHT);

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 150);

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 12);

    private final BooleanProperty showArrow = new SimpleBooleanProperty(this, "showArrow", true);

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    /** Null means uncontrolled; a non-null value makes visibility externally controlled. */
    private final ObjectProperty<Boolean> visible = new SimpleObjectProperty<>(this, "visible", null);

    private final IntegerProperty showAfter = new SimpleIntegerProperty(this, "showAfter", 0);

    private final IntegerProperty hideAfter = new SimpleIntegerProperty(this, "hideAfter", 200);

    private final IntegerProperty autoClose = new SimpleIntegerProperty(this, "autoClose", 0);

    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent", true);

    private final BooleanProperty teleported = new SimpleBooleanProperty(this, "teleported", true);

    private final StringProperty popperClass = new SimpleStringProperty(this, "popperClass", "");

    private final IntegerProperty tabIndex = new SimpleIntegerProperty(this, "tabIndex", 0);

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onShow = new SimpleObjectProperty<>(this, "onShow");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onHide = new SimpleObjectProperty<>(this, "onHide");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onBeforeEnter = new SimpleObjectProperty<>(this,
            "onBeforeEnter");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onAfterEnter = new SimpleObjectProperty<>(this,
            "onAfterEnter");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onBeforeLeave = new SimpleObjectProperty<>(this,
            "onBeforeLeave");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onAfterLeave = new SimpleObjectProperty<>(this,
            "onAfterLeave");

    private final ObjectProperty<EventHandler<EleFXPopoverEvent>> onVisibleChange = new SimpleObjectProperty<>(this,
            "onVisibleChange");

    private Popup popup;

    private VBox root;

    private Region arrow;

    private PauseTransition showTimer = new PauseTransition();

    private PauseTransition hideTimer = new PauseTransition();

    private PauseTransition autoCloseTimer = new PauseTransition();

    private Scene observedScene;

    private boolean pointerInReference;

    private boolean pointerInPopover;

    private final EventHandler<MouseEvent> mouseEntered = e -> {
        pointerInReference = true;
        if (getTrigger() == EleFXPopoverTrigger.HOVER) scheduleShow();
    };

    private final EventHandler<MouseEvent> mouseExited = e -> {
        pointerInReference = false;
        if (getTrigger() == EleFXPopoverTrigger.HOVER) scheduleHide();
    };

    private final EventHandler<MouseEvent> mouseClicked = e -> {
        if (getTrigger() == EleFXPopoverTrigger.CLICK) toggle();
    };

    private final EventHandler<ContextMenuEvent> contextMenu = e -> {
        if (getTrigger() == EleFXPopoverTrigger.CONTEXT_MENU) {
            showAt(e.getScreenX(), e.getScreenY());
            e.consume();
        }
    };

    private final EventHandler<KeyEvent> keyPressed = e -> {
        if (e.getCode() == KeyCode.ESCAPE)
            hide();
        else if (getTrigger() != EleFXPopoverTrigger.MANUAL && triggerKeys.contains(e.getCode())) {
            toggle();
            e.consume();
        }
    };

    private final ChangeListener<Boolean> focusChanged = (o, wasFocused, isFocused) -> {
        if (getTrigger() == EleFXPopoverTrigger.FOCUS) {
            if (isFocused)
                scheduleShow();
            else
                scheduleHide();
        }
    };

    private final EventHandler<MouseEvent> outsidePressed = e -> {
        if (isShowing() && !isInside(e.getTarget(), activeReference())) hide();
    };

    private final EventHandler<KeyEvent> escapePressed = e -> {
        if (e.getCode() == KeyCode.ESCAPE && isShowing()) {
            hide();
            e.consume();
        }
    };

    public EleFXPopover() {
        initialize();
    }

    public EleFXPopover(Node reference, String content) {
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

    public Popup getPopperRef() {
        return popup;
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

    public EleFXPopoverTrigger getTrigger() {
        return trigger.get();
    }

    public void setTrigger(EleFXPopoverTrigger value) {
        trigger.set(value == null ? EleFXPopoverTrigger.HOVER : value);
    }

    public ObjectProperty<EleFXPopoverTrigger> triggerProperty() {
        return trigger;
    }

    public ObservableList<KeyCode> getTriggerKeys() {
        return triggerKeys;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public StringProperty titleProperty() {
        return title;
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

    public EleFXPopoverPlacement getPlacement() {
        return placement.get();
    }

    public void setPlacement(EleFXPopoverPlacement value) {
        placement.set(value == null ? EleFXPopoverPlacement.BOTTOM : value);
    }

    public ObjectProperty<EleFXPopoverPlacement> placementProperty() {
        return placement;
    }

    public EleFXPopoverEffect getEffect() {
        return effect.get();
    }

    public void setEffect(EleFXPopoverEffect value) {
        effect.set(value == null ? EleFXPopoverEffect.LIGHT : value);
    }

    public ObjectProperty<EleFXPopoverEffect> effectProperty() {
        return effect;
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(double value) {
        if (value < 0 || !Double.isFinite(value))
            throw new IllegalArgumentException("width must be finite and non-negative");
        width.set(value);
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public double getOffset() {
        return offset.get();
    }

    public void setOffset(double value) {
        if (value < 0 || !Double.isFinite(value))
            throw new IllegalArgumentException("offset must be finite and non-negative");
        offset.set(value);
    }

    public DoubleProperty offsetProperty() {
        return offset;
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

    public boolean isPersistent() {
        return persistent.get();
    }

    public void setPersistent(boolean value) {
        persistent.set(value);
    }

    public BooleanProperty persistentProperty() {
        return persistent;
    }

    /** JavaFX popup windows are always overlays; retained for Element Plus API parity. */
    public boolean isTeleported() {
        return teleported.get();
    }

    public void setTeleported(boolean value) {
        teleported.set(value);
    }

    public BooleanProperty teleportedProperty() {
        return teleported;
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

    public int getTabIndex() {
        return tabIndex.get();
    }

    public void setTabIndex(int value) {
        tabIndex.set(value);
    }

    public IntegerProperty tabIndexProperty() {
        return tabIndex;
    }

    public EventHandler<EleFXPopoverEvent> getOnShow() {
        return onShow.get();
    }

    public void setOnShow(EventHandler<EleFXPopoverEvent> value) {
        onShow.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onShowProperty() {
        return onShow;
    }

    public EventHandler<EleFXPopoverEvent> getOnHide() {
        return onHide.get();
    }

    public void setOnHide(EventHandler<EleFXPopoverEvent> value) {
        onHide.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onHideProperty() {
        return onHide;
    }

    public EventHandler<EleFXPopoverEvent> getOnBeforeEnter() {
        return onBeforeEnter.get();
    }

    public void setOnBeforeEnter(EventHandler<EleFXPopoverEvent> value) {
        onBeforeEnter.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onBeforeEnterProperty() {
        return onBeforeEnter;
    }

    public EventHandler<EleFXPopoverEvent> getOnAfterEnter() {
        return onAfterEnter.get();
    }

    public void setOnAfterEnter(EventHandler<EleFXPopoverEvent> value) {
        onAfterEnter.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onAfterEnterProperty() {
        return onAfterEnter;
    }

    public EventHandler<EleFXPopoverEvent> getOnBeforeLeave() {
        return onBeforeLeave.get();
    }

    public void setOnBeforeLeave(EventHandler<EleFXPopoverEvent> value) {
        onBeforeLeave.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onBeforeLeaveProperty() {
        return onBeforeLeave;
    }

    public EventHandler<EleFXPopoverEvent> getOnAfterLeave() {
        return onAfterLeave.get();
    }

    public void setOnAfterLeave(EventHandler<EleFXPopoverEvent> value) {
        onAfterLeave.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onAfterLeaveProperty() {
        return onAfterLeave;
    }

    public EventHandler<EleFXPopoverEvent> getOnVisibleChange() {
        return onVisibleChange.get();
    }

    public void setOnVisibleChange(EventHandler<EleFXPopoverEvent> value) {
        onVisibleChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPopoverEvent>> onVisibleChangeProperty() {
        return onVisibleChange;
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
        virtualTriggering.addListener((o, wasVirtual, isVirtual) -> {
            detach(wasVirtual ? getVirtualRef() : getReference());
            attach(activeReference());
        });
        visible.addListener((o, oldValue, newValue) -> {
            if (newValue != null) {
                showTimer.stop();
                hideTimer.stop();
                if (newValue)
                    showNow();
                else
                    hideNow();
            }
        });
        disabled.addListener((o, oldValue, newValue) -> {
            if (newValue) hideNow();
        });
        title.addListener(o -> refresh());
        content.addListener(o -> refresh());
        contentNode.addListener(o -> refresh());
        effect.addListener(o -> refresh());
        width.addListener(o -> refresh());
        showArrow.addListener(o -> refresh());
        popperClass.addListener(o -> refresh());
        placement.addListener(o -> reposition());
        offset.addListener(o -> reposition());
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
        node.setFocusTraversable(true);
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
        else
            setShowing(value);
    }

    private void setShowing(boolean value) {
        if (value)
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
        showTimer.stop();
        hideTimer.stop();
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
        emit(EleFXPopoverEvent.BEFORE_ENTER, true, getOnBeforeEnter());
        popup.show(anchor.getScene().getWindow(), 0, 0);
        if (Double.isFinite(contextX)) {
            popup.setX(contextX);
            popup.setY(contextY);
        } else
            reposition();
        showing.set(true);
        observe(anchor.getScene());
        emit(EleFXPopoverEvent.SHOW, true, getOnShow());
        emit(EleFXPopoverEvent.AFTER_ENTER, true, getOnAfterEnter());
        emit(EleFXPopoverEvent.VISIBLE_CHANGE, true, getOnVisibleChange());
        if (getAutoClose() > 0) {
            autoCloseTimer.setDuration(Duration.millis(getAutoClose()));
            autoCloseTimer.playFromStart();
        }
    }

    private void hideNow() {
        showTimer.stop();
        hideTimer.stop();
        autoCloseTimer.stop();
        if (!isShowing()) return;
        emit(EleFXPopoverEvent.BEFORE_LEAVE, false, getOnBeforeLeave());
        popup.hide();
        showing.set(false);
        unobserve();
        emit(EleFXPopoverEvent.HIDE, false, getOnHide());
        emit(EleFXPopoverEvent.AFTER_LEAVE, false, getOnAfterLeave());
        emit(EleFXPopoverEvent.VISIBLE_CHANGE, false, getOnVisibleChange());
        if (!isPersistent()) {
            popup = null;
            root = null;
            arrow = null;
        }
    }

    private void createPopup() {
        if (popup != null) return;
        popup = new Popup();
        popup.setAutoFix(false);
        popup.setAutoHide(false);
        root = new VBox();
        root.getStyleClass().add("ele-popover");
        root.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        root.getStylesheets().add(EleFXThemes.POPOVER.toData());
        popup.getContent().add(root);
        root.setOnMouseEntered(e -> {
            pointerInPopover = true;
            hideTimer.stop();
        });
        root.setOnMouseExited(e -> {
            pointerInPopover = false;
            if (getTrigger() == EleFXPopoverTrigger.HOVER && !pointerInReference) scheduleHide();
        });
    }

    private void refresh() {
        if (root == null) return;
        root.getStyleClass().removeIf(s -> s.startsWith("ele-popover--") || s.equals(getPopperClass()));
        root.getStyleClass().add("ele-popover--" + getEffect().name().toLowerCase());
        if (!getPopperClass().isBlank()) root.getStyleClass().add(getPopperClass());
        root.setPrefWidth(getWidth());
        root.setMinWidth(getWidth());
        VBox body = new VBox();
        body.getStyleClass().add("ele-popover__body");
        if (!getTitle().isBlank()) {
            Label heading = new Label(getTitle());
            heading.getStyleClass().add("ele-popover__title");
            heading.setWrapText(true);
            body.getChildren().add(heading);
        }
        Node value = getContentNode();
        if (value == null) {
            Label label = new Label(getContent());
            label.getStyleClass().add("ele-popover__content");
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            value = label;
        }
        value.getStyleClass().add("ele-popover__content");
        body.getChildren().add(value);
        if (isShowArrow()) {
            if (arrow == null) {
                arrow = new Region();
                arrow.getStyleClass().add("ele-popover__arrow");
            }
            updateArrowClass();
            root.getChildren().setAll(body, arrow);
        } else
            root.getChildren().setAll(body);
    }

    private void updateArrowClass() {
        arrow.getStyleClass().removeIf(s -> s.startsWith("ele-popover__arrow--"));
        String side = switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> "bottom";
            case BOTTOM_START, BOTTOM, BOTTOM_END -> "top";
            case LEFT_START, LEFT, LEFT_END -> "right";
            default -> "left";
        };
        arrow.getStyleClass().add("ele-popover__arrow--" + side);
    }

    private void reposition() {
        Node anchor = activeReference();
        if (anchor == null || root == null || popup == null || !popup.isShowing()) return;
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b == null) return;
        root.applyCss();
        root.autosize();
        double w = root.prefWidth(-1), h = root.prefHeight(w);
        EleFXPopoverPlacement p = getPlacement();
        boolean horizontal = p.name().startsWith("TOP") || p.name().startsWith("BOTTOM");
        double x, y;
        if (horizontal) {
            x = p.name().endsWith("_START")
                    ? b.getMinX()
                    : p.name().endsWith("_END") ? b.getMaxX() - w : b.getMinX() + (b.getWidth() - w) / 2;
            y = p.name().startsWith("TOP") ? b.getMinY() - h - getOffset() : b.getMaxY() + getOffset();
        } else {
            y = p.name().endsWith("_START")
                    ? b.getMinY()
                    : p.name().endsWith("_END") ? b.getMaxY() - h : b.getMinY() + (b.getHeight() - h) / 2;
            x = p.name().startsWith("LEFT") ? b.getMinX() - w - getOffset() : b.getMaxX() + getOffset();
        }
        Rectangle2D screen = Screen.getScreensForRectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()).get(0)
                .getVisualBounds();
        popup.setX(Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - w)));
        popup.setY(Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - h)));
        positionArrow(w, h, b);
    }

    private void positionArrow(double w, double h, Bounds b) {
        if (arrow == null || popup == null) return;
        double x = Math.max(8, Math.min(w - 18, b.getMinX() + b.getWidth() / 2 - popup.getX() - ARROW_SIZE / 2));
        double y = Math.max(8, Math.min(h - 18, b.getMinY() + b.getHeight() / 2 - popup.getY() - ARROW_SIZE / 2));
        switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> arrow.resizeRelocate(x, h - ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
            case BOTTOM_START, BOTTOM, BOTTOM_END -> arrow.resizeRelocate(x, -ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
            case LEFT_START, LEFT, LEFT_END -> arrow.resizeRelocate(w - ARROW_SIZE / 2, y, ARROW_SIZE, ARROW_SIZE);
            default -> arrow.resizeRelocate(-ARROW_SIZE / 2, y, ARROW_SIZE, ARROW_SIZE);
        }
    }

    private void observe(Scene scene) {
        unobserve();
        observedScene = scene;
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressed);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escapePressed);
    }

    private void unobserve() {
        if (observedScene != null) {
            observedScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressed);
            observedScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapePressed);
            observedScene = null;
        }
    }

    private boolean isInside(Object target, Node node) {
        if (!(target instanceof Node current)) return false;
        for (Node n = current; n != null; n = n.getParent())
            if (n == node || n == root) return true;
        return false;
    }

    private void emit(javafx.event.EventType<EleFXPopoverEvent> type, boolean state,
                      EventHandler<EleFXPopoverEvent> handler) {
        EleFXPopoverEvent event = new EleFXPopoverEvent(this, null, type, state);
        if (handler != null) handler.handle(event);
    }

    private static void nonNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " must not be negative");
    }
}
