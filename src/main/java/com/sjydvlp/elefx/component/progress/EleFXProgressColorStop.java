package com.sjydvlp.elefx.component.progress;

import javafx.scene.paint.Paint;

/**
 * A colour selected once a progress value reaches its percentage threshold.
 * Stops are ordered by percentage by {@link EleFXProgress}; the first stop
 * whose threshold is at least the current percentage supplies the colour.
 */
public final class EleFXProgressColorStop {

    private final double percentage;

    private final Paint color;

    public EleFXProgressColorStop(double percentage, Paint color) {
        if (!Double.isFinite(percentage) || percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("percentage must be between 0 and 100");
        if (color == null) throw new IllegalArgumentException("color must not be null");
        this.percentage = percentage;
        this.color = color;
    }

    public double getPercentage() {
        return percentage;
    }

    public Paint getColor() {
        return color;
    }
}
