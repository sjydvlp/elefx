package com.sjydvlp.elefx.component.popover;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.internal.ScenePopupOverlay;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Element Plus-inspired floating content panel. A popover can display text or
 * arbitrary JavaFX content next to a reference node.
 */
public final class EleFXPopover {

    private static final double ARROW_SIZE = 10;

    // Keep the shadow and rotated arrow inside stable popup window bounds.
    private static final double SHADOW_MARGIN = 24;

    private final ObjectProperty<Node> reference = new SimpleObjectProperty<>(this, "reference");

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

    /** Null enables automatic triggers; a non-null value gives visibility control to the caller. */
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

    private ScenePopupOverlay sceneOverlay;

    private VBox root;

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

    private boolean pointerInPopover;

    private boolean hoverHidePending;

    private final EventHandler<MouseEvent> mouseEntered = e -> {
        pointerInReference = true;
        if (getTrigger() == EleFXPopoverTrigger.HOVER) scheduleShow();
    };

    private final EventHandler<MouseEvent> mouseExited = e -> {
        pointerInReference = false;
        if (getTrigger() == EleFXPopoverTrigger.HOVER) scheduleHoverHide();
    };

    private final EventHandler<MouseEvent> mouseClicked = e -> {
        if (getVisible() != null) return;
        if (getTrigger() == EleFXPopoverTrigger.CLICK)
            toggle();
        else if (getTrigger() == EleFXPopoverTrigger.CONTEXT_MENU && e.getButton() == MouseButton.PRIMARY) hide();
    };

    private final EventHandler<ContextMenuEvent> contextMenu = e -> {
        if (getVisible() == null && getTrigger() == EleFXPopoverTrigger.CONTEXT_MENU) {
            showTimer.stop();
            hideTimer.stop();
            showNow();
            e.consume();
        }
    };

