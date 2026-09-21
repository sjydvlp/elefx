package com.sjydvlp.elefx.component.dropdown;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Command, primary-button, or visibility event emitted by {@link EleFXDropdown}. */
public final class EleFXDropdownEvent extends Event {

    public static final EventType<EleFXDropdownEvent> COMMAND = new EventType<>(Event.ANY, "ELEFX_DROPDOWN_COMMAND");

    public static final EventType<EleFXDropdownEvent> CLICK = new EventType<>(Event.ANY, "ELEFX_DROPDOWN_CLICK");

    public static final EventType<EleFXDropdownEvent> VISIBLE_CHANGE = new EventType<>(Event.ANY,
            "ELEFX_DROPDOWN_VISIBLE_CHANGE");

    private final Object command;

    private final EleFXDropdownItem item;

    private final boolean visible;

    EleFXDropdownEvent(Object source, EventTarget target, EventType<EleFXDropdownEvent> type,
            Object command, EleFXDropdownItem item, boolean visible) {
        super(source, target, type);
        this.command = command;
        this.item = item;
        this.visible = visible;
    }

    public Object getCommand() {
        return command;
    }

    public EleFXDropdownItem getItem() {
        return item;
    }

    public boolean isVisible() {
        return visible;
    }
}
