package com.sjydvlp.elefx.component.layout;

/** 栅格行的水平对齐方式。 */
public enum EleFXRowJustify {

    START("start"),
    CENTER("center"),
    END("end"),
    SPACE_BETWEEN("space-between"),
    SPACE_AROUND("space-around"),
    SPACE_EVENLY("space-evenly");

    private final String value;

    EleFXRowJustify(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-row--justify-" + value;
    }
}
