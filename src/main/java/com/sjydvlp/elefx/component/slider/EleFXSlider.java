package com.sjydvlp.elefx.component.slider;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Element Plus inspired slider. It supports single and range selection, horizontal
 * and vertical tracks, discrete stops/marks, optional numeric input, keyboard
 * access, formatted value tooltips, and separate live input and committed change events.
 */
public class EleFXSlider extends HBox implements Themable {

    private static final double TRACK_THICKNESS = 4;

    private static final double TOOLTIP_GAP = 18;

    private static final double TOOLTIP_MIN_WIDTH = 40;

    private static final double MARK_LABEL_GAP = 18;

    private final Pane track = new Pane();

    private final Region rail = region("ele-slider__runway");

    private final Region bar = region("ele-slider__bar");

    private final Region lowerButton = region("ele-slider__button");

    private final Region upperButton = region("ele-slider__button");

    private final Label tooltip = new Label();

    private final Region tooltipArrow = region("ele-slider__tooltip-arrow");

    private final TextField input = new TextField();

    private final Button decreaseButton = new Button("−");

    private final Button increaseButton = new Button("+");

    private final HBox inputBox = new HBox();

    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0);

    private final DoubleProperty upperValue = new SimpleDoubleProperty(this, "upperValue", 100);

    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0);

    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", 100);

    private final DoubleProperty step = new SimpleDoubleProperty(this, "step", 1);

    /** -1 derives the displayed precision from {@link #getStep()}. */
    private final IntegerProperty precision = new SimpleIntegerProperty(this, "precision", -1);

    private final BooleanProperty range = new SimpleBooleanProperty(this, "range", false);

    private final BooleanProperty vertical = new SimpleBooleanProperty(this, "vertical", false);

    private final DoubleProperty sliderHeight = new SimpleDoubleProperty(this, "sliderHeight", 200);

    private final BooleanProperty showStops = new SimpleBooleanProperty(this, "showStops", false);

    private final BooleanProperty showInput = new SimpleBooleanProperty(this, "showInput", false);

    private final BooleanProperty showInputControls = new SimpleBooleanProperty(this, "showInputControls", true);

    private final BooleanProperty showTooltip = new SimpleBooleanProperty(this, "showTooltip", true);

    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent", true);

    private final BooleanProperty snapToMarks = new SimpleBooleanProperty(this, "snapToMarks", false);

    private final ObjectProperty<EleFXSliderSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXSliderSize.DEFAULT);

    private final ObjectProperty<Function<Double, String>> formatTooltip = new SimpleObjectProperty<>(this,
            "formatTooltip");

    private final ObjectProperty<Function<Double, String>> formatValueText = new SimpleObjectProperty<>(this,
            "formatValueText");

    private final ObjectProperty<EleFXSliderTooltipPlacement> tooltipPlacement = new SimpleObjectProperty<>(this,
            "tooltipPlacement", EleFXSliderTooltipPlacement.TOP);

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "Slider");

    private final StringProperty rangeStartLabel = new SimpleStringProperty(this, "rangeStartLabel", "Range start");

    private final StringProperty rangeEndLabel = new SimpleStringProperty(this, "rangeEndLabel", "Range end");

    private final ObservableMap<Double, EleFXSliderMark> marks = FXCollections.observableHashMap();

    private final ObjectProperty<EventHandler<EleFXSliderEvent>> onInput = new SimpleObjectProperty<>(this, "onInput");

    private final ObjectProperty<EventHandler<EleFXSliderEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final List<Node> decorations = new ArrayList<>();

    private boolean updating, dragging, draggingUpper;

    private double dragStartValue, dragStartUpperValue;

    private final EventHandler<MouseEvent> outsideClickHandler = this::handleSceneMousePressed;

    public EleFXSlider() {
        getStyleClass().add("ele-slider");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setFocusTraversable(true);
        track.getStyleClass().add("ele-slider__track");
        track.setMinSize(80, 32);
        track.setPrefSize(200, 32);
        tooltip.getStyleClass().add("ele-slider__tooltip");
        tooltip.setMouseTransparent(true);
        tooltip.setManaged(false);
        input.getStyleClass().add("ele-slider__input");
        input.setPrefWidth(100);
        inputBox.getStyleClass().add("ele-slider__input-box");
        decreaseButton.getStyleClass().add("ele-slider__input-control");
        decreaseButton.getStyleClass().add("ele-slider__input-decrease");
        increaseButton.getStyleClass().add("ele-slider__input-control");
        increaseButton.getStyleClass().add("ele-slider__input-increase");
        decreaseButton.setFocusTraversable(false);
        increaseButton.setFocusTraversable(false);
        inputBox.getChildren().addAll(decreaseButton, input, increaseButton);
        track.getChildren().addAll(rail, bar, lowerButton, upperButton, tooltipArrow, tooltip);
        getChildren().add(track);
        HBox.setHgrow(track, Priority.ALWAYS);
        lowerButton.setAccessibleRole(javafx.scene.AccessibleRole.SLIDER);
        upperButton.setAccessibleRole(javafx.scene.AccessibleRole.SLIDER);
        lowerButton.hoverProperty().addListener((o, oldValue, hovering) -> layoutTrack());
        upperButton.hoverProperty().addListener((o, oldValue, hovering) -> layoutTrack());
        installPointerHandlers(lowerButton, false);
        installPointerHandlers(upperButton, true);
        track.addEventHandler(MouseEvent.MOUSE_PRESSED, this::trackPressed);
        track.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::trackDragged);
        track.addEventHandler(MouseEvent.MOUSE_RELEASED, this::trackReleased);
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getTarget()instanceof Node node && !isDescendantOf(node, inputBox)) requestFocus();
        });
        sceneProperty().addListener((o, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
        });
        track.widthProperty().addListener(o -> layoutTrack());
        track.heightProperty().addListener(o -> layoutTrack());
        value.addListener((o, a, b) -> changed(a.doubleValue(), b.doubleValue(), getUpperValue(), getUpperValue()));
        upperValue.addListener((o, a, b) -> changed(getValue(), getValue(), a.doubleValue(), b.doubleValue()));
        min.addListener((o, a, b) -> rangeChanged());
        max.addListener((o, a, b) -> rangeChanged());
        step.addListener((o, a, b) -> {
            validateStep(b.doubleValue());
            normalize();
        });
        precision.addListener((o, a, b) -> {
            validatePrecision(b.intValue());
            updateInput();
            layoutTrack();
        });
        range.addListener((o, a, b) -> {
            normalize();
            rebuild();
        });
        vertical.addListener((o, a, b) -> rebuild());
        sliderHeight.addListener((o, a, b) -> rebuild());
        showStops.addListener((o, a, b) -> rebuild());
        showInput.addListener((o, a, b) -> rebuild());
        showInputControls.addListener((o, a, b) -> rebuild());
        snapToMarks.addListener((o, a, b) -> normalize());
        size.addListener((o, a, b) -> updateSize());
        tooltipPlacement.addListener((o, a, b) -> layoutTrack());
        marks.addListener((javafx.collections.MapChangeListener<Double, EleFXSliderMark>) c -> {
            validateMarks();
            rebuild();
            normalize();
        });
        disableProperty().addListener((o, a, b) -> layoutTrack());
        ariaLabel.addListener((o, a, b) -> updateAccessibleText());
        rangeStartLabel.addListener((o, a, b) -> updateAccessibleText());
        rangeEndLabel.addListener((o, a, b) -> updateAccessibleText());
        input.setOnAction(e -> commitInput());
        input.focusedProperty().addListener((o, a, b) -> {
            inputBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), b);
            if (!b) commitInput();
        });
        decreaseButton.setOnAction(e -> {
            setValue(getValue() - getStep());
            input.requestFocus();
        });
        increaseButton.setOnAction(e -> {
            setValue(getValue() + getStep());
            input.requestFocus();
        });
        setOnKeyPressed(this::keyPressed);
        updateSize();
        rebuild();
        sceneBuilderIntegration();
    }

    public EleFXSlider(double value) {
        this();
        setValue(value);
    }

    private static Region region(String style) {
        Region r = new Region();
        r.getStyleClass().add(style);
        // Pane lays out managed children using their preferred size. The slider
        // owns these nodes' bounds, so they must not be reset during a pulse.
        r.setManaged(false);
        return r;
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double v) {
        value.set(normalize(v));
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public double getLowerValue() {
        return getValue();
    }

    public void setLowerValue(double v) {
        setValue(v);
    }

    public DoubleProperty lowerValueProperty() {
        return value;
    }

    public double getUpperValue() {
        return upperValue.get();
    }

    public void setUpperValue(double v) {
        upperValue.set(normalize(v));
    }

    public DoubleProperty upperValueProperty() {
        return upperValue;
    }

    public void setRangeValues(double lower, double upper) {
        updating = true;
        value.set(normalize(lower));
        upperValue.set(normalize(upper));
        ensureOrder();
        updating = false;
        changed(getValue(), getValue(), getUpperValue(), getUpperValue());
    }

    public double getMin() {
        return min.get();
    }

    public void setMin(double v) {
        if (!Double.isFinite(v) || v > getMax())
            throw new IllegalArgumentException("min must be finite and no greater than max");
        min.set(v);
    }

    public DoubleProperty minProperty() {
        return min;
    }

    public double getMax() {
        return max.get();
    }

    public void setMax(double v) {
        if (!Double.isFinite(v) || v < getMin())
            throw new IllegalArgumentException("max must be finite and no less than min");
        max.set(v);
    }

    public DoubleProperty maxProperty() {
        return max;
    }

    public double getStep() {
        return step.get();
    }

    public void setStep(double v) {
        validateStep(v);
        snapToMarks.set(false);
        step.set(v);
    }

    public DoubleProperty stepProperty() {
        return step;
    }

    /** Enables Element Plus's {@code step="mark"} mode. */
    public void setStep(EleFXSliderStep value) {
        if (value != EleFXSliderStep.MARK) throw new IllegalArgumentException("Unsupported slider step mode");
        if (marks.isEmpty()) throw new IllegalStateException("marks are required when step is MARK");
        snapToMarks.set(true);
    }

    /**
     * Number of decimal places displayed by the built-in input and tooltip.
     * {@code -1} (the default) uses the decimal places of {@link #getStep()}.
     */
    public int getPrecision() {
        return precision.get();
    }

    public void setPrecision(int value) {
        validatePrecision(value);
        precision.set(value);
    }

    public IntegerProperty precisionProperty() {
        return precision;
    }

    public boolean isRange() {
        return range.get();
    }

    public void setRange(boolean v) {
        range.set(v);
    }

    public BooleanProperty rangeProperty() {
        return range;
    }

    public boolean isVertical() {
        return vertical.get();
    }

    public void setVertical(boolean v) {
        vertical.set(v);
    }

    public BooleanProperty verticalProperty() {
        return vertical;
    }

    /** Track length used in vertical mode (Element Plus's {@code height} attribute). */
    public double getSliderHeight() {
        return sliderHeight.get();
    }

    public void setSliderHeight(double v) {
        if (v <= 0 || !Double.isFinite(v)) throw new IllegalArgumentException("height must be positive");
        sliderHeight.set(v);
    }

    public DoubleProperty sliderHeightProperty() {
        return sliderHeight;
    }

    public boolean isShowStops() {
        return showStops.get();
    }

    public void setShowStops(boolean v) {
        showStops.set(v);
    }

    public BooleanProperty showStopsProperty() {
        return showStops;
    }

    public boolean isShowInput() {
        return showInput.get();
    }

    public void setShowInput(boolean v) {
        showInput.set(v);
    }

    public BooleanProperty showInputProperty() {
        return showInput;
    }

    public boolean isShowInputControls() {
        return showInputControls.get();
    }

    public void setShowInputControls(boolean v) {
        showInputControls.set(v);
    }

    public BooleanProperty showInputControlsProperty() {
        return showInputControls;
    }

    public boolean isShowTooltip() {
        return showTooltip.get();
    }

    public void setShowTooltip(boolean v) {
        showTooltip.set(v);
    }

    public BooleanProperty showTooltipProperty() {
        return showTooltip;
    }

    public boolean isPersistent() {
        return persistent.get();
    }

    public void setPersistent(boolean v) {
        persistent.set(v);
    }

    public BooleanProperty persistentProperty() {
        return persistent;
    }

    public EleFXSliderSize getSize() {
        return size.get();
    }

    public void setSize(EleFXSliderSize v) {
        size.set(v == null ? EleFXSliderSize.DEFAULT : v);
    }

    public ObjectProperty<EleFXSliderSize> sizeProperty() {
        return size;
    }

    public Function<Double, String> getFormatTooltip() {
        return formatTooltip.get();
    }

    public void setFormatTooltip(Function<Double, String> v) {
        formatTooltip.set(v);
        layoutTrack();
    }

    public ObjectProperty<Function<Double, String>> formatTooltipProperty() {
        return formatTooltip;
    }

    public Function<Double, String> getFormatValueText() {
        return formatValueText.get();
    }

    public void setFormatValueText(Function<Double, String> v) {
        formatValueText.set(v);
        updateAccessibleText();
    }

    public ObjectProperty<Function<Double, String>> formatValueTextProperty() {
        return formatValueText;
    }

    /** Position of the value tooltip relative to the active handle. */
    public EleFXSliderTooltipPlacement getTooltipPlacement() {
        return tooltipPlacement.get();
    }

    public void setTooltipPlacement(EleFXSliderTooltipPlacement value) {
        tooltipPlacement.set(value == null ? EleFXSliderTooltipPlacement.TOP : value);
    }

    public ObjectProperty<EleFXSliderTooltipPlacement> tooltipPlacementProperty() {
        return tooltipPlacement;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String v) {
        ariaLabel.set(v == null ? "Slider" : v);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public String getRangeStartLabel() {
        return rangeStartLabel.get();
    }

    public void setRangeStartLabel(String v) {
        rangeStartLabel.set(v == null ? "Range start" : v);
    }

    public StringProperty rangeStartLabelProperty() {
        return rangeStartLabel;
    }

    public String getRangeEndLabel() {
        return rangeEndLabel.get();
    }

    public void setRangeEndLabel(String v) {
        rangeEndLabel.set(v == null ? "Range end" : v);
    }

    public StringProperty rangeEndLabelProperty() {
        return rangeEndLabel;
    }

    /** Deprecated Element Plus-compatible alias for {@link #getAriaLabel()}. */
    @Deprecated
    public String getLabel() {
        return getAriaLabel();
    }

    @Deprecated
    public void setLabel(String v) {
        setAriaLabel(v);
    }

    @Deprecated
    public StringProperty labelProperty() {
        return ariaLabelProperty();
    }

    public ObservableMap<Double, EleFXSliderMark> getMarks() {
        return marks;
    }

    public void setMarks(java.util.Map<Double, EleFXSliderMark> v) {
        marks.clear();
        if (v != null) marks.putAll(v);
        validateMarks();
        rebuild();
    }

    public EventHandler<EleFXSliderEvent> getOnInput() {
        return onInput.get();
    }

    public void setOnInput(EventHandler<EleFXSliderEvent> v) {
        onInput.set(v);
    }

    public ObjectProperty<EventHandler<EleFXSliderEvent>> onInputProperty() {
        return onInput;
    }

    public EventHandler<EleFXSliderEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXSliderEvent> v) {
        onChange.set(v);
    }

    public ObjectProperty<EventHandler<EleFXSliderEvent>> onChangeProperty() {
        return onChange;
    }

    public TextField getInput() {
        return input;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SLIDER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void rebuild() {
        getStyleClass().removeIf(s -> s.startsWith("ele-slider--size-") || s.equals("ele-slider--vertical"));
        getStyleClass().add(getSize().styleClass());
        if (isVertical()) getStyleClass().add("ele-slider--vertical");
        double trackCrossSize = getSize().controlHeight();
        track.setPrefSize(isVertical() ? trackCrossSize : 200, isVertical() ? getSliderHeight() : trackCrossSize);
        track.setMinSize(isVertical() ? trackCrossSize : 80, isVertical() ? getSliderHeight() : trackCrossSize);
        if (isVertical()) {
            // A vertical Slider has a concrete track length. Make that length
            // part of both the track and wrapper constraints so a parent HBox/
            // VBox cannot collapse it back to the horizontal control height.
            track.setMaxSize(trackCrossSize, getSliderHeight());
            setMinSize(trackCrossSize, getSliderHeight());
            setPrefSize(trackCrossSize, getSliderHeight());
        } else {
            track.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        }
        upperButton.setVisible(isRange());
        // Both handles are manually positioned in layoutTrack(). Keeping the
        // range end unmanaged prevents Pane from resetting its size/position.
        upperButton.setManaged(false);
        boolean inputVisible = isShowInput() && !isRange() && !snapToMarks.get();
        inputBox.setVisible(inputVisible);
        inputBox.setManaged(inputVisible);
        decreaseButton.setVisible(isShowInputControls());
        decreaseButton.setManaged(isShowInputControls());
        increaseButton.setVisible(isShowInputControls());
        increaseButton.setManaged(isShowInputControls());
        if (inputVisible && !getChildren().contains(inputBox))
            getChildren().add(inputBox);
        else if (!inputVisible) getChildren().remove(inputBox);
        decorations.forEach(track.getChildren()::remove);
        decorations.clear();
        addDecorations();
        updateInput();
        layoutTrack();
    }

    private void addDecorations() {
        if (isShowStops() && !snapToMarks.get())
            for (double v = getMin() + getStep(); v < getMax() - 1e-9; v += getStep())
            addPoint(v, "ele-slider__stop", null);
        marks.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(e -> {
            addPoint(e.getKey(), "ele-slider__mark-stop", null);
            Label l = new Label(e.getValue().getLabel());
            l.setUserData(e.getKey());
            l.getStyleClass().add("ele-slider__mark-text");
            l.setManaged(false);
            if (e.getValue().getColor() != null) l.setTextFill(e.getValue().getColor());
            l.setMouseTransparent(true);
            decorations.add(l);
            addDecoration(l);
        });
    }

    private void addPoint(double v, String style, Paint ignored) {
        Region p = region(style);
        p.setUserData(v);
        p.setMouseTransparent(true);
        decorations.add(p);
        addDecoration(p);
    }

    /** Keeps marks above the rails but below the interactive handles. */
    private void addDecoration(Node node) {
        int handleIndex = track.getChildren().indexOf(lowerButton);
        track.getChildren().add(handleIndex < 0 ? track.getChildren().size() : handleIndex, node);
    }

    private void layoutTrack() {
        double w = track.getWidth(), h = track.getHeight(), centerX = w / 2, centerY = h / 2, start = 12,
                length = (isVertical() ? h : w) - 24;
        if (length <= 0) return;
        if (isVertical()) {
            rail.resizeRelocate(centerX - TRACK_THICKNESS / 2, start, TRACK_THICKNESS, length);
        } else
            rail.resizeRelocate(start, centerY - TRACK_THICKNESS / 2, length, TRACK_THICKNESS);
        double a = position(getValue()), b = isRange() ? position(getUpperValue()) : 0,
                from = isRange() ? Math.min(a, b) : 0, to = isRange() ? Math.max(a, b) : a;
        if (isVertical()) {
            // "to" is a 0..1 ratio, not a pixel coordinate. The previous
            // expression subtracted the ratio directly and detached the bar
            // from its handle except at the endpoints.
            bar.resizeRelocate(centerX - TRACK_THICKNESS / 2, start + length * (1 - to), TRACK_THICKNESS,
                    length * (to - from));
            place(lowerButton, a, centerX, start, length);
            if (isRange()) place(upperButton, b, centerX, start, length);
        } else {
            bar.resizeRelocate(start + length * from, centerY - TRACK_THICKNESS / 2, length * (to - from),
                    TRACK_THICKNESS);
            place(lowerButton, a, centerY, start, length);
            if (isRange()) place(upperButton, b, centerY, start, length);
        }
        for (Node n : decorations) {
            Object data = n.getUserData();
            if (n instanceof Label l) {
                double p = position(markForLabel(l));
                double labelWidth = l.prefWidth(-1), labelHeight = l.prefHeight(-1);
                if (isVertical())
                    l.resizeRelocate(centerX + MARK_LABEL_GAP, start + length - length * p - labelHeight / 2,
                            labelWidth,
                            labelHeight);
                else
                    l.resizeRelocate(start + length * p - labelWidth / 2, centerY + MARK_LABEL_GAP, labelWidth,
                            labelHeight);
            } else if (data instanceof Double v) placeDecoration(n, position(v), centerX, centerY, start, length);
        }
        boolean visible = isShowTooltip() && (dragging || lowerButton.isHover() || upperButton.isHover());
        tooltip.setVisible(visible);
        tooltipArrow.setVisible(visible);
        tooltip.setManaged(false);
        if (tooltip.isVisible()) {
            double p = position(draggingUpper ? getUpperValue() : getValue());
            tooltip.setText(format(draggingUpper ? getUpperValue() : getValue()));
            double tooltipWidth = Math.max(TOOLTIP_MIN_WIDTH, tooltip.prefWidth(-1));
            double tooltipHeight = tooltip.prefHeight(tooltipWidth);
            double handleX = isVertical() ? centerX : start + length * p;
            double handleY = isVertical() ? start + length - length * p : centerY;
            double tooltipX, tooltipY, arrowX, arrowY;
            switch (getTooltipPlacement()) {
                case BOTTOM -> {
                    tooltipX = handleX - tooltipWidth / 2;
                    tooltipY = handleY + TOOLTIP_GAP;
                    arrowX = handleX - 4;
                    arrowY = tooltipY - 4;
                }
                case LEFT -> {
                    tooltipX = handleX - tooltipWidth - TOOLTIP_GAP;
                    tooltipY = handleY - tooltipHeight / 2;
                    arrowX = tooltipX + tooltipWidth - 4;
                    arrowY = handleY - 4;
                }
                case RIGHT -> {
                    tooltipX = handleX + TOOLTIP_GAP;
                    tooltipY = handleY - tooltipHeight / 2;
                    arrowX = tooltipX - 4;
                    arrowY = handleY - 4;
                }
                case TOP -> {
                    tooltipX = handleX - tooltipWidth / 2;
                    tooltipY = handleY - tooltipHeight - TOOLTIP_GAP;
                    arrowX = handleX - 4;
                    arrowY = tooltipY + tooltipHeight - 4;
                }
                default -> throw new IllegalStateException("Unexpected tooltip placement");
            }
            tooltip.resizeRelocate(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
            tooltipArrow.resizeRelocate(arrowX, arrowY, 8, 8);
        }
    }

    private double markForLabel(Label l) {
        return l.getUserData()instanceof Double value ? value : getMin();
    }

    private void place(Region node, double p, double cross, double start, double length) {
        double d = getSize().handleRadius();
        if (isVertical())
            node.resizeRelocate(cross - d, start + length - length * p - d, d * 2, d * 2);
        else
            node.resizeRelocate(start + length * p - d, cross - d, d * 2, d * 2);
    }

    private void placeDecoration(Node n, double p, double crossX, double crossY, double start, double length) {
        if (isVertical())
            n.resizeRelocate(crossX - 3, start + length - length * p - 3, 6, 6);
        else
            n.resizeRelocate(start + length * p - 3, crossY - 3, 6, 6);
    }

    private void installPointerHandlers(Region handle, boolean upper) {
        handle.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (isDisabled()) return;
            dragging = true;
            draggingUpper = upper;
            dragStartValue = getValue();
            dragStartUpperValue = getUpperValue();
            requestFocus();
            layoutTrack();
            e.consume();
        });
        handle.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!isDisabled()) setFromMouse(e, upper);
            e.consume();
        });
        handle.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (dragging) {
                dragging = false;
                emit(EleFXSliderEvent.CHANGE, dragStartValue, getValue(), dragStartUpperValue, getUpperValue());
                layoutTrack();
            }
            e.consume();
        });
    }

    private void trackPressed(MouseEvent e) {
        // Rail and progress bar are track children, so their mouse events must
        // bubble here too. Handle events are consumed by their own handlers.
        if (isDisabled()) return;
        boolean upper = isRange() && Math.abs(valueAt(e) - getUpperValue()) < Math.abs(valueAt(e) - getValue());
        dragging = true;
        draggingUpper = upper;
        dragStartValue = getValue();
        dragStartUpperValue = getUpperValue();
        requestFocus();
        setFromMouse(e, upper);
    }

    private void trackDragged(MouseEvent e) {
        if (dragging && !isDisabled()) setFromMouse(e, draggingUpper);
    }

    private void trackReleased(MouseEvent e) {
        if (!dragging) return;
        dragging = false;
        emit(EleFXSliderEvent.CHANGE, dragStartValue, getValue(), dragStartUpperValue, getUpperValue());
        layoutTrack();
    }

    private void setFromMouse(MouseEvent e, boolean upper) {
        double v = valueAt(e);
        if (upper)
            setUpperValue(v);
        else
            setValue(v);
    }

    private double valueAt(MouseEvent e) {
        double length = (isVertical() ? track.getHeight() : track.getWidth()) - 24;
        // Drag events originating at a handle use handle-local coordinates.
        // Convert every event to the stable track coordinate system first.
        javafx.geometry.Point2D point = track.sceneToLocal(e.getSceneX(), e.getSceneY());
        double p = isVertical() ? 1 - (point.getY() - 12) / length : (point.getX() - 12) / length;
        return normalize(getMin() + Math.max(0, Math.min(1, p)) * (getMax() - getMin()));
    }

    private double position(double v) {
        return (v - getMin()) / (getMax() - getMin());
    }

    private void changed(double old, double now, double oldUpper, double nowUpper) {
        if (updating) return;
        updating = true;
        value.set(normalize(value.get()));
        upperValue.set(normalize(upperValue.get()));
        ensureOrder();
        updating = false;
        updateInput();
        updateAccessibleText();
        layoutTrack();
        emit(EleFXSliderEvent.INPUT, old, now, oldUpper, nowUpper);
    }

    private void ensureOrder() {
        if (isRange() && getValue() > getUpperValue()) {
            if (draggingUpper)
                value.set(getUpperValue());
            else
                upperValue.set(getValue());
        }
    }

    private void rangeChanged() {
        normalize();
        rebuild();
    }

    private void normalize() {
        updating = true;
        value.set(normalize(value.get()));
        upperValue.set(normalize(upperValue.get()));
        ensureOrder();
        updating = false;
        updateInput();
        layoutTrack();
    }

    private double normalize(double v) {
        if (!Double.isFinite(v)) return getMin();
        if (snapToMarks.get() && !marks.isEmpty())
            return marks.keySet().stream().min(Comparator.comparingDouble(m -> Math.abs(m - v))).orElse(getMin());
        double snapped = getMin() + Math.rint((v - getMin()) / getStep()) * getStep();
        return Math.max(getMin(), Math.min(getMax(), round(snapped)));
    }

    private double round(double v) {
        return BigDecimal.valueOf(v)
                .setScale(Math.min(12, Math.max(0, BigDecimal.valueOf(getStep()).stripTrailingZeros().scale())),
                        RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void validateStep(double v) {
        if (!Double.isFinite(v) || v <= 0)
            throw new IllegalArgumentException("step must be finite and greater than zero");
    }

    private void validatePrecision(int value) {
        if (value < -1 || value > 12) throw new IllegalArgumentException("precision must be between -1 and 12");
    }

    private void validateMarks() {
        if (marks.keySet().stream().anyMatch(v -> v == null || !Double.isFinite(v) || v < getMin() || v > getMax())
                || marks.values().stream().anyMatch(v -> v == null))
            throw new IllegalArgumentException("marks must have non-null values within [min, max]");
    }

    private void updateInput() {
        input.setText(format(getValue()));
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        for (Node current = node; current != null; current = current.getParent())
            if (current == ancestor) return true;
        return false;
    }

    private void handleSceneMousePressed(MouseEvent event) {
        if (!input.isFocused() || !(event.getTarget()instanceof Node node) || isDescendantOf(node, inputBox)) return;
        inputBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
        requestFocus();
    }

    private void commitInput() {
        try {
            setValue(Double.parseDouble(input.getText()));
            emit(EleFXSliderEvent.CHANGE, getValue(), getValue(), getUpperValue(), getUpperValue());
        } catch (NumberFormatException ignored) {
            updateInput();
        }
    }

    private void keyPressed(KeyEvent e) {
        if (isDisabled()) return;
        boolean upper = isRange() && e.isShiftDown();
        double base = upper ? getUpperValue() : getValue(), next = base;
        if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.UP)
            next += getStep();
        else if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.DOWN)
            next -= getStep();
        else if (e.getCode() == KeyCode.HOME)
            next = getMin();
        else if (e.getCode() == KeyCode.END)
            next = getMax();
        else
            return;
        double old = getValue(), oldU = getUpperValue();
        if (upper)
            setUpperValue(next);
        else
            setValue(next);
        emit(EleFXSliderEvent.CHANGE, old, getValue(), oldU, getUpperValue());
        e.consume();
    }

    private String format(double v) {
        Function<Double, String> f = getFormatTooltip();
        return f == null ? formatNumber(v) : String.valueOf(f.apply(v));
    }

    private String formatNumber(double value) {
        int scale = getPrecision() >= 0
                ? getPrecision()
                : Math.min(12, Math.max(0, BigDecimal.valueOf(getStep()).stripTrailingZeros().scale()));
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    private void updateAccessibleText() {
        Function<Double, String> f = getFormatValueText();
        lowerButton.setAccessibleText((isRange() ? getRangeStartLabel() : getAriaLabel()) + ": "
                + (f == null ? format(getValue()) : f.apply(getValue())));
        upperButton.setAccessibleText(
                getRangeEndLabel() + ": " + (f == null ? format(getUpperValue()) : f.apply(getUpperValue())));
    }

    private void updateSize() {
        rebuild();
    }

    private void emit(javafx.event.EventType<? extends EleFXSliderEvent> type, double old, double now, double oldU,
                      double nowU) {
        EleFXSliderEvent e = new EleFXSliderEvent(this, this, type, old, now, oldU, nowU);
        EventHandler<EleFXSliderEvent> h = type == EleFXSliderEvent.INPUT ? getOnInput() : getOnChange();
        if (h != null) h.handle(e);
        fireEvent(e);
    }
}
