package com.sjydvlp.elefx.component.tag;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;

/** A toggleable Element Plus-inspired check tag. */
public class EleFXCheckTag extends ToggleButton implements Themable {

    private static final String STYLE_CLASS = "ele-check-tag";

    private final ObjectProperty<EleFXTagType> type = new SimpleObjectProperty<>(this, "type", EleFXTagType.PRIMARY);

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    public EleFXCheckTag() {
        initialize();
    }

    public EleFXCheckTag(String text) {
        super(text);
        initialize();
    }

    public EleFXCheckTag(String text, EleFXTagType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFXTagType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXTagType> typeProperty() {
        return type;
    }

    public void setType(EleFXTagType type) {
        this.type.set(type == null ? EleFXTagType.PRIMARY : type);
    }

    /** Element Plus-compatible alias for JavaFX's selected state. */
    public boolean isChecked() {
        return isSelected();
    }

    /** Element Plus-compatible alias for JavaFX's selected property. */
    public javafx.beans.property.BooleanProperty checkedProperty() {
        return selectedProperty();
    }

    /** Element Plus-compatible alias for {@link #setSelected(boolean)}. */
    public void setChecked(boolean checked) {
        setSelected(checked);
    }

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<ActionEvent> handler) {
        onChange.set(handler);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TAG;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateType(null, getType());
        type.addListener((o, oldValue, newValue) -> updateType(oldValue, newValue));
        onChange.addListener((o, oldValue, newValue) -> setOnAction(newValue));
        sceneBuilderIntegration();
    }

    private void updateType(EleFXTagType oldValue, EleFXTagType newValue) {
        if (oldValue != null) getStyleClass().remove(oldValue.styleClass().replace("ele-tag", "ele-check-tag"));
        String styleClass = (newValue == null ? EleFXTagType.PRIMARY : newValue).styleClass()
                .replace("ele-tag", "ele-check-tag");
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }
}
