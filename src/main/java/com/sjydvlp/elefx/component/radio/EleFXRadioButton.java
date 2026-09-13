package com.sjydvlp.elefx.component.radio;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/** A radio rendered as an Element Plus segmented button. */
public class EleFXRadioButton<T> extends EleFXRadio<T> {

    private static final String STYLE_CLASS = "ele-radio-button";

    public EleFXRadioButton() {
        initializeButtonStyle();
    }

    public EleFXRadioButton(String text) {
        super(text);
        initializeButtonStyle();
    }

    public EleFXRadioButton(String text, T value) {
        super(text, value);
        initializeButtonStyle();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RadioButtonSkin<>(this);
    }

    private void initializeButtonStyle() {
        if (!getStyleClass().contains(STYLE_CLASS)) getStyleClass().add(STYLE_CLASS);
    }

    private static final class RadioButtonSkin<T> extends LabeledSkinBase<EleFXRadioButton<T>> {

        private Node textNode;

        private final EventHandler<MouseEvent> mouseHandler = event -> {
            EleFXRadioButton<T> radio = getSkinnable();
            if (event.getButton() == MouseButton.PRIMARY && !radio.isDisable()) {
                radio.fire();
                event.consume();
            }
        };

        private final EventHandler<KeyEvent> keyHandler = event -> {
            EleFXRadioButton<T> radio = getSkinnable();
            if (event.getCode() == KeyCode.SPACE && !radio.isDisable()) {
                radio.fire();
                event.consume();
            }
        };

        private RadioButtonSkin(EleFXRadioButton<T> control) {
            super(control);
            control.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
            control.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        }

        @Override
        protected void layoutChildren(double x, double y, double width, double height) {
            layoutLabelInArea(x, y, width, height, Pos.CENTER);
            if (textNode == null) textNode = getChildren().stream()
                    .filter(node -> node.getStyleClass().contains("text")).findFirst().orElse(null);
            if (textNode != null) {
                double center = textNode.getLayoutX() + textNode.getLayoutBounds().getMinX()
                        + textNode.getLayoutBounds().getWidth() / 2d;
                textNode.setTranslateX(x + width / 2d - center);
            }
        }

        @Override
        public void dispose() {
            EleFXRadioButton<T> radio = getSkinnable();
            radio.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseHandler);
            radio.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
            super.dispose();
        }
    }
}
