package com.sjydvlp.elefx.component.affix;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted when an {@link EleFXAffix} changes state or its target scrolls. */
public class EleFXAffixEvent extends Event {

    public static final EventType<EleFXAffixEvent> CHANGE = new EventType<>(Event.ANY, "ELEFX_AFFIX_CHANGE");

    public static final EventType<EleFXAffixEvent> SCROLL = new EventType<>(Event.ANY, "ELEFX_AFFIX_SCROLL");

    private final boolean fixed;

    private final double scrollTop;

    public EleFXAffixEvent(Object source, EventTarget target, EventType<? extends EleFXAffixEvent> eventType,
            boolean fixed, double scrollTop) {
        super(source, target, eventType);
        this.fixed = fixed;
        this.scrollTop = scrollTop;
    }

    /** Whether the affix is currently pinned to its configured viewport edge. */
    public boolean isFixed() {
        return fixed;
    }

    /** Target vertical scroll offset in pixels. */
    public double getScrollTop() {
        return scrollTop;
    }
}
