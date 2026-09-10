package com.sjydvlp.elefx.component.typography;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/**
 * A Label that applies Element Plus's six-level type scale and line-height convention.
 *
 * <p>
 * For semantic inline text treatments such as success/danger or strikethrough,
 * use {@code EleFXText}; this component is intended for document and page typography.
 * </p>
 */
public class EleFXTypography extends Label implements Themable {

    private static final String STYLE_CLASS = "ele-typography";

    private final ObjectProperty<EleFXTypographySize> size = new SimpleObjectProperty<>(this, "size",
            EleFXTypographySize.BASE);

    private final ObjectProperty<EleFXTypographyLineHeight> lineHeight = new SimpleObjectProperty<>(this, "lineHeight",
            EleFXTypographyLineHeight.REGULAR);

    public EleFXTypography() {
        initialize();
    }

    public EleFXTypography(String text) {
        super(text);
        initialize();
    }

    public EleFXTypography(String text, EleFXTypographySize size) {
        super(text);
        setSize(size);
        initialize();
    }

    public EleFXTypographySize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXTypographySize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXTypographySize size) {
        this.size.set(size == null ? EleFXTypographySize.BASE : size);
    }

    public EleFXTypographyLineHeight getLineHeight() {
        return lineHeight.get();
    }

    public ObjectProperty<EleFXTypographyLineHeight> lineHeightProperty() {
        return lineHeight;
    }

    public void setLineHeight(EleFXTypographyLineHeight lineHeight) {
        this.lineHeight.set(lineHeight == null ? EleFXTypographyLineHeight.REGULAR : lineHeight);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TYPOGRAPHY;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setWrapText(true);
        updateStyleClass(null, getSize());
        updateStyleClass(null, getLineHeight());
        size.addListener((observable, oldValue, value) -> updateStyleClass(oldValue, value));
        lineHeight.addListener((observable, oldValue, value) -> updateStyleClass(oldValue, value));
        sceneBuilderIntegration();
    }

    private void updateStyleClass(Object oldValue, Object newValue) {
        if (oldValue instanceof EleFXTypographySize oldSize) getStyleClass().remove(oldSize.styleClass());
        if (oldValue instanceof EleFXTypographyLineHeight oldLineHeight)
            getStyleClass().remove(oldLineHeight.styleClass());
        String styleClass = newValue instanceof EleFXTypographySize typographySize
                ? typographySize.styleClass()
                : ((EleFXTypographyLineHeight) newValue).styleClass();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
        requestLayout();
    }
}
