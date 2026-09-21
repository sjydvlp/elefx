package com.sjydvlp.elefx.component.menu;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import java.util.List;

/** Selection and expansion event emitted by {@link EleFXMenu}. */
public final class EleFXMenuEvent extends Event {

    public static final EventType<EleFXMenuEvent> SELECT = new EventType<>(Event.ANY, "ELEFX_MENU_SELECT");

    public static final EventType<EleFXMenuEvent> OPEN = new EventType<>(Event.ANY, "ELEFX_MENU_OPEN");

    public static final EventType<EleFXMenuEvent> CLOSE = new EventType<>(Event.ANY, "ELEFX_MENU_CLOSE");

    private final String index;

    private final List<String> indexPath;

    private final EleFXMenuItem item;

    EleFXMenuEvent(Object source, EventTarget target, EventType<EleFXMenuEvent> type, String index,
            List<String> indexPath, EleFXMenuItem item) {
        super(source, target, type);
        this.index = index;
        this.indexPath = List.copyOf(indexPath);
        this.item = item;
    }

    public String getIndex() {
        return index;
    }

    public List<String> getIndexPath() {
        return indexPath;
    }

    public EleFXMenuItem getItem() {
        return item;
    }
}
