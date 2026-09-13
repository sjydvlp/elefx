package com.sjydvlp.elefx.component.inputnumber;

/** Position of the increment and decrement controls. */
public enum EleFXInputNumberControlsPosition {

    DEFAULT("default"),
    RIGHT("right");

    private final String cssName;

    EleFXInputNumberControlsPosition(String cssName) {
        this.cssName = cssName;
    }

    String styleClass() {
        return "ele-input-number--controls-" + cssName;
    }
}
