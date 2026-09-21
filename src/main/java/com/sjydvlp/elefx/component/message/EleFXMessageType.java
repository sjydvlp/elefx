package com.sjydvlp.elefx.component.message;

/** Semantic appearance of an {@link EleFXMessage}. */
public enum EleFXMessageType {

    PRIMARY,
    SUCCESS,
    INFO,
    WARNING,
    ERROR;

    String styleClass() {
        return "ele-message--" + name().toLowerCase(java.util.Locale.ROOT);
    }
}
