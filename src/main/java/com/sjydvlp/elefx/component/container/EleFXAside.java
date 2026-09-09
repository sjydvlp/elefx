package com.sjydvlp.elefx.component.container;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/** Side region for an {@link EleFXContainer}. Set its preferred width to control its size. */
public class EleFXAside extends Pane implements Themable {

    public EleFXAside() {
        initialize();
    }

    public EleFXAside(Node... children) {
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
        getStyleClass().add("ele-aside");
        setPrefWidth(300);
        sceneBuilderIntegration();
    }
}
