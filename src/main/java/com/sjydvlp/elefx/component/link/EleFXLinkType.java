package com.sjydvlp.elefx.component.link;

/** Semantic colour variants for {@link EleFXLink}. */
public enum EleFXLinkType {

    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    private final String value;

    EleFXLinkType(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-link--" + value;
    }
}
