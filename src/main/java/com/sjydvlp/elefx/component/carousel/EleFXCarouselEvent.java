package com.sjydvlp.elefx.component.carousel;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted after the active carousel item changes. */
public final class EleFXCarouselEvent extends Event {

    public static final EventType<EleFXCarouselEvent> CHANGE = new EventType<>(Event.ANY, "ELEFX_CAROUSEL_CHANGE");

    private final int activeIndex;

    private final int previousIndex;

    public EleFXCarouselEvent(Object source, EventTarget target, int activeIndex, int previousIndex) {
        super(source, target, CHANGE);
        this.activeIndex = activeIndex;
        this.previousIndex = previousIndex;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public int getPreviousIndex() {
        return previousIndex;
    }
}
