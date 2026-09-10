package com.sjydvlp.elefx.component.typography;

/** Standard Element Plus line-height multipliers. */
public enum EleFXTypographyLineHeight {

    NONE(1.0),
    COMPACT(1.3),
    REGULAR(1.5),
    LOOSE(1.7);

    private final double multiplier;

    EleFXTypographyLineHeight(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }

    public String styleClass() {
        return "ele-typography--line-" + name().toLowerCase();
    }
}
