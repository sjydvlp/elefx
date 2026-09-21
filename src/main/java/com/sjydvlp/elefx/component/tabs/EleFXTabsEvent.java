package com.sjydvlp.elefx.component.tabs;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;

/** Event emitted for tab selection, addition and removal. */
public class EleFXTabsEvent extends Event {

    public static final EventType<EleFXTabsEvent> ANY = new EventType<>(Event.ANY, "ELEFX_TABS");

    public static final EventType<EleFXTabsEvent> TAB_CLICK = new EventType<>(ANY, "TAB_CLICK");

    public static final EventType<EleFXTabsEvent> TAB_CHANGE = new EventType<>(ANY, "TAB_CHANGE");

    public static final EventType<EleFXTabsEvent> TAB_ADD = new EventType<>(ANY, "TAB_ADD");

    public static final EventType<EleFXTabsEvent> TAB_REMOVE = new EventType<>(ANY, "TAB_REMOVE");

    public static final EventType<EleFXTabsEvent> EDIT = new EventType<>(ANY, "EDIT");

    private final EleFXTabPane pane;

    private final Object oldName;

    private final String action;

    private final InputEvent inputEvent;

    EleFXTabsEvent(Object source, EventTarget target, EventType<? extends EleFXTabsEvent> type,
            EleFXTabPane pane, Object oldName, String action, InputEvent inputEvent) {
        super(source, target, type);
        this.pane = pane;
        this.oldName = oldName;
        this.action = action;
        this.inputEvent = inputEvent;
    }

    public EleFXTabPane getPane() {
        return pane;
    }

    public Object getName() {
        return pane == null ? null : pane.getName();
    }

    public Object getOldName() {
        return oldName;
    }

    /** "add" or "remove" for edit events; null for selection events. */
    public String getAction() {
        return action;
    }

    public InputEvent getInputEvent() {
        return inputEvent;
    }
}
