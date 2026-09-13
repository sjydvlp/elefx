package com.sjydvlp.elefx.component.inputnumber;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event fired after an input number value has changed. */
public class EleFXInputNumberEvent extends Event {

    public static final EventType<EleFXInputNumberEvent> CHANGE = new EventType<>(Event.ANY,
            "ELEFX_INPUT_NUMBER_CHANGE");

    private final Double oldValue;

    private final Double value;

    public EleFXInputNumberEvent(Object source, EventTarget target, Double oldValue, Double value) {
        super(source, target, CHANGE);
        this.oldValue = oldValue;
        this.value = value;
    }

    public Double getOldValue() {
        return oldValue;
    }

    public Double getValue() {
        return value;
    }
}
