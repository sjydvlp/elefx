package com.sjydvlp.elefx.button;

import com.sjydvlp.elefx.theme.EleFxTheme;
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
public class EleFxButton extends Button implements Themable {

    private static final String STYLE_CLASS = "ele-button";

    private final ObjectProperty<EleFxButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFxButtonType.DEFAULT);

    public EleFxButton() {
        super("Button");
        initialize();
    }

    public EleFxButton(String text) {
        super(text);
        initialize();
    }

    public EleFxButton(String text, EleFxButtonType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFxButton(String text, double prefWidth, double prefHeight) {
        super(text);
        setPrefSize(prefWidth, prefHeight);
        initialize();
    }

    public EleFxButton(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    public EleFxButtonType getType() {
        return type.get();
    }

    public ObjectProperty<EleFxButtonType> typeProperty() {
        return type;
    }

    public void setType(EleFxButtonType type) {
        this.type.set(type == null ? EleFxButtonType.DEFAULT : type);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFxTheme.BUTTON;
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

    private void updateTypeStyleClass(EleFxButtonType oldType, EleFxButtonType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }
        getStyleClass().add((newType == null ? EleFxButtonType.DEFAULT : newType).styleClass());
    }
}
