package com.sjydvlp.elefx.component.dropdown;

import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.button.EleFXButtonSize;
import com.sjydvlp.elefx.component.button.EleFXButtonType;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

/** Element Plus-inspired dropdown with hover, click, context-menu, and split-button modes. */
public class EleFXDropdown extends StackPane implements Themable {

    // The rotated 10 px arrow projects 5 px toward the trigger. Keep Element Plus'
    // 12 px popper offset so its tip still has a visible, comfortable gap.
    private static final double POPUP_OFFSET = 12;

    private final StringProperty text = new SimpleStringProperty(this, "text", "Dropdown List");

    private final ObjectProperty<Node> triggerNode = new SimpleObjectProperty<>(this, "triggerNode");

    private final ObjectProperty<Node> triggerIcon = new SimpleObjectProperty<>(this, "triggerIcon");

    private final ObjectProperty<Node> virtualRef = new SimpleObjectProperty<>(this, "virtualRef");

    private final ObjectProperty<EleFXDropdownTrigger> trigger = new SimpleObjectProperty<>(this, "trigger",
            EleFXDropdownTrigger.HOVER);

    private final ObjectProperty<EleFXDropdownPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXDropdownPlacement.BOTTOM);

    private final ObjectProperty<EleFXButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFXButtonType.DEFAULT);

    private final ObjectProperty<EleFXDropdownSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXDropdownSize.DEFAULT);

    private final BooleanProperty splitButton = new SimpleBooleanProperty(this, "splitButton", false);

    private final BooleanProperty hideOnClick = new SimpleBooleanProperty(this, "hideOnClick", true);

    private final BooleanProperty showArrow = new SimpleBooleanProperty(this, "showArrow", true);

    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent", true);

    private final IntegerProperty showTimeout = new SimpleIntegerProperty(this, "showTimeout", 150);

    private final IntegerProperty hideTimeout = new SimpleIntegerProperty(this, "hideTimeout", 150);

    private final DoubleProperty maxMenuHeight = new SimpleDoubleProperty(this, "maxHeight", 0);

    private final StringProperty effectName = new SimpleStringProperty(this, "effect", "light");

    private final StringProperty role = new SimpleStringProperty(this, "role", "menu");

    private final StringProperty popperClass = new SimpleStringProperty(this, "popperClass", "");

    private final ObservableList<KeyCode> triggerKeys = FXCollections.observableArrayList(KeyCode.ENTER, KeyCode.SPACE,
            KeyCode.DOWN);

    private final ObservableList<EleFXDropdownItem> items = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<EleFXDropdownEvent>> onCommand = new SimpleObjectProperty<>(this,
            "onCommand");

    private final ObjectProperty<EventHandler<EleFXDropdownEvent>> onClick = new SimpleObjectProperty<>(this,
            "onClick");

    private final ObjectProperty<EventHandler<EleFXDropdownEvent>> onVisibleChange = new SimpleObjectProperty<>(this,
            "onVisibleChange");

    private final PopupControl popup = new PopupControl();

    private final StackPane popupRoot = new StackPane();

    private final VBox menu = new VBox();

    private final StackPane arrow = new StackPane();

    private final PauseTransition showTimer = new PauseTransition();

    private final PauseTransition hideTimer = new PauseTransition();

    private final HBox defaultTrigger = new HBox(4);

    private final Label defaultTriggerText = new Label();

    private Node popupAnchor;

    private boolean pointerInTrigger;

    public EleFXDropdown() {
        initialize();
    }

    public EleFXDropdown(EleFXDropdownItem... values) {
        this();
        if (values != null) items.addAll(values);
    }

    public String getText() {
        return text.get();
    }

    public void setText(String v) {
        text.set(v == null ? "" : v);
    }

    public StringProperty textProperty() {
        return text;
    }

    /** Content node that receives the trigger listeners. */
    public Node getTriggerNode() {
        return triggerNode.get();
    }

    public void setTriggerNode(Node v) {
        triggerNode.set(v);
    }

    public ObjectProperty<Node> triggerNodeProperty() {
        return triggerNode;
    }

    /**
     * Optional icon rendered after the default trigger text. This is ignored when a custom
     * {@link #getTriggerNode() trigger node} is supplied.
     */
    public Node getTriggerIcon() {
        return triggerIcon.get();
    }

    public void setTriggerIcon(Node value) {
        triggerIcon.set(value);
    }

    public ObjectProperty<Node> triggerIconProperty() {
        return triggerIcon;
    }

    /** Alternate node used as the popup anchor, analogous to Element Plus virtual-ref. */
    public Node getVirtualRef() {
        return virtualRef.get();
    }

    public void setVirtualRef(Node v) {
        virtualRef.set(v);
    }

    public ObjectProperty<Node> virtualRefProperty() {
        return virtualRef;
    }

    public EleFXDropdownTrigger getTrigger() {
        return trigger.get();
    }

    public void setTrigger(EleFXDropdownTrigger v) {
        trigger.set(v == null ? EleFXDropdownTrigger.HOVER : v);
    }

    public ObjectProperty<EleFXDropdownTrigger> triggerProperty() {
        return trigger;
    }

    public EleFXDropdownPlacement getPlacement() {
        return placement.get();
    }

    public void setPlacement(EleFXDropdownPlacement v) {
        placement.set(v == null ? EleFXDropdownPlacement.BOTTOM : v);
    }

    public ObjectProperty<EleFXDropdownPlacement> placementProperty() {
        return placement;
    }

    public EleFXButtonType getType() {
        return type.get();
    }

    public void setType(EleFXButtonType v) {
        type.set(v == null ? EleFXButtonType.DEFAULT : v);
    }

    public ObjectProperty<EleFXButtonType> typeProperty() {
        return type;
    }

    public EleFXDropdownSize getSize() {
        return size.get();
    }

    public void setSize(EleFXDropdownSize v) {
        size.set(v == null ? EleFXDropdownSize.DEFAULT : v);
    }

    public ObjectProperty<EleFXDropdownSize> sizeProperty() {
        return size;
    }

    public boolean isSplitButton() {
        return splitButton.get();
    }

    public void setSplitButton(boolean v) {
        splitButton.set(v);
    }

    public BooleanProperty splitButtonProperty() {
        return splitButton;
    }

    public boolean isHideOnClick() {
        return hideOnClick.get();
    }

    public void setHideOnClick(boolean v) {
        hideOnClick.set(v);
    }

    public BooleanProperty hideOnClickProperty() {
        return hideOnClick;
    }

    public boolean isShowArrow() {
        return showArrow.get();
    }

    public void setShowArrow(boolean v) {
        showArrow.set(v);
    }

    public BooleanProperty showArrowProperty() {
        return showArrow;
    }

    public boolean isPersistent() {
        return persistent.get();
    }

    public void setPersistent(boolean v) {
        persistent.set(v);
    }

    public BooleanProperty persistentProperty() {
        return persistent;
    }

    public int getShowTimeout() {
        return showTimeout.get();
    }

    public void setShowTimeout(int v) {
        positive(v, "showTimeout");
        showTimeout.set(v);
    }

    public IntegerProperty showTimeoutProperty() {
        return showTimeout;
    }

    public int getHideTimeout() {
        return hideTimeout.get();
    }

    public void setHideTimeout(int v) {
        positive(v, "hideTimeout");
        hideTimeout.set(v);
    }

    public IntegerProperty hideTimeoutProperty() {
        return hideTimeout;
    }

    public double getMaxMenuHeight() {
        return maxMenuHeight.get();
    }

    public void setMaxMenuHeight(double v) {
        if (!Double.isFinite(v) || v < 0) throw new IllegalArgumentException("maxHeight must be non-negative");
        maxMenuHeight.set(v);
    }

    public DoubleProperty maxMenuHeightProperty() {
        return maxMenuHeight;
    }

    public String getEffectName() {
        return effectName.get();
    }

    public void setEffectName(String v) {
        effectName.set(v == null || v.isBlank() ? "light" : v);
    }

    public StringProperty effectNameProperty() {
        return effectName;
    }

    /** Semantic menu role, exposed for accessibility/documentation (for example menu or navigation). */
    public String getRole() {
        return role.get();
    }

    public void setRole(String v) {
        role.set(v == null ? "menu" : v);
    }

    public StringProperty roleProperty() {
        return role;
    }

    public String getPopperClass() {
        return popperClass.get();
    }

    public void setPopperClass(String v) {
        popperClass.set(v == null ? "" : v.trim());
    }

    public StringProperty popperClassProperty() {
        return popperClass;
    }

    public ObservableList<KeyCode> getTriggerKeys() {
        return triggerKeys;
    }

    public ObservableList<EleFXDropdownItem> getItems() {
        return items;
    }

    public EventHandler<EleFXDropdownEvent> getOnCommand() {
        return onCommand.get();
    }

    public void setOnCommand(EventHandler<EleFXDropdownEvent> v) {
        onCommand.set(v);
    }

    public ObjectProperty<EventHandler<EleFXDropdownEvent>> onCommandProperty() {
        return onCommand;
    }

    public EventHandler<EleFXDropdownEvent> getOnClick() {
        return onClick.get();
    }

    public void setOnClick(EventHandler<EleFXDropdownEvent> v) {
        onClick.set(v);
    }

    public ObjectProperty<EventHandler<EleFXDropdownEvent>> onClickProperty() {
        return onClick;
    }

    public EventHandler<EleFXDropdownEvent> getOnVisibleChange() {
        return onVisibleChange.get();
    }

    public void setOnVisibleChange(EventHandler<EleFXDropdownEvent> v) {
        onVisibleChange.set(v);
    }

    public ObjectProperty<EventHandler<EleFXDropdownEvent>> onVisibleChangeProperty() {
        return onVisibleChange;
    }

    public boolean isShowing() {
        return popup.isShowing();
    }

    public void handleOpen() {
        open();
    }

    /**
     * Opens the menu with its top-left corner at the supplied screen coordinates. Use this for
     * pointer-positioned context menus; the normal {@link #handleOpen()} method follows the
     * configured placement relative to the trigger or virtual reference.
     */
    public void handleOpenAt(double screenX, double screenY) {
        if (isDisabled() || popup.isShowing() || items.isEmpty()) return;
        Node anchor = getVirtualRef() == null ? popupAnchor : getVirtualRef();
        if (anchor == null || anchor.getScene() == null) return;
        popupRoot.applyCss();
        popupRoot.layout();
        positionArrow(popupRoot.prefWidth(-1), 0);
        popup.show(anchor, screenX, screenY);
    }

    public void handleClose() {
        close();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.DROPDOWN;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-dropdown");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Region.USE_PREF_SIZE);
        defaultTrigger.getStyleClass().add("ele-dropdown__trigger");
        defaultTrigger.setAlignment(Pos.CENTER_LEFT);
        defaultTriggerText.getStyleClass().add("ele-dropdown__trigger-text");
        defaultTriggerText.textProperty().bind(text);
        menu.getStyleClass().add("ele-dropdown__menu");
        menu.setFocusTraversable(true);
        arrow.getStyleClass().add("ele-dropdown__arrow");
        arrow.setRotate(45);
        popupRoot.getStyleClass().add("ele-dropdown__popup");
        // The arrow overlays the menu edge so its background can mask the shared border.
        popupRoot.getChildren().addAll(menu, arrow);
        popupRoot.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        popupRoot.getStylesheets().add(EleFXThemes.DROPDOWN.toData());
        popup.getScene().setRoot(popupRoot);
        // Coordinates target the rendered menu, not the PopupWindow bounds (which may include
        // a platform inset or drop-shadow extent).
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        popup.setOnShown(e -> {
            visible(true);
            menu.requestFocus();
        });
        popup.setOnHidden(e -> visible(false));
        popup.setOnAutoHide(e -> {
            showTimer.stop();
            hideTimer.stop();
        });
        popupRoot.hoverProperty().addListener((observable, wasHovered, hovered) -> {
            if (hovered) {
                hideTimer.stop();
            } else if (getTrigger() == EleFXDropdownTrigger.HOVER && !pointerInTrigger) {
                delayedClose();
            }
        });
        showTimer.setOnFinished(e -> open());
        hideTimer.setOnFinished(e -> closeIfPointerOutside());
        triggerNode.addListener(o -> refreshTrigger());
        triggerIcon.addListener(o -> refreshTrigger());
        splitButton.addListener(o -> refreshTrigger());
        type.addListener(o -> refreshTrigger());
        size.addListener(o -> {
            refreshTrigger();
            refreshMenu();
            updateMenuClasses();
        });
        trigger.addListener(o -> updateAutoHide());
        placement.addListener(o -> updateMenuClasses());
        disabledProperty().addListener(o -> refreshTrigger());
        items.addListener((ListChangeListener<EleFXDropdownItem>) c -> refreshMenu());
        maxMenuHeight.addListener(o -> refreshMenu());
        effectName.addListener(o -> updateMenuClasses());
        popperClass.addListener(o -> updateMenuClasses());
        showArrow.addListener(o -> updateMenuClasses());
        showTimeout.addListener((o, a, b) -> showTimer.setDuration(Duration.millis(b.intValue())));
        hideTimeout.addListener((o, a, b) -> hideTimer.setDuration(Duration.millis(b.intValue())));
        showTimer.setDuration(Duration.millis(getShowTimeout()));
        hideTimer.setDuration(Duration.millis(getHideTimeout()));
        refreshTrigger();
        refreshMenu();
        updateAutoHide();
        updateMenuClasses();
        sceneBuilderIntegration();
    }

    private void refreshTrigger() {
        getChildren().clear();
        defaultTrigger.getChildren().setAll(defaultTriggerText);
        if (getTriggerIcon() != null) defaultTrigger.getChildren().add(getTriggerIcon());
        Node base = getTriggerNode() == null ? defaultTrigger : getTriggerNode();
        Node active;
        if (isSplitButton()) {
            EleFXButton main = new EleFXButton(getText());
            main.setType(getType());
            main.setSize(buttonSize());
            main.setDisable(isDisabled());
            main.getStyleClass().add("ele-dropdown__split-main");
            main.setOnAction(e -> fire(EleFXDropdownEvent.CLICK, null, null, false, getOnClick()));
            EleFXButton arrow = new EleFXButton("", new EleFXIcon(EleFXIconType.ARROW_DOWN, 12));
            arrow.setType(getType());
            arrow.setSize(buttonSize());
            arrow.setDisable(isDisabled());
            arrow.getStyleClass().add("ele-dropdown__split-caret");
            HBox group = new HBox(main, arrow);
            group.getStyleClass().add("ele-dropdown__split");
            group.setAlignment(Pos.CENTER_LEFT);
            active = group;
        } else
            active = base;
        getChildren().add(active);
        if (isSplitButton()) {
            HBox split = (HBox) active;
            // Element Plus uses the caret button alone as the split-dropdown trigger.
            popupAnchor = split.getChildren().get(1);
            attachTrigger(popupAnchor);
        } else {
            popupAnchor = active;
            attachTrigger(popupAnchor);
        }
    }

    private void attachTrigger(Node node) {
        node.setOnMouseEntered(e -> {
            pointerInTrigger = true;
            if (getTrigger() == EleFXDropdownTrigger.HOVER) delayedOpen();
        });
        node.setOnMouseExited(e -> {
            pointerInTrigger = false;
            if (getTrigger() == EleFXDropdownTrigger.HOVER) delayedClose();
        });
        node.setOnMouseClicked(e -> {
            if (getTrigger() == EleFXDropdownTrigger.CLICK) toggle();
        });
        node.setOnContextMenuRequested(e -> {
            if (getTrigger() == EleFXDropdownTrigger.CONTEXT_MENU) {
                // Context menu is only another opening gesture. Position it from the same
                // trigger/caret anchor and placement contract as hover and click menus.
                open();
                e.consume();
            }
        });
        node.setOnKeyPressed(this::keyPressed);
        node.setFocusTraversable(!isDisabled());
    }

    private void keyPressed(KeyEvent e) {
        if (triggerKeys.contains(e.getCode())) {
            toggle();
            e.consume();
        } else if (e.getCode() == KeyCode.ESCAPE) close();
    }

    private void refreshMenu() {
        menu.getChildren().clear();
        for (EleFXDropdownItem item : items) {
            if (item == null) throw new IllegalStateException("Dropdown items must not contain null");
            if (item.isDivided()) {
                StackPane divider = new StackPane();
                divider.getStyleClass().add("ele-dropdown__divider");
                menu.getChildren().add(divider);
                VBox.setMargin(divider, new Insets(dividerMargin(), 0, dividerMargin(), 0));
            }
            menu.getChildren().add(itemNode(item));
        }
        menu.setMaxHeight(getMaxMenuHeight() <= 0 ? Double.MAX_VALUE : getMaxMenuHeight());
    }

    private Node itemNode(EleFXDropdownItem item) {
        HBox row = new HBox(8);
        row.getStyleClass().add("ele-dropdown__item");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setDisable(item.isDisabled() || isDisabled());
        Node body = item.getContent() == null ? new Label(item.getText()) : item.getContent();
        if (item.getIcon() != null) row.getChildren().add(item.getIcon());
        row.getChildren().add(body);
        HBox.setHgrow(body, Priority.ALWAYS);
        row.setOnMouseClicked(e -> {
            if (!row.isDisabled()) command(item);
        });
        row.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                command(item);
                e.consume();
            }
        });
        row.setFocusTraversable(!row.isDisabled());
        return row;
    }

    private void command(EleFXDropdownItem item) {
        fire(EleFXDropdownEvent.COMMAND, item.getCommand(), item, true, getOnCommand());
        if (isHideOnClick()) close();
    }

    private void toggle() {
        if (popup.isShowing())
            close();
        else
            open();
    }

    private void delayedOpen() {
        hideTimer.stop();
        showTimer.playFromStart();
    }

    private void delayedClose() {
        showTimer.stop();
        hideTimer.playFromStart();
    }

    private void closeIfPointerOutside() {
        if (!pointerInTrigger && !popupRoot.isHover()) close();
    }

    private void open() {
        if (isDisabled() || popup.isShowing() || items.isEmpty()) return;
        Node anchor = getVirtualRef() == null ? popupAnchor : getVirtualRef();
        if (anchor == null || anchor.getScene() == null) return;
        // boundsInLocal includes visual effects such as a Card's drop shadow. Placement must
        // follow the control's layout edge instead, otherwise START/END shift by the shadow spread.
        Bounds b = anchor.localToScreen(anchor.getLayoutBounds());
        if (b == null) return;
        double[] xy = coordinates(b);
        popup.show(anchor, xy[0], xy[1]);
    }

    private double[] coordinates(Bounds b) {
        popupRoot.applyCss();
        popupRoot.layout();
        double popupWidth = popupRoot.prefWidth(-1);
        double popupHeight = popupRoot.prefHeight(popupWidth);
        positionArrow(popupWidth, b.getWidth());
        double x = switch (getPlacement()) {
            case TOP_START, BOTTOM_START -> b.getMinX();
            case TOP, BOTTOM -> b.getMinX() + (b.getWidth() - popupWidth) / 2;
            case TOP_END, BOTTOM_END -> b.getMaxX() - popupWidth;
        };
        double y = switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> b.getMinY() - popupHeight - POPUP_OFFSET;
            case BOTTOM_START, BOTTOM, BOTTOM_END -> b.getMaxY() + POPUP_OFFSET;
        };
        return new double[] {x, y};
    }

    private void close() {
        showTimer.stop();
        hideTimer.stop();
        popup.hide();
    }

    private void visible(boolean value) {
        getStyleClass().remove("ele-dropdown--open");
        if (value) getStyleClass().add("ele-dropdown--open");
        fire(EleFXDropdownEvent.VISIBLE_CHANGE, null, null, value, getOnVisibleChange());
    }

    private void updateMenuClasses() {
        menu.getStyleClass().removeIf(s -> s.startsWith("ele-dropdown__menu--") || s.equals(""));
        menu.getStyleClass().add("ele-dropdown__menu--" + getEffectName());
        menu.getStyleClass().add("ele-dropdown__menu--size-" + getSize().name().toLowerCase());
        popupRoot.getStyleClass().removeIf(s -> s.startsWith("ele-dropdown__popup--"));
        popupRoot.getStyleClass().add("ele-dropdown__popup--" + getEffectName());
        if (isShowArrow()) menu.getStyleClass().add("ele-dropdown__menu--arrow");
        if (!getPopperClass().isBlank()) menu.getStyleClass().add(getPopperClass());
        arrow.setVisible(isShowArrow());
        arrow.getStyleClass().removeIf(s -> s.startsWith("ele-dropdown__arrow--"));
        arrow.getStyleClass().add("ele-dropdown__arrow--" + arrowSide());
        positionArrow(popupRoot.prefWidth(-1), 0);
    }

    private void updateAutoHide() {
        // PopupWindow auto-hide is focus-based. For hover menus, the explicit pointer enter/exit
        // handlers are the source of truth so the pointer can travel from trigger into the popup.
        popup.setAutoHide(getTrigger() != EleFXDropdownTrigger.HOVER);
    }

    private String arrowSide() {
        return switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> "bottom";
            case BOTTOM_START, BOTTOM, BOTTOM_END -> "top";
        };
    }

    private void positionArrow(double popupWidth, double anchorWidth) {
        double arrowCenterX = switch (getPlacement()) {
            case TOP_START, BOTTOM_START -> anchorWidth > 0 ? anchorWidth / 2 : 12;
            case TOP, BOTTOM -> popupWidth / 2;
            case TOP_END, BOTTOM_END -> anchorWidth > 0 ? popupWidth - anchorWidth / 2 : popupWidth - 12;
        };
        arrow.setTranslateX(arrowCenterX - 5);
        arrow.setTranslateY(0);
        switch (arrowSide()) {
            case "top" -> {
                StackPane.setAlignment(arrow, Pos.TOP_LEFT);
                arrow.setTranslateY(-5);
            }
            case "bottom" -> {
                StackPane.setAlignment(arrow, Pos.BOTTOM_LEFT);
                arrow.setTranslateY(5);
            }
            default -> throw new IllegalStateException("Unknown dropdown arrow side");
        }
    }

    private EleFXButtonSize buttonSize() {
        return switch (getSize()) {
            case LARGE -> EleFXButtonSize.LARGE;
            case DEFAULT -> EleFXButtonSize.DEFAULT;
            case SMALL -> EleFXButtonSize.SMALL;
        };
    }

    private double dividerMargin() {
        return switch (getSize()) {
            case LARGE -> 8;
            case DEFAULT -> 6;
            case SMALL -> 4;
        };
    }

    private void fire(javafx.event.EventType<EleFXDropdownEvent> type, Object command, EleFXDropdownItem item,
                      boolean visible, EventHandler<EleFXDropdownEvent> handler) {
        EleFXDropdownEvent event = new EleFXDropdownEvent(this, this, type, command, item, visible);
        fireEvent(event);
        if (!event.isConsumed() && handler != null) handler.handle(event);
    }

    private static void positive(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " must not be negative");
    }
}
