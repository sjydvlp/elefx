package com.sjydvlp.elefx.component.tag;

/** Visual treatment for an {@link EleFXTag}. */
public enum EleFXTagEffect {

    DARK("dark"),
    LIGHT("light"),
    PLAIN("plain");

    private final String value;

    EleFXTagEffect(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-tag--effect-" + value;
    }
}
