package com.sjydvlp.elefx.component.checkbox;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

/** Common selection contract used by checkbox and checkbox-button groups. */
public interface EleFXCheckable<T> {

    T getValue();

    ObjectProperty<T> valueProperty();

    boolean isSelected();

    BooleanProperty selectedProperty();

    void setSelected(boolean selected);

    boolean isDisable();

    void setDisable(boolean disabled);

    ObservableList<String> getStyleClass();
}
