package com.sjydvlp.elefx.component.divider;

/** Line treatment supported by Element Plus dividers. */
public enum EleFXDividerBorderStyle {

    SOLID,
    DASHED,
    DOTTED,
    DOUBLE;

    String styleClass() {
        return "ele-divider--" + name().toLowerCase();
    }
}
