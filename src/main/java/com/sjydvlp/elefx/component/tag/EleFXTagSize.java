package com.sjydvlp.elefx.component.tag;

/** Element Plus compatible size presets for {@link EleFXTag}. */
public enum EleFXTagSize {

    LARGE("large"),
    DEFAULT("default"),
    SMALL("small");

    private final String value;

    EleFXTagSize(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-tag--size-" + value;
    }
}
