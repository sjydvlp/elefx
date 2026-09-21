package com.sjydvlp.elefx.component.pageheader;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted when the user activates a page header's back section. */
public final class EleFXPageHeaderEvent extends Event {

    public static final EventType<EleFXPageHeaderEvent> BACK = new EventType<>(Event.ANY, "ELEFX_PAGE_HEADER_BACK");

    public EleFXPageHeaderEvent(Object source, EventTarget target) {
        super(source, target, BACK);
    }
}
