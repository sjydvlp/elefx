package com.sjydvlp.elefx.component.typography;

/** Element Plus typography scale. */
public enum EleFXTypographySize {

    EXTRA_SMALL("extra-small"),
    SMALL("small"),
    BASE("base"),
    MEDIUM("medium"),
    LARGE("large"),
    EXTRA_LARGE("extra-large");

    private final String value;

    EleFXTypographySize(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-typography--" + value;
    }
}
