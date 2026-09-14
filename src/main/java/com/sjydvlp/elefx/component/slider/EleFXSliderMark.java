package com.sjydvlp.elefx.component.slider;

import javafx.scene.paint.Paint;

/** Label and optional label colour for an {@link EleFXSlider} mark. */
public final class EleFXSliderMark {

    private final String label;

    private final Paint color;

    public EleFXSliderMark(String label) {
        this(label, null);
    }

    public EleFXSliderMark(String label, Paint color) {
        this.label = label == null ? "" : label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Paint getColor() {
        return color;
    }
}
