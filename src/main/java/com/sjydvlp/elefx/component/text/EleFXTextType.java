package com.sjydvlp.elefx.component.text;

/** Semantic colour variants for {@link EleFXText}. */
public enum EleFXTextType {

    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    private final String value;

    EleFXTextType(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-text--" + value;
    }
}
