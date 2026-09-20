package com.sjydvlp.elefx.component.anchor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/** A navigable item in an {@link EleFXAnchor}. */
public class EleFXAnchorLink extends VBox {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty href = new SimpleStringProperty(this, "href", "");

    private final ObjectProperty<Node> target = new SimpleObjectProperty<>(this, "target");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final ObservableList<EleFXAnchorLink> subLinks = FXCollections.observableArrayList();

    private final Label titleLabel = new Label();

    private final VBox childrenBox = new VBox();

    private EleFXAnchor owner;

    public EleFXAnchorLink() {
        this("", "");
    }

    public EleFXAnchorLink(String title, String href) {
        getStyleClass().add("ele-anchor__link");
        titleLabel.getStyleClass().add("ele-anchor__link-title");
        titleLabel.textProperty().bind(this.title);
        titleLabel.setFocusTraversable(true);
        titleLabel.setOnMouseClicked(event -> {
            if (owner != null) owner.activate(this);
        });
        titleLabel.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER, SPACE -> {
                    if (owner != null) owner.activate(this);
                    event.consume();
                }
                default -> {
                }
            }
        });
        childrenBox.getStyleClass().add("ele-anchor__sub-links");
        getChildren().addAll(titleLabel, childrenBox);
        setTitle(title);
        setHref(href);
        content.addListener((o, oldValue, newValue) -> refreshContent());
        subLinks.addListener((ListChangeListener<EleFXAnchorLink>) change -> refreshChildren());
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

    public String getHref() {
        return href.get();
    }

    public void setHref(String value) {
        href.set(value == null ? "" : value);
    }

    public StringProperty hrefProperty() {
        return href;
    }

    /** Optional concrete target. When absent, Anchor resolves {@code href} as a scene node id. */
    public Node getTarget() {
        return target.get();
    }

    public void setTarget(Node value) {
        target.set(value);
    }

    public ObjectProperty<Node> targetProperty() {
        return target;
    }

    /** Optional custom node shown instead of the title label. */
    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    /** Nested links; ignored visually in horizontal Anchor mode. */
    public ObservableList<EleFXAnchorLink> getSubLinks() {
        return subLinks;
    }

    void setOwner(EleFXAnchor value) {
        owner = value;
        for (EleFXAnchorLink link : subLinks)
            link.setOwner(value);
    }

    void setActive(boolean value) {
        getStyleClass().remove("ele-anchor__link--active");
        if (value) getStyleClass().add("ele-anchor__link--active");
    }

    Label titleLabel() {
        return titleLabel;
    }

    private void refreshContent() {
        Node replacement = getContent();
        getChildren().removeIf(node -> node != childrenBox);
        if (replacement == null)
            getChildren().add(0, titleLabel);
        else {
            replacement.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (owner != null) owner.activate(this);
            });
            getChildren().add(0, replacement);
        }
    }

    private void refreshChildren() {
        childrenBox.getChildren().setAll(subLinks);
        for (EleFXAnchorLink link : subLinks)
            link.setOwner(owner);
        if (owner != null) owner.linksChanged();
    }
}
