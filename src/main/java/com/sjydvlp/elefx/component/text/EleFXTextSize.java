package com.sjydvlp.elefx.component.text;

/** Visual sizes for {@link EleFXText}. */
public enum EleFXTextSize {

    LARGE("large"),
    DEFAULT("default"),
    SMALL("small");

    private final String value;

    EleFXTextSize(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-text--size-" + value;
    }
}
