package com.sjydvlp.elefx.component.watermark;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Text rendering options for {@link EleFXWatermark}. */
public final class EleFXWatermarkFont {

    private final StringProperty color = new SimpleStringProperty(this, "color", "rgba(0, 0, 0, .15)");

    private final DoubleProperty fontSize = new SimpleDoubleProperty(this, "fontSize", 16);

    private final ObjectProperty<FontWeight> fontWeight = new SimpleObjectProperty<>(this, "fontWeight",
            FontWeight.NORMAL);

    private final StringProperty fontFamily = new SimpleStringProperty(this, "fontFamily", "sans-serif");

    private final DoubleProperty fontGap = new SimpleDoubleProperty(this, "fontGap", 3);

    private final ObjectProperty<FontPosture> fontStyle = new SimpleObjectProperty<>(this, "fontStyle",
            FontPosture.REGULAR);

    private final ObjectProperty<TextAlignment> textAlign = new SimpleObjectProperty<>(this, "textAlign",
            TextAlignment.CENTER);

    private final StringProperty textBaseline = new SimpleStringProperty(this, "textBaseline", "hanging");

    public String getColor() {
        return color.get();
    }

    public StringProperty colorProperty() {
        return color;
    }

    public void setColor(String value) {
        color.set(value == null ? "rgba(0, 0, 0, .15)" : value);
    }

    public double getFontSize() {
        return fontSize.get();
    }

    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public void setFontSize(double value) {
        fontSize.set(Math.max(1, value));
    }

    public FontWeight getFontWeight() {
        return fontWeight.get();
    }

    public ObjectProperty<FontWeight> fontWeightProperty() {
        return fontWeight;
    }

    public void setFontWeight(FontWeight value) {
        fontWeight.set(value == null ? FontWeight.NORMAL : value);
    }

    public String getFontFamily() {
        return fontFamily.get();
    }

    public StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public void setFontFamily(String value) {
        fontFamily.set(value == null || value.isBlank() ? "sans-serif" : value);
    }

    public double getFontGap() {
        return fontGap.get();
    }

    public DoubleProperty fontGapProperty() {
        return fontGap;
    }

    public void setFontGap(double value) {
        fontGap.set(Math.max(0, value));
    }

    public FontPosture getFontStyle() {
        return fontStyle.get();
    }

    public ObjectProperty<FontPosture> fontStyleProperty() {
        return fontStyle;
    }

    public void setFontStyle(FontPosture value) {
        fontStyle.set(value == null ? FontPosture.REGULAR : value);
    }

    public TextAlignment getTextAlign() {
        return textAlign.get();
    }

    public ObjectProperty<TextAlignment> textAlignProperty() {
        return textAlign;
    }

    public void setTextAlign(TextAlignment value) {
        textAlign.set(value == null ? TextAlignment.CENTER : value);
    }

    /** Canvas-compatible baseline: {@code hanging}, {@code center}, {@code alphabetic}, or {@code bottom}. */
    public String getTextBaseline() {
        return textBaseline.get();
    }

    public StringProperty textBaselineProperty() {
        return textBaseline;
    }

    public void setTextBaseline(String value) {
        textBaseline.set(value == null || value.isBlank() ? "hanging" : value);
    }
}
