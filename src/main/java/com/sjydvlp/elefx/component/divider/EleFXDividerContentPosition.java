package com.sjydvlp.elefx.component.divider;

/** Position of horizontal divider content. */
public enum EleFXDividerContentPosition {

    LEFT,
    CENTER,
    RIGHT;

    String styleClass() {
        return "ele-divider--content-" + name().toLowerCase();
    }
}
