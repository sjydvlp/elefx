package com.sjydvlp.elefx.component.tabs;

/** Position of the tab navigation strip. */
public enum EleFXTabsPosition {

    TOP("ele-tabs--top"),
    RIGHT("ele-tabs--right"),
    BOTTOM("ele-tabs--bottom"),
    LEFT("ele-tabs--left");

    private final String styleClass;

    EleFXTabsPosition(String styleClass) {
        this.styleClass = styleClass;
    }

    String styleClass() {
        return styleClass;
    }
}
