package com.sjydvlp.elefx.component.collapse;

import java.util.List;
import java.util.Objects;

/** Immutable context supplied to {@link EleFXCollapse#setBeforeCollapse} before a user-driven state change. */
public final class EleFXCollapseBeforeChange {

    private final EleFXCollapseItem item;

    private final String itemName;

    private final boolean expanding;

    private final List<String> activeNames;

    private final List<String> nextActiveNames;

    EleFXCollapseBeforeChange(EleFXCollapseItem item, String itemName, boolean expanding,
            List<String> activeNames, List<String> nextActiveNames) {
        this.item = Objects.requireNonNull(item, "item");
        this.itemName = Objects.requireNonNull(itemName, "itemName");
        this.expanding = expanding;
        this.activeNames = List.copyOf(activeNames);
        this.nextActiveNames = List.copyOf(nextActiveNames);
    }

    /** Item whose header was activated. */
    public EleFXCollapseItem getItem() {
        return item;
    }

    /** Resolved unique name of {@link #getItem()}. */
    public String getItemName() {
        return itemName;
    }

    /** Whether the requested operation expands, rather than collapses, the item. */
    public boolean isExpanding() {
        return expanding;
    }

    /** Active names before the requested operation. */
    public List<String> getActiveNames() {
        return activeNames;
    }

    /** Active names that will apply if the callback returns {@code true}. */
    public List<String> getNextActiveNames() {
        return nextActiveNames;
    }
}
