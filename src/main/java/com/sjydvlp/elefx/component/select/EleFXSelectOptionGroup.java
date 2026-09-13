package com.sjydvlp.elefx.component.select;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/** A labelled, optionally disabled group of select options. */
public final class EleFXSelectOptionGroup<T> {

    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    private final ObservableList<EleFXSelectOption<T>> options = FXCollections.observableArrayList(
            option -> new Observable[] {option.disabledProperty()});

    public EleFXSelectOptionGroup(String label) {
        setLabel(label);
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

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean value) {
        disabled.set(value);
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public ObservableList<EleFXSelectOption<T>> getOptions() {
        return options;
    }

    /**
     * Adds options without exposing JavaFX's unchecked generic-varargs warning.
     *
     * @return whether the group changed
     */
    @SafeVarargs
    public final boolean addOptions(EleFXSelectOption<T>... values) {
        return options.addAll(List.of(values));
    }
}
