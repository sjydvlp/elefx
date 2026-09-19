package com.sjydvlp.elefx.component.segmented;

/** Element Plus compatible segmented size presets. */
public enum EleFXSegmentedSize {

    LARGE("ele-segmented--large"),
    DEFAULT("ele-segmented--default"),
    SMALL("ele-segmented--small");

    private final String styleClass;

    EleFXSegmentedSize(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return styleClass;
    }
}
