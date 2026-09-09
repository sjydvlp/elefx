package com.sjydvlp.elefx.component.link;

/** Intended destination target for an {@link EleFXLink} href. */
public enum EleFXLinkTarget {

    SELF("_self"),
    BLANK("_blank"),
    PARENT("_parent"),
    TOP("_top");

    private final String value;

    EleFXLinkTarget(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
