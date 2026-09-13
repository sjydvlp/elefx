package com.sjydvlp.elefx.component.rate;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted after a user action changes an {@link EleFXRate}'s value. */
public class EleFXRateEvent extends Event {

    public static final EventType<EleFXRateEvent> VALUE_CHANGED = new EventType<>(Event.ANY,
            "ELEFX_RATE_VALUE_CHANGED");

    private final double oldValue;

    private final double value;

    public EleFXRateEvent(Object source, EventTarget target, double oldValue, double value) {
        super(source, target, VALUE_CHANGED);
        this.oldValue = oldValue;
        this.value = value;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getValue() {
        return value;
    }
}
