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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Element Plus-inspired dropdown with hover, click, context-menu, and split-button modes. */
public class EleFXDropdown extends StackPane implements Themable {

    private final StringProperty text = new SimpleStringProperty(this, "text", "Dropdown List");

    private final ObjectProperty<Node> triggerNode = new SimpleObjectProperty<>(this, "triggerNode");

    private final ObjectProperty<Node> virtualRef = new SimpleObjectProperty<>(this, "virtualRef");

    private final ObjectProperty<EleFXDropdownTrigger> trigger = new SimpleObjectProperty<>(this, "trigger",
            EleFXDropdownTrigger.HOVER);

    private final ObjectProperty<EleFXDropdownPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXDropdownPlacement.BOTTOM);

    private final ObjectProperty<EleFXButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFXButtonType.DEFAULT);

    private final ObjectProperty<EleFXButtonSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXButtonSize.DEFAULT);

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

    private final VBox menu = new VBox();

    private final PauseTransition showTimer = new PauseTransition();

    private final PauseTransition hideTimer = new PauseTransition();

    private final Label defaultTrigger = new Label();

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

    public EleFXButtonSize getSize() {
        return size.get();
    }

    public void setSize(EleFXButtonSize v) {
        size.set(v == null ? EleFXButtonSize.DEFAULT : v);
    }

    public ObjectProperty<EleFXButtonSize> sizeProperty() {
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
        defaultTrigger.getStyleClass().add("ele-dropdown__trigger");
        defaultTrigger.textProperty().bind(text);
        menu.getStyleClass().add("ele-dropdown__menu");
        menu.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        menu.getStylesheets().add(EleFXThemes.DROPDOWN.toData());
        popup.getScene().setRoot(menu);
        popup.setAutoHide(true);
        popup.setOnShown(e -> visible(true));
        popup.setOnHidden(e -> visible(false));
        popup.setOnAutoHide(e -> {
            showTimer.stop();
            hideTimer.stop();
        });
        menu.setOnMouseEntered(e -> hideTimer.stop());
        menu.setOnMouseExited(e -> {
            if (getTrigger() == EleFXDropdownTrigger.HOVER && !pointerInTrigger) delayedClose();
        });
        showTimer.setOnFinished(e -> open());
        hideTimer.setOnFinished(e -> close());
        triggerNode.addListener(o -> refreshTrigger());
        splitButton.addListener(o -> refreshTrigger());
        type.addListener(o -> refreshTrigger());
        size.addListener(o -> refreshTrigger());
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
        updateMenuClasses();
        sceneBuilderIntegration();
    }

    private void refreshTrigger() {
        getChildren().clear();
        Node base = getTriggerNode() == null ? defaultTrigger : getTriggerNode();
        Node active;
        if (isSplitButton()) {
            EleFXButton main = new EleFXButton(getText());
            main.setType(getType());
            main.setSize(getSize());
            main.setDisable(isDisabled());
            main.setOnAction(e -> fire(EleFXDropdownEvent.CLICK, null, null, false, getOnClick()));
            EleFXButton arrow = new EleFXButton("", new EleFXIcon(EleFXIconType.ARROW_DOWN, 12));
            arrow.setType(getType());
            arrow.setSize(getSize());
            arrow.setDisable(isDisabled());
            arrow.setOnAction(e -> toggle());
            HBox group = new HBox(main, arrow);
            group.getStyleClass().add("ele-dropdown__split");
            active = group;
        } else
            active = base;
        getChildren().add(active);
        attachTrigger(active);
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
                openAt(e.getScreenX(), e.getScreenY());
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

    private void open() {
        if (isDisabled() || popup.isShowing() || items.isEmpty()) return;
        Node anchor = getVirtualRef() == null
                ? (getChildren().isEmpty() ? null : getChildren().get(0))
                : getVirtualRef();
        if (anchor == null || anchor.getScene() == null) return;
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b == null) return;
        double[] xy = coordinates(b);
        popup.show(anchor, xy[0], xy[1]);
    }

    private void openAt(double x, double y) {
        if (!isDisabled() && !items.isEmpty() && !getChildren().isEmpty()) popup.show(getChildren().get(0), x, y);
    }

    private double[] coordinates(Bounds b) {
        double x = switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> b.getMinX();
            case BOTTOM_START, BOTTOM, BOTTOM_END -> b.getMinX();
            case LEFT_START, LEFT, LEFT_END -> b.getMinX() - 4;
            case RIGHT_START, RIGHT, RIGHT_END -> b.getMaxX() + 4;
        };
        double y = switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> b.getMinY() - 4;
            case BOTTOM_START, BOTTOM, BOTTOM_END -> b.getMaxY() + 4;
            case LEFT_START, LEFT, LEFT_END, RIGHT_START, RIGHT, RIGHT_END -> b.getMinY();
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
        if (isShowArrow()) menu.getStyleClass().add("ele-dropdown__menu--arrow");
        if (!getPopperClass().isBlank()) menu.getStyleClass().add(getPopperClass());
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
