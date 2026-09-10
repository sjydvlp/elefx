package com.sjydvlp.elefx.component.checkbox;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;

/** Element Plus inspired checkbox with optional group value metadata. */
public class EleFXCheckbox<T> extends CheckBox implements Themable, EleFXCheckable<T> {

    private static final String STYLE_CLASS = "ele-checkbox";

    private static final String BORDER_STYLE_CLASS = "ele-checkbox--border";

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", false);

    private final ObjectProperty<EleFXCheckboxSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXCheckboxSize.DEFAULT);

    public EleFXCheckbox() {
        initialize();
    }

    public EleFXCheckbox(String text) {
        super(text);
        initialize();
    }

    public EleFXCheckbox(String text, T value) {
        super(text);
        setValue(value);
        initialize();
    }

    public T getValue() {
        return value.get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    public boolean isBordered() {
        return bordered.get();
    }

    public BooleanProperty borderedProperty() {
        return bordered;
    }

    public void setBordered(boolean bordered) {
        this.bordered.set(bordered);
    }

    public EleFXCheckboxSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXCheckboxSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXCheckboxSize size) {
        this.size.set(size == null ? EleFXCheckboxSize.DEFAULT : size);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CHECKBOX;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateBorderStyle(isBordered());
        bordered.addListener((observable, oldValue, newValue) -> updateBorderStyle(newValue));
        updateSizeStyle(null, getSize());
        size.addListener((observable, oldValue, newValue) -> updateSizeStyle(oldValue, newValue));
        sceneBuilderIntegration();
    }

    private void updateBorderStyle(boolean enabled) {
        if (enabled) {
            if (!getStyleClass().contains(BORDER_STYLE_CLASS)) getStyleClass().add(BORDER_STYLE_CLASS);
        } else {
            getStyleClass().remove(BORDER_STYLE_CLASS);
        }
    }

    private void updateSizeStyle(EleFXCheckboxSize oldSize, EleFXCheckboxSize newSize) {
        if (oldSize != null) getStyleClass().remove(oldSize.styleClass());
        String styleClass = (newSize == null ? EleFXCheckboxSize.DEFAULT : newSize).styleClass();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }
}
