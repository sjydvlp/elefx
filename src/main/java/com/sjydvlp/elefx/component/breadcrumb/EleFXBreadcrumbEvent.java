package com.sjydvlp.elefx.component.breadcrumb;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * A cancellable request to navigate to a breadcrumb item's target.
 *
 * <p>
 * JavaFX has no built-in router, so applications perform navigation from this event.
 * The {@link #isReplace()} flag has the same history-replacement meaning as Element Plus.
 * </p>
 */
public final class EleFXBreadcrumbEvent extends Event {

    public static final EventType<EleFXBreadcrumbEvent> NAVIGATE = new EventType<>(Event.ANY,
            "ELEFX_BREADCRUMB_NAVIGATE");

    private final EleFXBreadcrumbItem item;

    private final String target;

    private final boolean replace;

    public EleFXBreadcrumbEvent(Object source, EventTarget target, EleFXBreadcrumbItem item) {
        super(source, target, NAVIGATE);
        this.item = item;
        this.target = item.getTo();
        this.replace = item.isReplace();
    }

    public EleFXBreadcrumbItem getItem() {
        return item;
    }

    /** The application's route/path target. */
    public String getRoute() {
        return target;
    }

    /** Whether navigation should replace the current history entry. */
    public boolean isReplace() {
        return replace;
    }
}
