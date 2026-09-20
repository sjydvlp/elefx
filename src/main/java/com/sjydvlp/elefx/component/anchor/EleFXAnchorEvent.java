package com.sjydvlp.elefx.component.anchor;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted when an anchor link becomes active or is activated by the user. */
public final class EleFXAnchorEvent extends Event {

    public static final EventType<EleFXAnchorEvent> CHANGE = new EventType<>(Event.ANY, "ELEFX_ANCHOR_CHANGE");

    public static final EventType<EleFXAnchorEvent> CLICK = new EventType<>(Event.ANY, "ELEFX_ANCHOR_CLICK");

    private final EleFXAnchorLink link;

    public EleFXAnchorEvent(Object source, EventTarget target, EventType<? extends EleFXAnchorEvent> type,
            EleFXAnchorLink link) {
        super(source, target, type);
        this.link = link;
    }

    /** Link associated with this event. */
    public EleFXAnchorLink getLink() {
        return link;
    }

    /** href of the associated link, or an empty string when none is set. */
    public String getHref() {
        return link == null ? "" : link.getHref();
    }
}
