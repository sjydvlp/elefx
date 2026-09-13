package com.sjydvlp.elefx.component.rate;

/** Visual sizes supported by {@link EleFXRate}. */
public enum EleFXRateSize {

    LARGE("large", 24),
    DEFAULT("default", 20),
    SMALL("small", 16);

    private final String cssName;

    private final double iconSize;

    EleFXRateSize(String cssName, double iconSize) {
        this.cssName = cssName;
        this.iconSize = iconSize;
    }

    public String styleClass() {
        return "ele-rate--size-" + cssName;
    }

    public double iconSize() {
        return iconSize;
    }
}
