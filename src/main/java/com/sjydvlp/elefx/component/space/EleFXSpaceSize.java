package com.sjydvlp.elefx.component.space;

/** Preset gaps used by {@link EleFXSpace}. */
public enum EleFXSpaceSize {

    SMALL(8),
    DEFAULT(12),
    LARGE(16);

    private final double pixels;

    EleFXSpaceSize(double pixels) {
        this.pixels = pixels;
    }

    public double pixels() {
        return pixels;
    }
}
