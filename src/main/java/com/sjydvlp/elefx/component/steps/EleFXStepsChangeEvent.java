package com.sjydvlp.elefx.component.steps;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted whenever the active step changes. */
public class EleFXStepsChangeEvent extends Event {

    public static final EventType<EleFXStepsChangeEvent> CHANGE = new EventType<>(Event.ANY, "ELEFX_STEPS_CHANGE");

    private final int oldActive;

    private final int active;

    public EleFXStepsChangeEvent(Object source, EventTarget target, int oldActive, int active) {
        super(source, target, CHANGE);
        this.oldActive = oldActive;
        this.active = active;
    }

    public int getOldActive() {
        return oldActive;
    }

    public int getActive() {
        return active;
    }
}
