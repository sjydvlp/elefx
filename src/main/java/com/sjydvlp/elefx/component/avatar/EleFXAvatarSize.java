package com.sjydvlp.elefx.component.avatar;

/** Element Plus Avatar preset sizes. */
public enum EleFXAvatarSize {

    LARGE("large", 56),
    DEFAULT("default", 40),
    SMALL("small", 24);

    private final String value;

    private final double pixels;

    EleFXAvatarSize(String value, double pixels) {
        this.value = value;
        this.pixels = pixels;
    }

    public double pixels() {
        return pixels;
    }

    public String styleClass() {
        return "ele-avatar--" + value;
    }
}
