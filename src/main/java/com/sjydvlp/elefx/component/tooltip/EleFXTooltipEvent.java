package com.sjydvlp.elefx.component.tooltip;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/** Lifecycle event emitted by {@link EleFXTooltip}. */
public final class EleFXTooltipEvent extends Event {

    public static final EventType<EleFXTooltipEvent> BEFORE_SHOW = new EventType<>(Event.ANY,
            "ELEFX_TOOLTIP_BEFORE_SHOW");

    public static final EventType<EleFXTooltipEvent> SHOW = new EventType<>(Event.ANY, "ELEFX_TOOLTIP_SHOW");

    public static final EventType<EleFXTooltipEvent> BEFORE_HIDE = new EventType<>(Event.ANY,
            "ELEFX_TOOLTIP_BEFORE_HIDE");

    public static final EventType<EleFXTooltipEvent> HIDE = new EventType<>(Event.ANY, "ELEFX_TOOLTIP_HIDE");

    private final boolean visible;

    private final EleFXTooltipTrigger trigger;

    EleFXTooltipEvent(Object source, EventTarget target, EventType<? extends EleFXTooltipEvent> type,
            boolean visible, EleFXTooltipTrigger trigger) {
        super(source, target, type);
        this.visible = visible;
        this.trigger = trigger;
    }

    public boolean isVisible() {
        return visible;
    }

    public EleFXTooltipTrigger getTrigger() {
        return trigger;
    }
}
