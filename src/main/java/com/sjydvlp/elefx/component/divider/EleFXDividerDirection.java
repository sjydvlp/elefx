package com.sjydvlp.elefx.component.divider;

/** The direction of an {@link EleFXDivider}. */
public enum EleFXDividerDirection {

    HORIZONTAL,
    VERTICAL;

    String styleClass() {
        return "ele-divider--" + name().toLowerCase();
    }
}
