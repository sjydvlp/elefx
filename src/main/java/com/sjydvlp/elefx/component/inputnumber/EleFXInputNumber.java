package com.sjydvlp.elefx.component.inputnumber;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Element Plus inspired input for nullable numerical values.
 *
 * <p>
 * The control accepts direct input, arrow keys, and increment/decrement buttons.
 * Invalid text produces a {@code null} value until it is corrected or the field loses
 * focus. Values are clamped to the configured range when committed.
 * </p>
 */
public class EleFXInputNumber extends HBox implements Themable {

    public static final double MIN_SAFE_INTEGER = -9007199254740991d;

    public static final double MAX_SAFE_INTEGER = 9007199254740991d;

    private final TextField input = new TextField();

    private final EleFXIcon defaultDecreaseIcon = new EleFXIcon(EleFXIconType.MINUS, 14);

    private final EleFXIcon defaultIncreaseIcon = new EleFXIcon(EleFXIconType.PLUS, 14);

    private final Button decreaseButton = button(defaultDecreaseIcon, "ele-input-number__decrease");

    private final Button increaseButton = button(defaultIncreaseIcon, "ele-input-number__increase");

    private final StackPane rightControls = new StackPane();

    private final HBox rightAdornment = new HBox();

    private final StackPane prefixAdornment = new StackPane();

    private final StackPane suffixAdornment = new StackPane();

    private final BorderPane inputArea = new BorderPane();

    private final ObjectProperty<Double> value = new SimpleObjectProperty<>(this, "value", 1d);

    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", MIN_SAFE_INTEGER);

    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", MAX_SAFE_INTEGER);

    private final DoubleProperty step = new SimpleDoubleProperty(this, "step", 1);

    private final BooleanProperty stepStrictly = new SimpleBooleanProperty(this, "stepStrictly", false);

    private final IntegerProperty precision = new SimpleIntegerProperty(this, "precision", -1);

