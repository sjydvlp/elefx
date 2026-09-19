package com.sjydvlp.elefx.component.segmented;

/** Layout direction for {@link EleFXSegmented}. */
public enum EleFXSegmentedDirection {

    HORIZONTAL("ele-segmented--horizontal"),
    VERTICAL("ele-segmented--vertical");

    private final String styleClass;

    EleFXSegmentedDirection(String styleClass) {
        this.styleClass = styleClass;
    }

    public String styleClass() {
        return styleClass;
    }
}
