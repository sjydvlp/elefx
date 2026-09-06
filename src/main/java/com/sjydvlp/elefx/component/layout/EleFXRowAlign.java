package com.sjydvlp.elefx.component.layout;

/** 栅格行的垂直对齐方式，默认拉伸各列到所在行的高度。 */
public enum EleFXRowAlign {

    STRETCH("stretch"),
    TOP("top"),
    MIDDLE("middle"),
    BOTTOM("bottom");

    private final String value;

    EleFXRowAlign(String value) {
        this.value = value;
    }

    public String styleClass() {
        return "ele-row--align-" + value;
    }
}
