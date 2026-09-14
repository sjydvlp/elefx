package com.sjydvlp.elefx.component.switcher;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted when an {@link EleFXSwitch}'s value changes. */
public class EleFXSwitchEvent extends Event {

    public static final EventType<EleFXSwitchEvent> VALUE_CHANGED = new EventType<>(Event.ANY,
            "ELEFX_SWITCH_VALUE_CHANGED");

    private final boolean oldValue;

    private final boolean value;

    public EleFXSwitchEvent(Object source, EventTarget target, boolean oldValue, boolean value) {
        super(source, target, VALUE_CHANGED);
        this.oldValue = oldValue;
        this.value = value;
    }

    public boolean getOldValue() {
        return oldValue;
    }

    public boolean getValue() {
        return value;
    }
}
