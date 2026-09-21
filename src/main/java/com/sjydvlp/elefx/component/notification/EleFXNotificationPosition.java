package com.sjydvlp.elefx.component.notification;

/** Corner in which an {@link EleFXNotification} stack is displayed. */
public enum EleFXNotificationPosition {

    TOP_RIGHT,
    TOP_LEFT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT;

    boolean isBottom() {
        return name().startsWith("BOTTOM");
    }

    boolean isLeft() {
        return name().endsWith("LEFT");
    }
}
