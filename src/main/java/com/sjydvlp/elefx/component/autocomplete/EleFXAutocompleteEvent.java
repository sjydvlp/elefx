package com.sjydvlp.elefx.component.autocomplete;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Event emitted after an autocomplete suggestion is selected. */
public class EleFXAutocompleteEvent<T> extends Event {

    public static final EventType<EleFXAutocompleteEvent<?>> SELECT = new EventType<>(Event.ANY,
            "ELEFX_AUTOCOMPLETE_SELECT");

    private final T item;

    @SuppressWarnings("unchecked")
    public EleFXAutocompleteEvent(Object source, EventTarget target, T item) {
        super(source, target, (EventType<? extends Event>) SELECT);
        this.item = item;
    }

    public T getItem() {
        return item;
    }
}
