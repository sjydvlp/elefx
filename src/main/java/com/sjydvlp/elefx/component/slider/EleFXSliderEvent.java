package com.sjydvlp.elefx.component.slider;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Slider input (live) and change (committed) events. */
public class EleFXSliderEvent extends Event {

    public static final EventType<EleFXSliderEvent> INPUT = new EventType<>(Event.ANY, "ELEFX_SLIDER_INPUT");

    public static final EventType<EleFXSliderEvent> CHANGE = new EventType<>(Event.ANY, "ELEFX_SLIDER_CHANGE");

    private final double oldValue, value, oldUpperValue, upperValue;

    public EleFXSliderEvent(Object source, EventTarget target, EventType<? extends EleFXSliderEvent> type,
            double oldValue, double value, double oldUpperValue, double upperValue) {
        super(source, target, type);
        this.oldValue = oldValue;
        this.value = value;
        this.oldUpperValue = oldUpperValue;
        this.upperValue = upperValue;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getValue() {
        return value;
    }

    public double getOldUpperValue() {
        return oldUpperValue;
    }

    public double getUpperValue() {
        return upperValue;
    }
}
