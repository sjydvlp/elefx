package com.sjydvlp.elefx.component.popover;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Lifecycle event emitted by an {@link EleFXPopover}. */
public final class EleFXPopoverEvent extends Event {

    public static final EventType<EleFXPopoverEvent> SHOW = new EventType<>(Event.ANY, "ELEFX_POPOVER_SHOW");

    public static final EventType<EleFXPopoverEvent> BEFORE_ENTER = new EventType<>(Event.ANY,
            "ELEFX_POPOVER_BEFORE_ENTER");

    public static final EventType<EleFXPopoverEvent> AFTER_ENTER = new EventType<>(Event.ANY,
            "ELEFX_POPOVER_AFTER_ENTER");

    public static final EventType<EleFXPopoverEvent> HIDE = new EventType<>(Event.ANY, "ELEFX_POPOVER_HIDE");

    public static final EventType<EleFXPopoverEvent> BEFORE_LEAVE = new EventType<>(Event.ANY,
            "ELEFX_POPOVER_BEFORE_LEAVE");

    public static final EventType<EleFXPopoverEvent> AFTER_LEAVE = new EventType<>(Event.ANY,
            "ELEFX_POPOVER_AFTER_LEAVE");

    public static final EventType<EleFXPopoverEvent> VISIBLE_CHANGE = new EventType<>(Event.ANY,
            "ELEFX_POPOVER_VISIBLE_CHANGE");

    private final boolean visible;

    EleFXPopoverEvent(Object source, EventTarget target, EventType<? extends EleFXPopoverEvent> type, boolean visible) {
        super(source, target, type);
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
