package com.sjydvlp.elefx.component.breadcrumb;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

/** One level in an {@link EleFXBreadcrumb}. */
public class EleFXBreadcrumbItem extends HBox {

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final StringProperty to = new SimpleStringProperty(this, "to", "");

    private final BooleanProperty replace = new SimpleBooleanProperty(this, "replace", false);

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final Label textLabel = new Label();

    private EleFXBreadcrumb owner;

    private boolean last;

    public EleFXBreadcrumbItem() {
        this("");
    }

    public EleFXBreadcrumbItem(String text) {
        getStyleClass().add("ele-breadcrumb__item");
        textLabel.getStyleClass().add("ele-breadcrumb__inner");
        textLabel.textProperty().bind(this.text);
        setAccessibleRole(AccessibleRole.HYPERLINK);
        setFocusTraversable(false);
        this.text.addListener((o, oldValue, newValue) -> setAccessibleText(newValue));
        content.addListener((o, oldValue, newValue) -> refreshContent());
        to.addListener(o -> updateInteractivity());
        disabledProperty().addListener(o -> updateInteractivity());
        setOnMouseClicked(event -> activate());
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                activate();
                event.consume();
            }
        });
        setText(text);
        refreshContent();
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

    /** Target route/path. An empty value leaves the item as display-only. */
    public String getTo() {
        return to.get();
    }

    public void setTo(String value) {
        to.set(value == null ? "" : value);
    }

    public StringProperty toProperty() {
        return to;
    }

    public boolean isReplace() {
        return replace.get();
    }

    public void setReplace(boolean value) {
        replace.set(value);
    }

    public BooleanProperty replaceProperty() {
        return replace;
    }

    /** Optional custom content shown in place of the text label. */
    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    void setOwner(EleFXBreadcrumb value) {
        owner = value;
    }

    void setLast(boolean value) {
        last = value;
        getStyleClass().remove("ele-breadcrumb__item--last");
        if (value) getStyleClass().add("ele-breadcrumb__item--last");
        updateInteractivity();
    }

    private void refreshContent() {
        getChildren().setAll(getContent() == null ? textLabel : getContent());
    }

    private void activate() {
        if (owner != null && !last && !getTo().isBlank() && !isDisabled()) owner.navigate(this);
    }

    private void updateInteractivity() {
        boolean navigable = !last && !getTo().isBlank() && !isDisabled();
        setFocusTraversable(navigable);
        getStyleClass().remove("ele-breadcrumb__item--navigable");
        if (navigable) getStyleClass().add("ele-breadcrumb__item--navigable");
    }
}
