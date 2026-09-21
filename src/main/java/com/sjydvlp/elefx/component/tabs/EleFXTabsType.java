package com.sjydvlp.elefx.component.tabs;

/** Visual variants supported by {@link EleFXTabs}. */
public enum EleFXTabsType {

    DEFAULT("ele-tabs--default"),
    CARD("ele-tabs--card"),
    BORDER_CARD("ele-tabs--border-card");

    private final String styleClass;

    EleFXTabsType(String styleClass) {
        this.styleClass = styleClass;
    }

    String styleClass() {
        return styleClass;
    }
}
