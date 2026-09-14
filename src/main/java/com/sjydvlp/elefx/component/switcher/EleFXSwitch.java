package com.sjydvlp.elefx.component.switcher;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

/**
 * A JavaFX switch styled after Element Plus.
 *
 * <p>
 * The {@link #selectedProperty()} is the Boolean equivalent of Element
 * Plus's {@code v-model}. Active/inactive text can be placed alongside the
 * control or inside its track with {@link #setInlinePrompt(boolean)}.
 * </p>
 */
public class EleFXSwitch extends HBox implements Themable {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private static final PseudoClass INLINE_PROMPT = PseudoClass.getPseudoClass("inline-prompt");

    private final Label activeLabel = new Label();

    private final StackPane track = new StackPane();

    private final Label prompt = new Label();

    private final StackPane thumb = new StackPane();

    private final Label inactiveLabel = new Label();

    private final TranslateTransition thumbTransition = new TranslateTransition(Duration.millis(200), thumb);

    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected", false);

    private final BooleanProperty inlinePrompt = new SimpleBooleanProperty(this, "inlinePrompt", false);

    private final DoubleProperty switchWidth = new SimpleDoubleProperty(this, "switchWidth", -1);

    private final ObjectProperty<EleFXSwitchSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXSwitchSize.DEFAULT);

    private final StringProperty activeText = new SimpleStringProperty(this, "activeText", "");

    private final StringProperty inactiveText = new SimpleStringProperty(this, "inactiveText", "");

    private final ObjectProperty<Node> activeAction = new SimpleObjectProperty<>(this, "activeAction");

    private final ObjectProperty<Node> inactiveAction = new SimpleObjectProperty<>(this, "inactiveAction");

    private final ObjectProperty<Node> activeIcon = new SimpleObjectProperty<>(this, "activeIcon");

    private final ObjectProperty<Node> inactiveIcon = new SimpleObjectProperty<>(this, "inactiveIcon");

    private final ObjectProperty<Paint> activeColor = new SimpleObjectProperty<>(this, "activeColor",
            Color.web("#409eff"));

    private final ObjectProperty<Paint> inactiveColor = new SimpleObjectProperty<>(this, "inactiveColor",
            Color.web("#dcdfe6"));

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "Switch");

    private final ObjectProperty<EventHandler<EleFXSwitchEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    public EleFXSwitch() {
        getStyleClass().add("ele-switch");
        activeLabel.getStyleClass().add("ele-switch__label");
        activeLabel.getStyleClass().add("ele-switch__label--active");
        inactiveLabel.getStyleClass().add("ele-switch__label");
        inactiveLabel.getStyleClass().add("ele-switch__label--inactive");
        track.getStyleClass().add("ele-switch__core");
        prompt.getStyleClass().add("ele-switch__prompt");
        thumb.getStyleClass().add("ele-switch__action");
        track.getChildren().addAll(prompt, thumb);
        // Keep Element Plus's fixed text order: inactive-text [switch]
        // active-text. State changes only the highlighted label.
        getChildren().addAll(inactiveLabel, track, activeLabel);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.CHECK_BOX);
        thumbTransition.setInterpolator(Interpolator.EASE_BOTH);

        selected.addListener((o, oldValue, newValue) -> valueChanged(oldValue, newValue));
        inlinePrompt.addListener((o, oldValue, newValue) -> updateContent());
        size.addListener((o, oldValue, newValue) -> updateSize());
        switchWidth.addListener((o, oldValue, newValue) -> updateSize());
        activeText.addListener((o, oldValue, newValue) -> updateContent());
        inactiveText.addListener((o, oldValue, newValue) -> updateContent());
        activeAction.addListener((o, oldValue, newValue) -> updateContent());
        inactiveAction.addListener((o, oldValue, newValue) -> updateContent());
        activeIcon.addListener((o, oldValue, newValue) -> updateContent());
        inactiveIcon.addListener((o, oldValue, newValue) -> updateContent());
        activeColor.addListener((o, oldValue, newValue) -> updateColors());
        inactiveColor.addListener((o, oldValue, newValue) -> updateColors());
        ariaLabel.addListener((o, oldValue, newValue) -> updateAccessibleText());
        track.widthProperty().addListener((o, oldValue, newValue) -> updateThumbPosition(false));
        track.heightProperty().addListener((o, oldValue, newValue) -> updateThumbPosition(false));
        setOnMouseClicked(this::handleMouseClick);
        setOnKeyPressed(this::handleKeyPressed);

        updateSize();
        updateContent();
        updateColors();
        updateSelectedStyle();
        updateThumbPosition(false);
        updateAccessibleText();
        sceneBuilderIntegration();
    }

    public EleFXSwitch(boolean selected) {
        this();
        setSelected(selected);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    /** Alias for users who prefer a model-value naming convention. */
    public boolean isValue() {
        return isSelected();
    }

    public void setValue(boolean value) {
        setSelected(value);
    }

    public BooleanProperty valueProperty() {
        return selectedProperty();
    }

    public boolean isInlinePrompt() {
        return inlinePrompt.get();
    }

    public void setInlinePrompt(boolean value) {
        inlinePrompt.set(value);
    }

    public BooleanProperty inlinePromptProperty() {
        return inlinePrompt;
    }

    /** Explicit track width; a negative value uses the selected size's default. */
    public double getSwitchWidth() {
        return switchWidth.get();
    }

    public void setSwitchWidth(double value) {
        if (!Double.isFinite(value) || value == 0 || value < -1)
            throw new IllegalArgumentException("switchWidth must be positive or -1");
        switchWidth.set(value);
    }

    public DoubleProperty switchWidthProperty() {
        return switchWidth;
    }

    public EleFXSwitchSize getSize() {
        return size.get();
    }

    public void setSize(EleFXSwitchSize value) {
        size.set(value == null ? EleFXSwitchSize.DEFAULT : value);
    }

    public ObjectProperty<EleFXSwitchSize> sizeProperty() {
        return size;
    }

    public String getActiveText() {
        return activeText.get();
    }

    public void setActiveText(String value) {
        activeText.set(value == null ? "" : value);
    }

    public StringProperty activeTextProperty() {
        return activeText;
    }

    public String getInactiveText() {
        return inactiveText.get();
    }

    public void setInactiveText(String value) {
        inactiveText.set(value == null ? "" : value);
    }

    public StringProperty inactiveTextProperty() {
        return inactiveText;
    }

    /** Node displayed in the thumb while the switch is active. */
    public Node getActiveAction() {
        return activeAction.get();
    }

    public void setActiveAction(Node value) {
        activeAction.set(value);
    }

    public ObjectProperty<Node> activeActionProperty() {
        return activeAction;
    }

    /** Node displayed in the thumb while the switch is inactive. */
    public Node getInactiveAction() {
        return inactiveAction.get();
    }

    public void setInactiveAction(Node value) {
        inactiveAction.set(value);
    }

    public ObjectProperty<Node> inactiveActionProperty() {
        return inactiveAction;
    }

    /**
     * Icon displayed for the on state. It replaces {@link #getActiveText()}.
     * With inline prompt enabled, the icon is rendered inside the switch track.
     */
    public Node getActiveIcon() {
        return activeIcon.get();
    }

    public void setActiveIcon(Node value) {
        activeIcon.set(value);
    }

    public ObjectProperty<Node> activeIconProperty() {
        return activeIcon;
    }

    /**
     * Icon displayed for the off state. It replaces {@link #getInactiveText()}.
     * With inline prompt enabled, the icon is rendered inside the switch track.
     */
    public Node getInactiveIcon() {
        return inactiveIcon.get();
    }

    public void setInactiveIcon(Node value) {
        inactiveIcon.set(value);
    }

    public ObjectProperty<Node> inactiveIconProperty() {
        return inactiveIcon;
    }

    public Paint getActiveColor() {
        return activeColor.get();
    }

    public void setActiveColor(Paint value) {
        activeColor.set(value == null ? Color.web("#409eff") : value);
    }

    public ObjectProperty<Paint> activeColorProperty() {
        return activeColor;
    }

    public Paint getInactiveColor() {
        return inactiveColor.get();
    }

    public void setInactiveColor(Paint value) {
        inactiveColor.set(value == null ? Color.web("#dcdfe6") : value);
    }

    public ObjectProperty<Paint> inactiveColorProperty() {
        return inactiveColor;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String value) {
        ariaLabel.set(value == null ? "Switch" : value);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public EventHandler<EleFXSwitchEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXSwitchEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXSwitchEvent>> onChangeProperty() {
        return onChange;
    }

    /** Toggles the value unless this switch is disabled. */
    public void toggle() {
        if (!isDisabled()) setSelected(!isSelected());
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SWITCH;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void handleMouseClick(MouseEvent event) {
        if (!isDisabled()) {
            requestFocus();
            toggle();
        }
        event.consume();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
            toggle();
            event.consume();
        }
    }

    private void valueChanged(boolean oldValue, boolean newValue) {
        updateSelectedStyle();
        updateContent();
        updateThumbPosition(true);
        updateColors();
        updateAccessibleText();
        if (oldValue != newValue) {
            EleFXSwitchEvent event = new EleFXSwitchEvent(this, this, oldValue, newValue);
            EventHandler<EleFXSwitchEvent> handler = getOnChange();
            if (handler != null) handler.handle(event);
            fireEvent(event);
        }
    }

    private void updateSize() {
        getStyleClass().removeIf(styleClass -> styleClass.startsWith("ele-switch--size-"));
        EleFXSwitchSize resolved = getSize() == null ? EleFXSwitchSize.DEFAULT : getSize();
        getStyleClass().add(resolved.styleClass());
        double trackWidth = getSwitchWidth() < 0 ? resolved.width() : Math.max(getSwitchWidth(), resolved.height());
        track.setMinSize(trackWidth, resolved.height());
        track.setPrefSize(trackWidth, resolved.height());
        track.setMaxSize(trackWidth, resolved.height());
        thumb.setMinSize(resolved.thumbSize(), resolved.thumbSize());
        thumb.setPrefSize(resolved.thumbSize(), resolved.thumbSize());
        thumb.setMaxSize(resolved.thumbSize(), resolved.thumbSize());
        updateThumbPosition(false);
    }

    private void updateContent() {
        boolean inline = isInlinePrompt();
        pseudoClassStateChanged(INLINE_PROMPT, inline);
        // Clear graphics before changing their parent between the external
        // labels and the in-track prompt.
        prompt.setGraphic(null);
        activeLabel.setGraphic(null);
        inactiveLabel.setGraphic(null);
        updateStateLabel(activeLabel, getActiveText(), getActiveIcon(), !inline);
        updateStateLabel(inactiveLabel, getInactiveText(), getInactiveIcon(), !inline);
        activeLabel.setVisible(!inline && (getActiveIcon() != null || !getActiveText().isEmpty()));
        activeLabel.setManaged(activeLabel.isVisible());
        inactiveLabel.setVisible(!inline && (getInactiveIcon() != null || !getInactiveText().isEmpty()));
        inactiveLabel.setManaged(inactiveLabel.isVisible());
        Node icon = isSelected() ? getActiveIcon() : getInactiveIcon();
        String text = isSelected() ? getActiveText() : getInactiveText();
        prompt.setText(inline && icon == null ? text : "");
        prompt.setGraphic(inline ? icon : null);
        prompt.setContentDisplay(icon == null ? ContentDisplay.TEXT_ONLY : ContentDisplay.GRAPHIC_ONLY);
        prompt.setVisible(inline && (icon != null || !prompt.getText().isEmpty()));
        prompt.setManaged(prompt.isVisible());
        StackPane.setAlignment(prompt, isSelected() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        thumb.getChildren().setAll();
        Node action = isSelected() ? getActiveAction() : getInactiveAction();
        if (action != null) thumb.getChildren().add(action);
        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);
    }

    private void updateStateLabel(Label label, String text, Node icon, boolean visible) {
        label.setText(icon == null ? text : "");
        label.setGraphic(visible ? icon : null);
        label.setContentDisplay(icon == null ? ContentDisplay.TEXT_ONLY : ContentDisplay.GRAPHIC_ONLY);
    }

    /** Moves the thumb without changing the track's layout bounds. */
    private void updateThumbPosition(boolean animated) {
        double trackWidth = track.getWidth() > 0 ? track.getWidth() : track.getPrefWidth();
        double thumbWidth = thumb.getWidth() > 0 ? thumb.getWidth() : thumb.getPrefWidth();
        double target = isSelected() ? Math.max(0, trackWidth - thumbWidth - 4) : 0;
        thumbTransition.stop();
        if (!animated || Math.abs(thumb.getTranslateX() - target) < .01) {
            thumb.setTranslateX(target);
            return;
        }
        thumbTransition.setFromX(thumb.getTranslateX());
        thumbTransition.setToX(target);
        thumbTransition.playFromStart();
    }

    private void updateColors() {
        Paint paint = isSelected() ? getActiveColor() : getInactiveColor();
        track.setStyle("-fx-background-color: " + cssPaint(paint) + ";");
    }

    private String cssPaint(Paint paint) {
        if (paint instanceof Color color) {
            return String.format("#%02x%02x%02x", Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255),
                    Math.round(color.getBlue() * 255));
        }
        return paint.toString();
    }

    private void updateSelectedStyle() {
        pseudoClassStateChanged(SELECTED, isSelected());
    }

    private void updateAccessibleText() {
        String state = isSelected() ? "on" : "off";
        setAccessibleText(getAriaLabel().isBlank() ? state : getAriaLabel() + ", " + state);
    }
}
