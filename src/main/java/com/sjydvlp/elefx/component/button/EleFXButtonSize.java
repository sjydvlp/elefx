package com.sjydvlp.elefx.component.button;

/**
 * Visual sizes supported by {@link EleFXButton}.
 */
public enum EleFXButtonSize {

    LARGE("large"),
    DEFAULT("default"),
    SMALL("small");

    private final String styleClass;

    EleFXButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return "ele-button--size-" + styleClass;
    }
}
