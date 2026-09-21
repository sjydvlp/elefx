package com.sjydvlp.elefx.component.menu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** A titled non-selectable group of menu items. */
public class EleFXMenuItemGroup extends VBox implements EleFXMenuEntry {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObservableList<EleFXMenuEntry> items = FXCollections.observableArrayList();

    private final Label titleLabel = new Label();

    EleFXMenu menu;

    public EleFXMenuItemGroup() {
        getStyleClass().add("ele-menu-item-group");
        titleLabel.getStyleClass().add("ele-menu-item-group__title");
        title.addListener(o -> refresh());
        titleNode.addListener(o -> refresh());
        items.addListener((javafx.collections.ListChangeListener<EleFXMenuEntry>) c -> refresh());
        refresh();
    }

    public EleFXMenuItemGroup(String title, EleFXMenuEntry... entries) {
        this();
        setTitle(title);
        getItems().addAll(entries);
    }

    public ObservableList<EleFXMenuEntry> getItems() {
        return items;
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

    public Node getTitleNode() {
        return titleNode.get();
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    @Override
    public Node node() {
        return this;
    }

    void install(EleFXMenu owner) {
        menu = owner;
        refresh();
    }

    private void refresh() {
        getChildren().clear();
        if (getTitleNode() != null || !getTitle().isBlank()) {
            titleLabel.setText(getTitle());
            getChildren().add(getTitleNode() == null ? titleLabel : getTitleNode());
        }
        for (EleFXMenuEntry entry : items) {
            install(entry);
            getChildren().add(entry.node());
        }
    }

    private void install(EleFXMenuEntry entry) {
        if (entry instanceof EleFXMenuItem item)
            item.menu = menu;
        else if (entry instanceof EleFXSubMenu submenu)
            submenu.install(menu);
        else if (entry instanceof EleFXMenuItemGroup group) group.install(menu);
    }
}
