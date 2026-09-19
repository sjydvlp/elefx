package com.sjydvlp.elefx.component.segmented;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/** One selectable option in an {@link EleFXSegmented} control. */
public class EleFXSegmentedItem<T> {

    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public EleFXSegmentedItem() {
    }

    public EleFXSegmentedItem(String label, T value) {
        setLabel(label);
        setValue(value);
    }

    public EleFXSegmentedItem(T value) {
        this(value == null ? "" : String.valueOf(value), value);
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String label) {
        this.label.set(label == null ? "" : label);
    }

    public StringProperty labelProperty() {
        return label;
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

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public Node getGraphic() {
        return graphic.get();
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }
}
