package com.sjydvlp.elefx.component.transfer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/** A keyed item displayed by {@link EleFXTransfer}. */
public final class EleFXTransferItem<T> {

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<String> label = new SimpleObjectProperty<>(this, "label", "");

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    public EleFXTransferItem(T value, String label) {
        this(value, label, false);
    }

    public EleFXTransferItem(T value, String label, boolean disabled) {
        setValue(value);
        setLabel(label);
        setDisabled(disabled);
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String value) {
        label.set(value == null ? "" : value);
    }

    public ObjectProperty<String> labelProperty() {
        return label;
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
}
