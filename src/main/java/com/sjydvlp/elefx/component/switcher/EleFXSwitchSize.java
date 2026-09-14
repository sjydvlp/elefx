package com.sjydvlp.elefx.component.switcher;

/** Visual sizes supported by {@link EleFXSwitch}. */
public enum EleFXSwitchSize {

    LARGE("large", 50, 24, 20),
    DEFAULT("default", 40, 20, 16),
    SMALL("small", 30, 16, 12);

    private final String cssName;

    private final double width;

    private final double height;

    private final double thumbSize;

    EleFXSwitchSize(String cssName, double width, double height, double thumbSize) {
        this.cssName = cssName;
        this.width = width;
        this.height = height;
        this.thumbSize = thumbSize;
    }

    public String styleClass() {
        return "ele-switch--size-" + cssName;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double thumbSize() {
        return thumbSize;
    }
}
