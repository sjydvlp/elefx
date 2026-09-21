package com.sjydvlp.elefx.component.menu;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

/** Expandable menu entry. It renders inline in an expanded vertical menu and as a popup otherwise. */
public class EleFXSubMenu extends VBox implements EleFXMenuEntry {

    private final StringProperty index = new SimpleStringProperty(this, "index", "");

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    private final DoubleProperty popperOffset = new SimpleDoubleProperty(this, "popperOffset", Double.NaN);

    private final ObservableList<EleFXMenuEntry> items = FXCollections.observableArrayList();

    private final HBox header = new HBox();

    private final Label label = new Label();

    private final Label arrow = new Label("›");

    private final VBox content = new VBox();

    private final Popup popup = new Popup();

    private final PauseTransition hideDelay = new PauseTransition();

    EleFXMenu menu;

    EleFXSubMenu parent;

    public EleFXSubMenu() {
        initialize();
    }

    public EleFXSubMenu(String index, String text, EleFXMenuEntry... entries) {
        this();
        setIndex(index);
        setText(text);
        getItems().addAll(entries);
    }

    public ObservableList<EleFXMenuEntry> getItems() {
        return items;
    }

    public String getIndex() {
        return index.get();
    }

    public void setIndex(String value) {
        index.set(value == null ? "" : value);
    }

    public StringProperty indexProperty() {
        return index;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    public StringProperty textProperty() {
        return text;
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public Node getIcon() {
        return icon.get();
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public boolean isExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public double getPopperOffset() {
        return popperOffset.get();
    }

    public void setPopperOffset(double value) {
        popperOffset.set(value);
    }

    public DoubleProperty popperOffsetProperty() {
        return popperOffset;
    }

    @Override
    public Node node() {
        return this;
    }

    void install(EleFXMenu owner) {
        menu = owner;
        for (EleFXMenuEntry entry : items)
            installEntry(entry);
        refresh();
    }

    private void initialize() {
        getStyleClass().add("ele-sub-menu");
        header.getStyleClass().add("ele-sub-menu__title");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setFocusTraversable(true);
        label.getStyleClass().add("ele-sub-menu__label");
        HBox.setHgrow(label, Priority.ALWAYS);
        arrow.getStyleClass().add("ele-sub-menu__arrow");
        content.getStyleClass().add("ele-menu--popup");
        popup.getContent().add(content);
        popup.setAutoHide(false);
        getChildren().addAll(header, content);
        header.setOnMouseClicked(e -> toggle());
        header.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                toggle();
                e.consume();
            }
        });
        header.setOnMouseEntered(e -> {
            if (menu != null && menu.usesHover()) open();
        });
        header.setOnMouseExited(e -> scheduleHide());
        popup.getContent().get(0).setOnMouseEntered(e -> hideDelay.stop());
        popup.getContent().get(0).setOnMouseExited(e -> scheduleHide());
        hideDelay.setOnFinished(e -> close());
        items.addListener((javafx.collections.ListChangeListener<EleFXMenuEntry>) c -> refresh());
        text.addListener(o -> refresh());
        titleNode.addListener(o -> refresh());
        icon.addListener(o -> refresh());
        expanded.addListener(o -> refreshState());
        disableProperty().addListener(o -> refreshState());
        refresh();
    }

    void open() {
        if (isDisabled() || isExpanded()) return;
        if (menu != null) menu.open(this);
    }

    void close() {
        if (!isExpanded()) return;
        if (menu != null) menu.close(this);
    }

    private void toggle() {
        if (!isDisabled()) {
            if (isExpanded())
                close();
            else
                open();
        }
    }

    void setExpandedFromMenu(boolean value) {
        expanded.set(value);
        boolean inline = menu != null && menu.getMode() == EleFXMenuMode.VERTICAL && !menu.isCollapse();
        content.setVisible(inline && value);
        content.setManaged(inline && value);
        if (!inline) {
            if (value)
                showPopup();
            else
                popup.hide();
        }
        refreshState();
    }

    private void showPopup() {
        if (getScene() == null || popup.isShowing()) return;
        double offset = Double.isNaN(getPopperOffset()) ? menu.getPopperOffset() : getPopperOffset();
        javafx.geometry.Bounds b = header.localToScreen(header.getBoundsInLocal());
        if (b == null) return;
        boolean horizontal = menu.getMode() == EleFXMenuMode.HORIZONTAL;
        popup.show(header, horizontal ? b.getMinX() : b.getMaxX() + offset,
                horizontal ? b.getMaxY() + offset : b.getMinY());
    }

    private void scheduleHide() {
        if (menu != null && menu.usesHover()) {
            hideDelay.setDuration(menu.getHideTimeout());
            hideDelay.playFromStart();
        }
    }

    private void refresh() {
        header.getChildren().clear();
        if (getIcon() != null) {
            getIcon().getStyleClass().add("ele-sub-menu__icon");
            header.getChildren().add(getIcon());
        }
        label.setText(getText());
        header.getChildren().add(getTitleNode() == null ? label : getTitleNode());
        header.getChildren().add(arrow);
        content.getChildren().clear();
        for (EleFXMenuEntry e : items) {
            installEntry(e);
            content.getChildren().add(e.node());
        }
        refreshState();
    }

    private void installEntry(EleFXMenuEntry e) {
        if (e instanceof EleFXMenuItem item)
            item.menu = menu;
        else if (e instanceof EleFXSubMenu sub) {
            sub.parent = this;
            sub.install(menu);
        } else if (e instanceof EleFXMenuItemGroup group) group.install(menu);
    }

    private void refreshState() {
        getStyleClass().removeAll("ele-sub-menu--open", "ele-sub-menu--disabled", "ele-sub-menu--collapsed");
        if (isExpanded()) getStyleClass().add("ele-sub-menu--open");
        if (isDisabled()) getStyleClass().add("ele-sub-menu--disabled");
        if (menu != null && menu.isCollapse()) getStyleClass().add("ele-sub-menu--collapsed");
        arrow.setText(isExpanded() ? "⌄" : "›");
    }
}