    private final ObjectProperty<EleFXInputNumberSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXInputNumberSize.DEFAULT);

    private final BooleanProperty readOnly = new SimpleBooleanProperty(this, "readOnly", false);

    private final BooleanProperty controls = new SimpleBooleanProperty(this, "controls", true);

    private final ObjectProperty<EleFXInputNumberControlsPosition> controlsPosition = new SimpleObjectProperty<>(this,
            "controlsPosition", EleFXInputNumberControlsPosition.DEFAULT);

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "");

    private final ObjectProperty<Double> valueOnClear = new SimpleObjectProperty<>(this, "valueOnClear");

    private final ObjectProperty<EleFXInputNumberValueOnClear> valueOnClearMode = new SimpleObjectProperty<>(this,
            "valueOnClearMode");

    private final BooleanProperty validateEvent = new SimpleBooleanProperty(this, "validateEvent", true);

    private final StringProperty inputMode = new SimpleStringProperty(this, "inputMode", "decimal");

    private final ObjectProperty<Pos> align = new SimpleObjectProperty<>(this, "align", Pos.CENTER);

    private final BooleanProperty disabledScientific = new SimpleBooleanProperty(this, "disabledScientific", false);

    private final ObjectProperty<Function<Double, String>> formatter = new SimpleObjectProperty<>(this, "formatter");

    private final ObjectProperty<Function<String, String>> parser = new SimpleObjectProperty<>(this, "parser");

    private final ObjectProperty<Node> prefix = new SimpleObjectProperty<>(this, "prefix");

    private final ObjectProperty<Node> suffix = new SimpleObjectProperty<>(this, "suffix");

    private final ObjectProperty<Node> decreaseIcon = new SimpleObjectProperty<>(this, "decreaseIcon");

    private final ObjectProperty<Node> increaseIcon = new SimpleObjectProperty<>(this, "increaseIcon");

    private final ObjectProperty<EventHandler<EleFXInputNumberEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final ObjectProperty<EventHandler<javafx.event.Event>> onFocus = new SimpleObjectProperty<>(this,
            "onFocus");

    private final ObjectProperty<EventHandler<javafx.event.Event>> onBlur = new SimpleObjectProperty<>(this, "onBlur");

    private boolean updating;

    private boolean parsing;

    private final EventHandler<MouseEvent> outsideClickHandler = this::handleSceneMousePressed;

    public EleFXInputNumber() {
        getStyleClass().add("ele-input-number");
        input.getStyleClass().add("ele-input-number__input");
        input.setAlignment(getAlign());
        inputArea.getStyleClass().add("ele-input-number__input-wrapper");
        rightControls.getStyleClass().add("ele-input-number__right-controls");
        rightAdornment.getStyleClass().add("ele-input-number__right-adornment");
        rightAdornment.setAlignment(Pos.CENTER_RIGHT);
        prefixAdornment.getStyleClass().add("ele-input-number__prefix-adornment");
        prefixAdornment.setAlignment(Pos.CENTER_LEFT);
        suffixAdornment.getStyleClass().add("ele-input-number__suffix-adornment");
        suffixAdornment.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(inputArea, Priority.ALWAYS);
        setMinWidth(180);
        setPrefWidth(180);

        input.textProperty().addListener((o, oldText, text) -> parseInput(text));
        input.focusedProperty().addListener((o, oldFocus, focused) -> {
            pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("focused"), focused);
            if (focused)
                fireSimpleEvent(onFocus.get());
            else {
                commitInput();
                fireSimpleEvent(onBlur.get());
            }
        });
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                increment();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                decrement();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                commitInput();
                event.consume();
            }
        });
        setOnMousePressed(event -> {
            if (!isDisabled()) input.requestFocus();
        });
        decreaseButton.setOnAction(e -> {
            input.requestFocus();
            decrement();
        });
        increaseButton.setOnAction(e -> {
            input.requestFocus();
            increment();
        });
        value.addListener((o, oldValue, newValue) -> valueChanged(oldValue, newValue));
        min.addListener((o, oldValue, newValue) -> rangeChanged());
        max.addListener((o, oldValue, newValue) -> rangeChanged());
        step.addListener((o, oldValue, newValue) -> validateStep(newValue.doubleValue()));
        precision.addListener((o, oldValue, newValue) -> precisionChanged(newValue.intValue()));
        readOnly.addListener((o, oldValue, newValue) -> input.setEditable(!newValue));
        disableProperty().addListener((o, oldValue, newValue) -> updateButtonState());
        controls.addListener((o, oldValue, newValue) -> rebuild());
        controlsPosition.addListener((o, oldValue, newValue) -> rebuild());
        size.addListener((o, oldValue, newValue) -> updateSizeStyle(oldValue, newValue));
        align.addListener((o, oldValue, newValue) -> input.setAlignment(newValue == null ? Pos.CENTER : newValue));
        prefix.addListener((o, oldValue, newValue) -> rebuild());
        suffix.addListener((o, oldValue, newValue) -> rebuild());
        decreaseIcon.addListener((o, oldValue, newValue) -> updateControlIcons());
        increaseIcon.addListener((o, oldValue, newValue) -> updateControlIcons());
        ariaLabel.addListener((o, oldValue, newValue) -> input.setAccessibleText(newValue));
        sceneProperty().addListener((o, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
        });
        updateSizeStyle(null, getSize());
        updateInputText();
        rebuild();
        sceneBuilderIntegration();
    }

    public EleFXInputNumber(double value) {
        this();
        setValue(value);
    }

    public Double getValue() {
        return value.get();
    }

    public void setValue(Double value) {
        this.value.set(normalize(value));
    }

    public ObjectProperty<Double> valueProperty() {
        return value;
    }

    public double getMin() {
        return min.get();
    }

    public void setMin(double value) {
        validateSafe(value, "min");
        if (value > getMax()) throw new IllegalArgumentException("min must not exceed max");
        min.set(value);
    }

    public DoubleProperty minProperty() {
        return min;
    }

    public double getMax() {
        return max.get();
    }

    public void setMax(double value) {
        validateSafe(value, "max");
        if (value < getMin()) throw new IllegalArgumentException("max must not be less than min");
        max.set(value);
    }

    public DoubleProperty maxProperty() {
        return max;
    }

    public double getStep() {
        return step.get();
    }

    public void setStep(double value) {
        validateStep(value);
        if (getPrecision() >= 0 && getPrecision() < decimalPlaces(value))
            throw new IllegalArgumentException("precision must not be less than the decimal places of step");
        step.set(value);
    }

    public DoubleProperty stepProperty() {
        return step;
    }

    public boolean isStepStrictly() {
        return stepStrictly.get();
    }

    public void setStepStrictly(boolean value) {
        stepStrictly.set(value);
        rangeChanged();
    }

    public BooleanProperty stepStrictlyProperty() {
        return stepStrictly;
    }

    public int getPrecision() {
        return precision.get();
    }

    /** {@code -1} uses the precision implied by the entered value and step. */
    public void setPrecision(int value) {
        validatePrecision(value);
        precision.set(value);
    }

    public IntegerProperty precisionProperty() {
        return precision;
    }

    public EleFXInputNumberSize getSize() {
        return size.get();
    }

    public void setSize(EleFXInputNumberSize value) {
        size.set(value == null ? EleFXInputNumberSize.DEFAULT : value);
    }

    public ObjectProperty<EleFXInputNumberSize> sizeProperty() {
        return size;
    }

    public boolean isReadOnly() {
        return readOnly.get();
    }

    public void setReadOnly(boolean value) {
        readOnly.set(value);
    }

    public BooleanProperty readOnlyProperty() {
        return readOnly;
    }

    public boolean isControls() {
        return controls.get();
    }

    public void setControls(boolean value) {
        controls.set(value);
    }

    public BooleanProperty controlsProperty() {
        return controls;
    }

    public EleFXInputNumberControlsPosition getControlsPosition() {
        return controlsPosition.get();
    }

    public void setControlsPosition(EleFXInputNumberControlsPosition value) {
        controlsPosition.set(value == null ? EleFXInputNumberControlsPosition.DEFAULT : value);
    }

    public ObjectProperty<EleFXInputNumberControlsPosition> controlsPositionProperty() {
        return controlsPosition;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value == null ? "" : value);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String value) {
        ariaLabel.set(value == null ? "" : value);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public String getPlaceholder() {
        return input.getPromptText();
    }

    public void setPlaceholder(String value) {
        input.setPromptText(value);
    }

    public StringProperty placeholderProperty() {
        return input.promptTextProperty();
    }

    public ObjectProperty<Double> valueOnClearProperty() {
        return valueOnClear;
    }

    public Double getValueOnClear() {
        return valueOnClear.get();
    }

    public void setValueOnClear(Double value) {
        valueOnClearMode.set(null);
        valueOnClear.set(normalize(value));
    }

    /** Sets a range endpoint as the value produced when the field is cleared. */
    public void setValueOnClear(EleFXInputNumberValueOnClear value) {
        valueOnClear.set(null);
        valueOnClearMode.set(value);
    }

    public EleFXInputNumberValueOnClear getValueOnClearMode() {
        return valueOnClearMode.get();
    }

    public ObjectProperty<EleFXInputNumberValueOnClear> valueOnClearModeProperty() {
        return valueOnClearMode;
    }

    public boolean isValidateEvent() {
        return validateEvent.get();
    }

    public void setValidateEvent(boolean value) {
        validateEvent.set(value);
    }

    public BooleanProperty validateEventProperty() {
        return validateEvent;
    }

    public String getInputMode() {
        return inputMode.get();
    }

    public void setInputMode(String value) {
        inputMode.set(value == null ? "decimal" : value);
    }

    public StringProperty inputModeProperty() {
        return inputMode;
    }

    public Pos getAlign() {
        return align.get();
    }

    public void setAlign(Pos value) {
        align.set(value == null ? Pos.CENTER : value);
    }

    public ObjectProperty<Pos> alignProperty() {
        return align;
    }

    public boolean isDisabledScientific() {
        return disabledScientific.get();
    }

    public void setDisabledScientific(boolean value) {
        disabledScientific.set(value);
    }

    public BooleanProperty disabledScientificProperty() {
        return disabledScientific;
    }

    public Function<Double, String> getFormatter() {
        return formatter.get();
    }

    public void setFormatter(Function<Double, String> value) {
        formatter.set(value);
        updateInputText();
    }

    public ObjectProperty<Function<Double, String>> formatterProperty() {
        return formatter;
    }

    public Function<String, String> getParser() {
        return parser.get();
    }

    public void setParser(Function<String, String> value) {
        parser.set(value);
    }

    public ObjectProperty<Function<String, String>> parserProperty() {
        return parser;
    }

    public Node getPrefix() {
        return prefix.get();
    }

    public void setPrefix(Node value) {
        prefix.set(value);
    }

    public ObjectProperty<Node> prefixProperty() {
        return prefix;
    }

    public Node getSuffix() {
        return suffix.get();
    }

    public void setSuffix(Node value) {
        suffix.set(value);
    }

    public ObjectProperty<Node> suffixProperty() {
        return suffix;
    }

    /** Custom decrease button icon; {@code null} restores the Element Plus default. */
    public Node getDecreaseIcon() {
        return decreaseIcon.get();
    }

    public void setDecreaseIcon(Node value) {
        decreaseIcon.set(value);
    }

    public ObjectProperty<Node> decreaseIconProperty() {
        return decreaseIcon;
    }

    /** Custom increase button icon; {@code null} restores the Element Plus default. */
    public Node getIncreaseIcon() {
        return increaseIcon.get();
    }

    public void setIncreaseIcon(Node value) {
        increaseIcon.set(value);
    }

    public ObjectProperty<Node> increaseIconProperty() {
        return increaseIcon;
    }

    public EventHandler<EleFXInputNumberEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXInputNumberEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXInputNumberEvent>> onChangeProperty() {
        return onChange;
    }

    public EventHandler<javafx.event.Event> getOnFocus() {
        return onFocus.get();
    }

    public void setOnFocus(EventHandler<javafx.event.Event> value) {
        onFocus.set(value);
    }

    public ObjectProperty<EventHandler<javafx.event.Event>> onFocusProperty() {
        return onFocus;
    }

    public EventHandler<javafx.event.Event> getOnBlur() {
        return onBlur.get();
    }

    public void setOnBlur(EventHandler<javafx.event.Event> value) {
        onBlur.set(value);
    }

    public ObjectProperty<EventHandler<javafx.event.Event>> onBlurProperty() {
        return onBlur;
    }

    public TextField getInput() {
        return input;
    }

    public void focus() {
        input.requestFocus();
    }

    public void blur() {
        if (input.getScene() != null) input.getScene().getRoot().requestFocus();
    }

    public void clear() {
        setValue(clearValue());
    }

    public void increment() {
        changeBy(getStep());
    }

    public void decrement() {
        changeBy(-getStep());
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.INPUT_NUMBER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private Button button(Node icon, String style) {
        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add(style);
        button.setFocusTraversable(false);
        return button;
    }

    private void rebuild() {
        getChildren().clear();
        rightControls.getChildren().clear();
        rightAdornment.getChildren().clear();
        prefixAdornment.getChildren().clear();
        suffixAdornment.getChildren().clear();
        updateControlIcons();
        inputArea.setLeft(null);
        inputArea.setRight(null);
        Node prefix = getPrefix();
        Node suffix = getSuffix();
        if (prefix != null) {
            if (!prefix.getStyleClass().contains("ele-input-number__prefix"))
                prefix.getStyleClass().add("ele-input-number__prefix");
            prefixAdornment.getChildren().add(prefix);
            inputArea.setLeft(prefixAdornment);
        }
        if (suffix != null && !suffix.getStyleClass().contains("ele-input-number__suffix"))
            suffix.getStyleClass().add("ele-input-number__suffix");
        inputArea.setCenter(input);
        if (isControls()) {
            if (getControlsPosition() == EleFXInputNumberControlsPosition.RIGHT) {
                rightControls.getChildren().addAll(increaseButton, decreaseButton);
                StackPane.setAlignment(increaseButton, Pos.TOP_CENTER);
                StackPane.setAlignment(decreaseButton, Pos.BOTTOM_CENTER);
                if (suffix != null) {
                    suffixAdornment.getChildren().add(suffix);
                    rightAdornment.getChildren().add(suffixAdornment);
                }
                rightAdornment.getChildren().add(rightControls);
                inputArea.setRight(rightAdornment);
            } else {
                if (suffix != null) {
                    suffixAdornment.getChildren().add(suffix);
                    inputArea.setRight(suffixAdornment);
                }
                getChildren().add(decreaseButton);
            }
        } else if (suffix != null) {
            suffixAdornment.getChildren().add(suffix);
            inputArea.setRight(suffixAdornment);
        }
        getChildren().add(inputArea);
        if (isControls() && getControlsPosition() == EleFXInputNumberControlsPosition.DEFAULT)
            getChildren().add(increaseButton);
        updatePositionStyle();
        updateButtonState();
    }

    private void updateControlIcons() {
        boolean right = getControlsPosition() == EleFXInputNumberControlsPosition.RIGHT;
        defaultDecreaseIcon.setType(right ? EleFXIconType.ARROW_DOWN : EleFXIconType.MINUS);
        defaultIncreaseIcon.setType(right ? EleFXIconType.ARROW_UP : EleFXIconType.PLUS);
        decreaseButton.setGraphic(getDecreaseIcon() == null ? defaultDecreaseIcon : getDecreaseIcon());
        increaseButton.setGraphic(getIncreaseIcon() == null ? defaultIncreaseIcon : getIncreaseIcon());
    }

    private void parseInput(String text) {
        if (updating) return;
        parsing = true;
        try {
            if (text == null || text.trim().isEmpty()) {
                setValue(clearValue());
                return;
            }
            String raw = getParser() == null ? text : getParser().apply(text);
            if (isDisabledScientific() && raw != null && (raw.contains("e") || raw.contains("E"))) {
                setValue(null);
                return;
            }
            try {
                setValue(Double.parseDouble(raw.trim()));
            } catch (RuntimeException ex) {
                setValue(null);
            }
        } finally {
            parsing = false;
        }
        // Unlike plain numeric input, formatted input should immediately show
        // its presentation value (for example, "2" -> "$ 2"). Invalid text
        // remains untouched so the user can correct it.
        if (getFormatter() != null && getValue() != null) updateInputText();
    }

    private void commitInput() {
        if (input.getText().trim().isEmpty())
            setValue(clearValue());
        else if (getValue() != null)
            setValue(getValue());
        // During editing the raw text is deliberately retained (for example
        // "3" while stepStrictly has already normalized the value to "4").
        // Commit must always replace that raw text with the final value.
        updateInputText();
    }

    private void changeBy(double amount) {
        if (isDisabled() || isReadOnly()) return;
        Double current = getValue();
        setValue((current == null ? 0d : current) + amount);
    }

    private void valueChanged(Double oldValue, Double newValue) {
        if (!parsing) updateInputText();
        updateButtonState();
        if (!updating && !same(oldValue, newValue)) {
            EleFXInputNumberEvent event = new EleFXInputNumberEvent(this, this, oldValue, newValue);
            fireEvent(event);
            if (getOnChange() != null) getOnChange().handle(event);
        }
    }

    private void updateInputText() {
        updating = true;
        try {
            Double current = getValue();
            String text = current == null
                    ? ""
                    : (getFormatter() == null ? numberText(current) : getFormatter().apply(current));
            input.setText(text == null ? "" : text);
            input.positionCaret(input.getLength());
        } finally {
            updating = false;
        }
    }

    private Double normalize(Double candidate) {
        if (candidate == null || !Double.isFinite(candidate)) return null;
        double result = Math.max(getMin(), Math.min(getMax(), candidate));
        // Element Plus rounds strict-step values to a multiple of step from
        // zero, not from the configured minimum (whose default is a very
        // large negative safe integer).
        if (isStepStrictly()) result = Math.round(result / getStep()) * getStep();
        int scale = getPrecision() >= 0 ? getPrecision() : Math.max(decimalPlaces(getStep()), decimalPlaces(result));
        result = BigDecimal.valueOf(result).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        return Math.max(getMin(), Math.min(getMax(), result));
    }

    private String numberText(double number) {
        int scale = getPrecision() >= 0 ? getPrecision() : Math.max(decimalPlaces(getStep()), decimalPlaces(number));
        return BigDecimal.valueOf(number).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    private void rangeChanged() {
        Double current = getValue();
        if (current != null) setValue(current);
        updateButtonState();
    }

    private void precisionChanged(int value) {
        validatePrecision(value);
        Double current = getValue();
        if (current != null) setValue(current);
        // A numerically identical value (0 and 0.00) does not fire a
        // property change, but its display precision still changed.
        updateInputText();
    }

    private void updateButtonState() {
        Double current = getValue();
        boolean locked = isDisabled() || isReadOnly();
        decreaseButton.setDisable(locked || (current != null && current <= getMin()));
        increaseButton.setDisable(locked || (current != null && current >= getMax()));
    }

    private void updateSizeStyle(EleFXInputNumberSize oldSize, EleFXInputNumberSize newSize) {
        if (oldSize != null) getStyleClass().remove("ele-input-number--size-" + oldSize.name().toLowerCase());
        EleFXInputNumberSize actual = newSize == null ? EleFXInputNumberSize.DEFAULT : newSize;
        getStyleClass().add("ele-input-number--size-" + actual.name().toLowerCase());
    }

    private void updatePositionStyle() {
        getStyleClass().removeAll(EleFXInputNumberControlsPosition.DEFAULT.styleClass(),
                EleFXInputNumberControlsPosition.RIGHT.styleClass());
        getStyleClass().add(getControlsPosition().styleClass());
    }

    private void validateStep(double value) {
        if (!Double.isFinite(value) || value <= 0)
            throw new IllegalArgumentException("step must be a positive finite number");
    }

    private void validatePrecision(int value) {
        if (value < -1 || (value >= 0 && value < decimalPlaces(getStep())))
            throw new IllegalArgumentException("precision must be -1 or at least the decimal places of step");
    }

    private void validateSafe(double value, String name) {
        if (!Double.isFinite(value) || value < MIN_SAFE_INTEGER || value > MAX_SAFE_INTEGER)
            throw new IllegalArgumentException(name + " must be a finite safe integer range value");
    }

    private static int decimalPlaces(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        return Math.max(0, decimal.scale());
    }

    private static boolean same(Double first, Double second) {
        return first == second || (first != null && first.equals(second));
    }

    private void fireSimpleEvent(EventHandler<javafx.event.Event> handler) {
        if (handler != null) handler.handle(new javafx.event.Event(this, this, javafx.event.Event.ANY));
    }

    private Double clearValue() {
        if (getValueOnClearMode() == EleFXInputNumberValueOnClear.MIN) return getMin();
        if (getValueOnClearMode() == EleFXInputNumberValueOnClear.MAX) return getMax();
        return getValueOnClear();
    }

    private void handleSceneMousePressed(MouseEvent event) {
        if (!input.isFocused() || !(event.getTarget()instanceof Node target) || isDescendant(target)) return;
        if (getScene() != null) getScene().getRoot().requestFocus();
    }

    private boolean isDescendant(Node node) {
        for (Node current = node; current != null; current = current.getParent())
            if (current == this) return true;
        return false;
    }
}
