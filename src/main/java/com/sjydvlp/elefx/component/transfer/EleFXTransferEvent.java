package com.sjydvlp.elefx.component.transfer;

import javafx.event.Event;
import javafx.event.EventType;
import java.util.List;

/** Fired after one or more enabled items have moved between transfer panels. */
public final class EleFXTransferEvent<T> extends Event {

    public static final EventType<EleFXTransferEvent<?>> TRANSFER = new EventType<>(Event.ANY, "ELEFX_TRANSFER");

    private final EleFXTransferDirection direction;

    private final List<T> movedValues;

    @SuppressWarnings({"unchecked", "rawtypes"})
    EleFXTransferEvent(Object source, EleFXTransferDirection direction, List<T> movedValues) {
        super(source, null, (EventType) TRANSFER);
        this.direction = direction;
        this.movedValues = List.copyOf(movedValues);
    }

    public EleFXTransferDirection getDirection() {
        return direction;
    }

    public List<T> getMovedValues() {
        return movedValues;
    }
}
