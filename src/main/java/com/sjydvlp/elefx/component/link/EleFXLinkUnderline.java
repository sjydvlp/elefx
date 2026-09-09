package com.sjydvlp.elefx.component.link;

/** Determines when an {@link EleFXLink} displays its underline. */
public enum EleFXLinkUnderline {

    ALWAYS("always"),
    HOVER("hover"),
    NEVER("never");

    private final String value;

    EleFXLinkUnderline(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-link--underline-" + value;
    }
}
