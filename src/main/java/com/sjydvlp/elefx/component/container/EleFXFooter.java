package com.sjydvlp.elefx.component.container;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/** Footer region for an {@link EleFXContainer}. */
public class EleFXFooter extends Pane implements Themable {

    public EleFXFooter() {
        initialize();
    }

    public EleFXFooter(Node... children) {
        this();
        getChildren().addAll(children);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CONTAINER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-footer");
        setPrefHeight(60);
        sceneBuilderIntegration();
    }
}
