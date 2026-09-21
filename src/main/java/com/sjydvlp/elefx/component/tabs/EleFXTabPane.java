package com.sjydvlp.elefx.component.tabs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/** A page and its navigation metadata for {@link EleFXTabs}. */
public class EleFXTabPane {

    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    private final ObjectProperty<Object> name = new SimpleObjectProperty<>(this, "name");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final ObjectProperty<Node> labelGraphic = new SimpleObjectProperty<>(this, "labelGraphic");

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    private final BooleanProperty closable = new SimpleBooleanProperty(this, "closable", false);

    private final BooleanProperty lazy = new SimpleBooleanProperty(this, "lazy", false);

    public EleFXTabPane() {
    }

    public EleFXTabPane(String label, Node content) {
        setLabel(label);
        setContent(content);
    }

    public EleFXTabPane(String label, Object name, Node content) {
        this(label, content);
        setName(name);
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String value) {
        label.set(value == null ? "" : value);
    }

    public StringProperty labelProperty() {
        return label;
    }

    public Object getName() {
        return name.get();
    }

    public void setName(Object value) {
        name.set(value);
    }

    public ObjectProperty<Object> nameProperty() {
        return name;
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getLabelGraphic() {
        return labelGraphic.get();
    }

    public void setLabelGraphic(Node value) {
        labelGraphic.set(value);
    }

    public ObjectProperty<Node> labelGraphicProperty() {
        return labelGraphic;
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

    public boolean isClosable() {
        return closable.get();
    }

    public void setClosable(boolean value) {
        closable.set(value);
    }

    public BooleanProperty closableProperty() {
        return closable;
    }

    public boolean isLazy() {
        return lazy.get();
    }

    public void setLazy(boolean value) {
        lazy.set(value);
    }

    public BooleanProperty lazyProperty() {
        return lazy;
    }
}
