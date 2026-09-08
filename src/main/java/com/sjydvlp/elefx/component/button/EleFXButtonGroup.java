package com.sjydvlp.elefx.component.button;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.util.List;

/**
 * A contiguous horizontal or vertical group of related buttons.
 *
 * <p>
 * The group only owns layout and joined-edge styling. Buttons retain their
 * own type, size and action, which keeps a button reusable outside the group.
 * </p>
 */
public class EleFXButtonGroup extends FlowPane implements Themable {

    private static final String STYLE_CLASS = "ele-button-group";

    private static final String HORIZONTAL_FIRST_STYLE_CLASS = "ele-button-group__horizontal-first";

    private static final String HORIZONTAL_LAST_STYLE_CLASS = "ele-button-group__horizontal-last";

    private static final String VERTICAL_FIRST_STYLE_CLASS = "ele-button-group__vertical-first";

    private static final String VERTICAL_LAST_STYLE_CLASS = "ele-button-group__vertical-last";

    public EleFXButtonGroup() {
        initialize();
    }

    public EleFXButtonGroup(Node... children) {
        this();
        getChildren().addAll(children);
    }

    /**
     * Returns the direction in which group members are arranged.
     */
    public Orientation getDirection() {
        return getOrientation();
    }

    /**
     * Property alias for {@link #orientationProperty()} that expresses the
     * Button Group API without introducing a duplicate JavaFX property.
     */
    public ObjectProperty<Orientation> directionProperty() {
        return orientationProperty();
    }

    /**
     * Uses JavaFX's {@link Orientation} rather than a component-specific enum.
     * A {@code null} direction is treated as horizontal.
     */
    public void setDirection(Orientation direction) {
        setOrientation(direction == null ? Orientation.HORIZONTAL : direction);
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
        setHgap(0);
        setVgap(0);
        updateDirectionStyleClass(null, getDirection());
        orientationProperty()
                .addListener((observable, oldValue, newValue) -> updateDirectionStyleClass(oldValue, newValue));
        getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> refreshEdgeStyleClasses());
        refreshEdgeStyleClasses();
        sceneBuilderIntegration();
    }

    private void updateDirectionStyleClass(Orientation oldValue, Orientation newValue) {
        if (oldValue != null) {
            getStyleClass().remove(styleClass(oldValue));
        }
        String styleClass = styleClass(newValue == null ? Orientation.HORIZONTAL : newValue);
        if (!getStyleClass().contains(styleClass)) {
            getStyleClass().add(styleClass);
        }
        refreshEdgeStyleClasses();
    }

    private String styleClass(Orientation direction) {
        return "ele-button-group--" + direction.name().toLowerCase();
    }

    private void refreshEdgeStyleClasses() {
        List<Node> buttons = getManagedChildren().stream()
                .filter(EleFXButton.class::isInstance)
                .toList();
        for (Node button : buttons) {
            button.getStyleClass().removeAll(HORIZONTAL_FIRST_STYLE_CLASS, HORIZONTAL_LAST_STYLE_CLASS,
                    VERTICAL_FIRST_STYLE_CLASS, VERTICAL_LAST_STYLE_CLASS);
        }
        if (buttons.isEmpty()) {
            return;
        }
        boolean horizontal = getDirection() != Orientation.VERTICAL;
        buttons.get(0).getStyleClass().add(horizontal
                ? HORIZONTAL_FIRST_STYLE_CLASS
                : VERTICAL_FIRST_STYLE_CLASS);
        if (buttons.size() > 1) {
            buttons.get(buttons.size() - 1).getStyleClass().add(horizontal
                    ? HORIZONTAL_LAST_STYLE_CLASS
                    : VERTICAL_LAST_STYLE_CLASS);
        }
    }
}
