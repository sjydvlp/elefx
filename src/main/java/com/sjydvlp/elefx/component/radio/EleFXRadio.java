package com.sjydvlp.elefx.component.radio;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;

/**
 * Element Plus inspired radio control.
 *
 * <p>
 * {@code value} is the value contributed to an {@link EleFXRadioGroup};
 * {@link #getText()} is its visible label. The two are deliberately separate,
 * matching Element Plus' current value/label API.
 * </p>
 */
public class EleFXRadio<T> extends RadioButton implements Themable {

    private static final String STYLE_CLASS = "ele-radio";

    private static final String BORDER_STYLE_CLASS = "ele-radio--border";

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", false);

    private final ObjectProperty<EleFXRadioSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXRadioSize.DEFAULT);

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    public EleFXRadio() {
        initialize();
    }

    public EleFXRadio(String text) {
        super(text);
        initialize();
    }

    public EleFXRadio(String text, T value) {
        super(text);
        setValue(value);
        initialize();
    }

    /** Alias for Element Plus' display-only {@code label} attribute. */
    public String getLabel() {
        return getText();
    }

    public void setLabel(String label) {
        setText(label);
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

    /** Alias for the Element Plus {@code border} attribute. */
    public boolean isBorder() {
        return isBordered();
    }

    public void setBorder(boolean border) {
        setBordered(border);
    }

    public EleFXRadioSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXRadioSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXRadioSize size) {
        this.size.set(size == null ? EleFXRadioSize.DEFAULT : size);
    }

    /** A semantic name for form metadata and accessibility tooling. */
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name == null ? "" : name);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.RADIO;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateBooleanStyle(isBordered(), BORDER_STYLE_CLASS);
        bordered.addListener((observable, oldValue, newValue) -> updateBooleanStyle(newValue, BORDER_STYLE_CLASS));
        updateSizeStyle(null, getSize());
        size.addListener((observable, oldValue, newValue) -> updateSizeStyle(oldValue, newValue));
        sceneBuilderIntegration();
    }

    private void updateBooleanStyle(boolean active, String styleClass) {
        if (active && !getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
        if (!active) getStyleClass().remove(styleClass);
    }

    private void updateSizeStyle(EleFXRadioSize oldSize, EleFXRadioSize newSize) {
        if (oldSize != null) getStyleClass().remove(oldSize.styleClass());
        String styleClass = (newSize == null ? EleFXRadioSize.DEFAULT : newSize).styleClass();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }
}
