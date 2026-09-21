package com.sjydvlp.elefx.component.message;

/** Viewport edge at which a message stack is displayed. */
public enum EleFXMessagePlacement {

    TOP,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    boolean isBottom() {
        return name().startsWith("BOTTOM");
    }

    boolean isLeft() {
        return name().endsWith("LEFT");
    }

    boolean isRight() {
        return name().endsWith("RIGHT");
    }
}
