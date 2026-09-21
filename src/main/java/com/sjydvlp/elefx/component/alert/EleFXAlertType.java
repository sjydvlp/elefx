package com.sjydvlp.elefx.component.alert;

/** Semantic styles supported by {@link EleFXAlert}. */
public enum EleFXAlertType {

    PRIMARY,
    SUCCESS,
    INFO,
    WARNING,
    ERROR;

    String styleClass() {
        return "ele-alert--" + name().toLowerCase(java.util.Locale.ROOT);
    }
}
