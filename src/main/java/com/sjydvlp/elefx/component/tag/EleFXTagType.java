package com.sjydvlp.elefx.component.tag;

/** Semantic colour variants for {@link EleFXTag}. */
public enum EleFXTagType {

    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    private final String value;

    EleFXTagType(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-tag--" + value;
    }
}
