package com.sjydvlp.elefx.button;

import com.sjydvlp.elefx.theme.EleStylesheet;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * 按钮
 *
 * @author sjydvlp@163.com
 * @date 2026/8/18 23:33
 */
public class EleButton extends Button implements Themable {

    private static final String STYLE_CLASS = "ele-button";

    private final ObjectProperty<EleButtonType> type = new SimpleObjectProperty<>(this, "type", EleButtonType.DEFAULT);

    public EleButton() {
        super("Button");
        initialize();
    }

    public EleButton(String text) {
        super(text);
        initialize();
    }

    public EleButton(String text, EleButtonType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleButton(String text, double prefWidth, double prefHeight) {
        super(text);
        setPrefSize(prefWidth, prefHeight);
        initialize();
    }

    public EleButton(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    public EleButtonType getType() {
        return type.get();
    }

    public ObjectProperty<EleButtonType> typeProperty() {
        return type;
    }

    public void setType(EleButtonType type) {
        this.type.set(type == null ? EleButtonType.DEFAULT : type);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleStylesheet.BUTTON;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateTypeStyleClass(null, getType());
        type.addListener((observable, oldType, newType) -> updateTypeStyleClass(oldType, newType));
        setAlignment(Pos.CENTER);
        sceneBuilderIntegration();
    }

    private void updateTypeStyleClass(EleButtonType oldType, EleButtonType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }
        getStyleClass().add((newType == null ? EleButtonType.DEFAULT : newType).styleClass());
    }
}
