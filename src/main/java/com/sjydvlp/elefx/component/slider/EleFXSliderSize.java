package com.sjydvlp.elefx.component.slider;

/** Visual sizes supported by {@link EleFXSlider}. */
public enum EleFXSliderSize {

    LARGE("ele-slider--size-large", 40, 10),
    DEFAULT("ele-slider--size-default", 32, 10),
    SMALL("ele-slider--size-small", 24, 8);

    private final String styleClass;

    private final double controlHeight;

    private final double handleRadius;

    EleFXSliderSize(String styleClass, double controlHeight, double handleRadius) {
        this.styleClass = styleClass;
        this.controlHeight = controlHeight;
        this.handleRadius = handleRadius;
    }

    public String styleClass() {
        return styleClass;
    }

    public double controlHeight() {
        return controlHeight;
    }

    public double handleRadius() {
        return handleRadius;
    }
}
