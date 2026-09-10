package com.sjydvlp.elefx.component.checkbox;

/** Element Plus compatible checkbox size presets. */
public enum EleFXCheckboxSize {

    LARGE("ele-checkbox--size-large"),
    DEFAULT("ele-checkbox--size-default"),
    SMALL("ele-checkbox--size-small");

    private final String styleClass;

    EleFXCheckboxSize(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return styleClass;
    }
}
