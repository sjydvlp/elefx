package com.sjydvlp.elefx.component.button;

import com.sjydvlp.elefx.theme.EleFXThemes;
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
public class EleFXButton extends Button implements Themable {

    private static final String STYLE_CLASS = "ele-button";

    private static final String CIRCLE_STYLE_CLASS = "ele-button--circle";

    private final ObjectProperty<EleFXButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFXButtonType.DEFAULT);

    private final BooleanProperty circle = new SimpleBooleanProperty(this, "circle", false);

    public EleFXButton() {
        super("Button");
        initialize();
    }

    public EleFXButton(String text) {
        super(text);
        initialize();
    }

    public EleFXButton(String text, EleFXButtonType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFXButton(String text, double prefWidth, double prefHeight) {
        super(text);
        setPrefSize(prefWidth, prefHeight);
        initialize();
    }

    public EleFXButton(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    public EleFXButton(String text, Node icon, EleFXButtonType type) {
        super(text, icon);
        setType(type);
        initialize();
    }

    public EleFXButton(Node icon) {
        super(null, icon);
        initialize();
    }

    public EleFXButton(Node icon, EleFXButtonType type) {
        super(null, icon);
        setType(type);
        initialize();
    }

    public EleFXButton(Node icon, EleFXButtonType type, boolean circle) {
        super(null, icon);
        setType(type);
        setCircle(circle);
        initialize();
    }

    public EleFXButtonType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXButtonType> typeProperty() {
        return type;
    }

    public void setType(EleFXButtonType type) {
        this.type.set(type == null ? EleFXButtonType.DEFAULT : type);
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
        return EleFXThemes.BUTTON;
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

    private void updateTypeStyleClass(EleFXButtonType oldType, EleFXButtonType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }
        getStyleClass().add((newType == null ? EleFXButtonType.DEFAULT : newType).styleClass());
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
