package com.sjydvlp.elefx.component.switcher;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted when an {@link EleFXSwitch}'s value changes. */
public class EleFXSwitchEvent extends Event {

    public static final EventType<EleFXSwitchEvent> VALUE_CHANGED = new EventType<>(Event.ANY,
            "ELEFX_SWITCH_VALUE_CHANGED");

    private final Object oldValue;

    private final Object value;

    public EleFXSwitchEvent(Object source, EventTarget target, Object oldValue, Object value) {
        super(source, target, VALUE_CHANGED);
        this.oldValue = oldValue;
        this.value = value;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getValue() {
        return value;
    }
}
