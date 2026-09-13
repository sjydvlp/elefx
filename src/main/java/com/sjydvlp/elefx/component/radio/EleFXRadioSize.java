package com.sjydvlp.elefx.component.radio;

/** Element Plus compatible radio size presets. */
public enum EleFXRadioSize {

    LARGE("ele-radio--size-large"),
    DEFAULT("ele-radio--size-default"),
    SMALL("ele-radio--size-small");

    private final String styleClass;

    EleFXRadioSize(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return styleClass;
    }
}
