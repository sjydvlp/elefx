package com.sjydvlp.elefx.component.menu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.List;

/** A selectable leaf in an {@link EleFXMenu}. */
public class EleFXMenuItem extends HBox implements EleFXMenuEntry {

    private final StringProperty index = new SimpleStringProperty(this, "index", "");

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Object> route = new SimpleObjectProperty<>(this, "route");

    private final Label label = new Label();

    EleFXMenu menu;

    public EleFXMenuItem() {
        initialize();
    }

    public EleFXMenuItem(String index, String text) {
        this();
        setIndex(index);
        setText(text);
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

    public Object getRoute() {
        return route.get();
    }

    public void setRoute(Object value) {
        route.set(value);
    }

    public ObjectProperty<Object> routeProperty() {
        return route;
    }

    @Override
    public Node node() {
        return this;
    }

    List<String> indexPath() {
        return menu == null ? List.of(getIndex()) : menu.pathFor(this);
    }

    void refresh() {
        getStyleClass().removeAll("ele-menu-item--active", "ele-menu-item--collapsed");
        if (menu != null && getIndex().equals(menu.getActiveIndex())) getStyleClass().add("ele-menu-item--active");
        if (menu != null && menu.isCollapse() && menu.getMode() == EleFXMenuMode.VERTICAL)
            getStyleClass().add("ele-menu-item--collapsed");
    }

    private void initialize() {
        getStyleClass().add("ele-menu-item");
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        label.getStyleClass().add("ele-menu-item__label");
        HBox.setHgrow(label, Priority.ALWAYS);
        getChildren().add(label);
        text.addListener(o -> refreshContent());
        titleNode.addListener(o -> refreshContent());
        icon.addListener(o -> refreshContent());
        setOnMouseClicked(e -> activate());
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                activate();
                e.consume();
            }
        });
        disableProperty().addListener(o -> refresh());
        refreshContent();
    }

    private void refreshContent() {
        getChildren().clear();
        if (getIcon() != null) {
            getIcon().getStyleClass().add("ele-menu-item__icon");
            getChildren().add(getIcon());
        }
        label.setText(getText());
        getChildren().add(getTitleNode() == null ? label : getTitleNode());
    }

    private void activate() {
        if (!isDisabled() && menu != null) menu.select(this);
    }
}
