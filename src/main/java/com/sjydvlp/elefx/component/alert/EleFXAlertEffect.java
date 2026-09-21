package com.sjydvlp.elefx.component.alert;

/** Colour treatment for an {@link EleFXAlert}. */
public enum EleFXAlertEffect {

    LIGHT,
    DARK;

    String styleClass() {
        return "ele-alert--effect-" + name().toLowerCase(java.util.Locale.ROOT);
    }
}
