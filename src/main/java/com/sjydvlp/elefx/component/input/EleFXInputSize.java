package com.sjydvlp.elefx.component.input;

/** Visual sizes supported by {@link EleFXInput}. */
public enum EleFXInputSize {

    LARGE("large"),
    DEFAULT("default"),
    SMALL("small");

    private final String cssName;

    EleFXInputSize(String cssName) {
        this.cssName = cssName;
    }

    public String styleClass() {
        return "ele-input--size-" + cssName;
    }
}
