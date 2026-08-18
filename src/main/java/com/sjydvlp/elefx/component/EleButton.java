package com.sjydvlp.elefx.component;

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

    public EleButton() {
        super("Button");
        initialize();
    }

    public EleButton(String text) {
        super(text);
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
        setAlignment(Pos.CENTER);
        sceneBuilderIntegration();
    }
}
