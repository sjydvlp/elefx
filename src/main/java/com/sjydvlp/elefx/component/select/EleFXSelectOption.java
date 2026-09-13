package com.sjydvlp.elefx.component.select;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;

/** A labelled value offered by an {@link EleFXSelect}. */
public final class EleFXSelectOption<T> {

    private final T value;

    private final String label;

    private final BooleanProperty disabled;

    public EleFXSelectOption(T value, String label) {
        this(value, label, false);
    }

    public EleFXSelectOption(T value, String label, boolean disabled) {
        this.value = value;
        this.label = label == null ? "" : label;
        this.disabled = new SimpleBooleanProperty(this, "disabled", disabled);
    }

    public T getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof EleFXSelectOption<?> other && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