    private final EventHandler<KeyEvent> keyPressed = e -> {
        if (getVisible() != null) return;
        if (e.getCode() == KeyCode.ESCAPE)
            hide();
        else if (triggerKeys.contains(e.getCode())) {
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
        if (getVisible() == null && isShowing() && !isInside(e.getTarget(), getReference())) hide();
    };

    private final EventHandler<KeyEvent> escapePressed = e -> {
        if (getVisible() == null && e.getCode() == KeyCode.ESCAPE && isShowing()) {
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

    /** Native popup handle; background hover content is hosted in the owner's Scene instead. */
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
            detach(oldNode);
            attach(newNode);
        });
        visible.addListener((o, oldValue, newValue) -> {
            if (newValue != null) {
                showTimer.stop();
                hideTimer.stop();
                autoCloseTimer.stop();
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
        hideTimer.setOnFinished(e -> {
            if (hoverHidePending && (pointerInReference || pointerInPopover)) return;
            hideNow();
        });
        autoCloseTimer.setOnFinished(e -> {
            if (getVisible() == null) hide();
        });
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
        hideTimer.stop();
        if (getVisible() != null || isDisabled() || isShowing()) return;
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
        if (pointerInReference || pointerInPopover) return;
        scheduleHide();
        hoverHidePending = true;
    }

    private void showNow() {
        if (isDisabled() || isShowing()) return;
        Node anchor = getReference();
        if (anchor == null || anchor.getScene() == null || anchor.getScene().getWindow() == null) return;
        Window owner = anchor.getScene().getWindow();
        if (!owner.isShowing()) return;
        // A fresh hover over an exposed reference is valid even when another
        // window owns keyboard focus. Delayed/programmatic background shows
        // remain suppressed.
        if (!owner.isFocused() && !(getTrigger() == EleFXPopoverTrigger.HOVER && pointerInReference)) {
            if (Boolean.TRUE.equals(getVisible())) {
                observe(anchor.getScene());
                suspendedForOwner = true;
            }
            return;
        }
        createPopup();
        refresh();
        popupContainer.applyCss();
        popupContainer.autosize();
        popupContainer.layout();
        emit(EleFXPopoverEvent.BEFORE_ENTER, true, getOnBeforeEnter());
        if (owner.isFocused()) {
            popup.show(owner, 0, 0);
        } else {
            popup.getContent().remove(popupContainer);
            sceneOverlay = new ScenePopupOverlay(anchor.getScene(), popupContainer);
        }
        reposition();
        showing.set(true);
        observe(anchor.getScene());
        emit(EleFXPopoverEvent.SHOW, true, getOnShow());
        emit(EleFXPopoverEvent.AFTER_ENTER, true, getOnAfterEnter());
        emit(EleFXPopoverEvent.VISIBLE_CHANGE, true, getOnVisibleChange());
        if (getVisible() == null && getAutoClose() > 0) {
            autoCloseTimer.setDuration(Duration.millis(getAutoClose()));
            autoCloseTimer.playFromStart();
        }
    }

    private void hideNow() {
        showTimer.stop();
        hideTimer.stop();
        autoCloseTimer.stop();
        if (!isShowing()) {
            unobserve();
            return;
        }
        emit(EleFXPopoverEvent.BEFORE_LEAVE, false, getOnBeforeLeave());
        if (sceneOverlay != null) {
            sceneOverlay.hide();
            sceneOverlay = null;
            popup.getContent().add(popupContainer);
        }
        popup.hide();
        pointerInPopover = false;
        showing.set(false);
        unobserve();
        emit(EleFXPopoverEvent.HIDE, false, getOnHide());
        emit(EleFXPopoverEvent.AFTER_LEAVE, false, getOnAfterLeave());
        emit(EleFXPopoverEvent.VISIBLE_CHANGE, false, getOnVisibleChange());
        if (!isPersistent()) {
            popup = null;
            root = null;
            popupContainer = null;
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
        popupContainer = new StackPane(root);
        popupContainer.setPickOnBounds(false);
        popupContainer.setPadding(new Insets(SHADOW_MARGIN));
        popup.getContent().add(popupContainer);
        // The popup window also covers the transparent shadow padding. Treat it
        // as part of the hover surface so crossing it cannot close the panel.
        popupContainer.setOnMouseEntered(e -> {
            pointerInPopover = true;
            hideTimer.stop();
        });
        popupContainer.setOnMouseExited(e -> {
            pointerInPopover = false;
            if (getTrigger() == EleFXPopoverTrigger.HOVER) scheduleHoverHide();
        });
    }

    private void refresh() {
        if (root == null) return;
        root.getStyleClass().removeIf(s -> s.startsWith("ele-popover--") || s.equals(getPopperClass()));
        root.getStyleClass().add("ele-popover--" + getEffect().name().toLowerCase());
        if (!getPopperClass().isBlank()) root.getStyleClass().add(getPopperClass());
        root.setPrefWidth(getWidth());
        root.setMinWidth(getWidth());
        root.setMaxWidth(getWidth());
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
                // Unmanaged overlay: VBox must not allocate a row for the arrow.
                arrow.setManaged(false);
                arrow.setMouseTransparent(true);
                arrow.getStyleClass().add("ele-popover__arrow");
            }
            updateArrowClass();
            root.getChildren().setAll(body, arrow);
        } else
            root.getChildren().setAll(body);
        if (isLayerShowing()) reposition();
    }

    private boolean isLayerShowing() {
        return popup != null && (popup.isShowing() || (sceneOverlay != null && sceneOverlay.isShowing()));
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
        Node anchor = getReference();
        if (anchor == null || root == null || !isLayerShowing()) return;
        if (arrow != null && isShowArrow()) updateArrowClass();
        // Focus effects can expand boundsInLocal when the popup takes focus.
        Bounds b = anchor.localToScreen(anchor.getLayoutBounds());
        if (b == null) return;
        popupContainer.applyCss();
        popupContainer.autosize();
        popupContainer.layout();
        double w = root.getWidth(), h = root.getHeight();
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
        popup.setX(Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - w)) - SHADOW_MARGIN);
        popup.setY(Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - h)) - SHADOW_MARGIN);
        if (sceneOverlay != null) sceneOverlay.moveTo(popup.getX(), popup.getY());
        positionArrow(w, h, b);
    }

    private void positionArrow(double w, double h, Bounds b) {
        if (arrow == null || popup == null) return;
        double x = Math.max(8,
                Math.min(w - 18, b.getMinX() + b.getWidth() / 2 - popup.getX() - SHADOW_MARGIN - ARROW_SIZE / 2));
        double y = Math.max(8,
                Math.min(h - 18, b.getMinY() + b.getHeight() / 2 - popup.getY() - SHADOW_MARGIN - ARROW_SIZE / 2));
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
            observedScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapePressed);
            observedScene = null;
        }
    }

    private void updateOwnerState() {
        Window owner = observedOwner;
        if (owner == null) return;
        if (!owner.isFocused()) pointerInReference = false;
        if (!owner.isShowing()) {
            hideNow();
        } else if (!owner.isFocused() && !suspendedForOwner) {
            Scene scene = observedScene;
            boolean restore = Boolean.TRUE.equals(getVisible());
            hideNow();
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
