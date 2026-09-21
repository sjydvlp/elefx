package com.sjydvlp.elefx.component.dropdown;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/** A selectable item in an {@link EleFXDropdown}. */
public final class EleFXDropdownItem {

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Object> command = new SimpleObjectProperty<>(this, "command");

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    private final BooleanProperty divided = new SimpleBooleanProperty(this, "divided", false);

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public EleFXDropdownItem() {
        this("");
    }

    public EleFXDropdownItem(String text) {
        setText(text);
    }

    public EleFXDropdownItem(String text, Object command) {
        this(text);
        setCommand(command);
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

    public Object getCommand() {
        return command.get();
    }

    public void setCommand(Object value) {
        command.set(value);
    }

    public ObjectProperty<Object> commandProperty() {
        return command;
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

    public boolean isDivided() {
        return divided.get();
    }

    public void setDivided(boolean value) {
        divided.set(value);
    }

    public BooleanProperty dividedProperty() {
        return divided;
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

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }
}
