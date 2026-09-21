package com.sjydvlp.elefx.component.notification;

/** Semantic appearance of an {@link EleFXNotification}. */
public enum EleFXNotificationType {

    PRIMARY,
    SUCCESS,
    INFO,
    WARNING,
    ERROR;

    String styleClass() {
        return "ele-notification--" + name().toLowerCase(java.util.Locale.ROOT);
    }
}
