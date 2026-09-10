package com.sjydvlp.elefx.component.checkbox;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/** A checkbox rendered as an Element Plus style toggle button. */
public class EleFXCheckboxButton<T> extends EleFXCheckbox<T> {

    private static final String STYLE_CLASS = "ele-checkbox-button";

    public EleFXCheckboxButton() {
        initializeButtonStyle();
    }

    public EleFXCheckboxButton(String text) {
        super(text);
        initializeButtonStyle();
    }

    public EleFXCheckboxButton(String text, T value) {
        super(text, value);
        initializeButtonStyle();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CheckboxButtonSkin<>(this);
    }

    private void initializeButtonStyle() {
        if (!getStyleClass().contains(STYLE_CLASS)) getStyleClass().add(STYLE_CLASS);
    }

    /** A label-only skin: checkbox-button has no hidden checkbox box to offset its text. */
    private static final class CheckboxButtonSkin<T> extends LabeledSkinBase<EleFXCheckboxButton<T>> {

        private Node textNode;

        private final EventHandler<MouseEvent> mouseClickHandler = event -> {
            EleFXCheckboxButton<T> control = getSkinnable();
            if (event.getButton() == MouseButton.PRIMARY && !control.isDisable()) {
                control.fire();
                event.consume();
            }
        };

        private final EventHandler<KeyEvent> keyPressHandler = event -> {
            EleFXCheckboxButton<T> control = getSkinnable();
            if (event.getCode() == KeyCode.SPACE && !control.isDisable()) {
                control.fire();
                event.consume();
            }
        };

        private CheckboxButtonSkin(EleFXCheckboxButton<T> control) {
            super(control);
            control.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickHandler);
            control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
        }

        @Override
        protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
            layoutLabelInArea(contentX, contentY, contentWidth, contentHeight, Pos.CENTER);
            centerTextNode(contentX, contentWidth);
        }

        private void centerTextNode(double contentX, double contentWidth) {
            if (textNode == null) {
                textNode = getChildren().stream()
                        .filter(node -> node.getStyleClass().contains("text"))
                        .findFirst()
                        .orElse(null);
            }
            if (textNode == null) return;

            double textCenter = textNode.getLayoutX() + textNode.getLayoutBounds().getMinX()
                    + textNode.getLayoutBounds().getWidth() / 2d;
            textNode.setTranslateX(contentX + contentWidth / 2d - textCenter);
        }

        @Override
        public void dispose() {
            EleFXCheckboxButton<T> control = getSkinnable();
            control.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickHandler);
            control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
            super.dispose();
        }
    }
}
