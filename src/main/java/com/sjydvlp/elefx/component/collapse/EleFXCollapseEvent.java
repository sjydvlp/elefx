package com.sjydvlp.elefx.component.collapse;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import java.util.List;

/** Event emitted after the active collapse items change through user interaction. */
public class EleFXCollapseEvent extends Event {

    public static final EventType<EleFXCollapseEvent> CHANGE = new EventType<>(Event.ANY,
            "ELEFX_COLLAPSE_CHANGE");

    private final List<String> activeNames;

    public EleFXCollapseEvent(Object source, EventTarget target, List<String> activeNames) {
        super(source, target, CHANGE);
        this.activeNames = List.copyOf(activeNames);
    }

    public List<String> getActiveNames() {
        return activeNames;
    }
}
