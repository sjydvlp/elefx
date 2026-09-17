package com.sjydvlp.elefx.component.pagination;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted after a pagination value changes. */
public final class EleFXPaginationEvent extends Event {

    public static final EventType<EleFXPaginationEvent> CURRENT_CHANGE = new EventType<>(Event.ANY,
            "ELEFX_PAGINATION_CURRENT_CHANGE");

    public static final EventType<EleFXPaginationEvent> SIZE_CHANGE = new EventType<>(Event.ANY,
            "ELEFX_PAGINATION_SIZE_CHANGE");

    public static final EventType<EleFXPaginationEvent> PREV_CLICK = new EventType<>(Event.ANY,
            "ELEFX_PAGINATION_PREV_CLICK");

    public static final EventType<EleFXPaginationEvent> NEXT_CLICK = new EventType<>(Event.ANY,
            "ELEFX_PAGINATION_NEXT_CLICK");

    private final int oldValue;

    private final int value;

    public EleFXPaginationEvent(Object source, EventTarget target, EventType<? extends EleFXPaginationEvent> type,
            int oldValue, int value) {
        super(source, target, type);
        this.oldValue = oldValue;
        this.value = value;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getValue() {
        return value;
    }
}
