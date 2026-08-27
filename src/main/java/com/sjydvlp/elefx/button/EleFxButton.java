package com.sjydvlp.elefx.button;

import com.sjydvlp.elefx.theme.EleFxTheme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static final String CIRCLE_STYLE_CLASS = "ele-button--circle";

    private final ObjectProperty<EleFxButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFxButtonType.DEFAULT);

    private final BooleanProperty circle = new SimpleBooleanProperty(this, "circle", false);

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

    public EleFxButton(String text, Node icon, EleFxButtonType type) {
        super(text, icon);
        setType(type);
        initialize();
    }

    public EleFxButton(Node icon) {
        super(null, icon);
        initialize();
    }

    public EleFxButton(Node icon, EleFxButtonType type) {
        super(null, icon);
        setType(type);
        initialize();
    }

    public EleFxButton(Node icon, EleFxButtonType type, boolean circle) {
        super(null, icon);
        setType(type);
        setCircle(circle);
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

    public boolean isCircle() {
        return circle.get();
    }

    public BooleanProperty circleProperty() {
        return circle;
    }

    public void setCircle(boolean circle) {
        this.circle.set(circle);
    }

    public Node getIcon() {
        return getGraphic();
    }

    public ObjectProperty<Node> iconProperty() {
        return graphicProperty();
    }

    public void setIcon(Node icon) {
        setGraphic(icon);
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
        updateCircleStyleClass(isCircle());
        circle.addListener((observable, oldCircle, newCircle) -> updateCircleStyleClass(newCircle));
        setAlignment(Pos.CENTER);
        sceneBuilderIntegration();
    }

    private void updateTypeStyleClass(EleFxButtonType oldType, EleFxButtonType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }
        getStyleClass().add((newType == null ? EleFxButtonType.DEFAULT : newType).styleClass());
    }

    private void updateCircleStyleClass(boolean circle) {
        if (circle) {
            if (!getStyleClass().contains(CIRCLE_STYLE_CLASS)) {
                getStyleClass().add(CIRCLE_STYLE_CLASS);
            }
        } else {
            getStyleClass().remove(CIRCLE_STYLE_CLASS);
        }
    }
}
