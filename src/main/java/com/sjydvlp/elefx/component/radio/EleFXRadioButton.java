package com.sjydvlp.elefx.component.radio;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

/** A radio rendered as an Element Plus segmented button. */
public class EleFXRadioButton<T> extends EleFXRadio<T> {

    private static final String STYLE_CLASS = "ele-radio-button";

    private static final PseudoClass KEYBOARD_FOCUS = PseudoClass.getPseudoClass("keyboard-focus");

    private static final Object INPUT_MODE_KEY = new Object();

    private final ChangeListener<Boolean> inputModeChanged = (observable, previous,
                                                              keyboard) -> pseudoClassStateChanged(KEYBOARD_FOCUS,
                                                                      keyboard);

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
        sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null) {
                keyboardInput(previous).removeListener(inputModeChanged);
            }
            if (current == null) {
                pseudoClassStateChanged(KEYBOARD_FOCUS, false);
            } else {
                BooleanProperty input = keyboardInput(current);
                input.addListener(inputModeChanged);
                pseudoClassStateChanged(KEYBOARD_FOCUS, input.get());
            }
        });
    }

    private static BooleanProperty keyboardInput(Scene scene) {
        return (BooleanProperty) scene.getProperties().computeIfAbsent(INPUT_MODE_KEY, key -> {
            var keyboard = new SimpleBooleanProperty();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> keyboard.set(true));
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> keyboard.set(false));
            scene.addEventFilter(TouchEvent.TOUCH_PRESSED, event -> keyboard.set(false));
            return keyboard;
        });
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
