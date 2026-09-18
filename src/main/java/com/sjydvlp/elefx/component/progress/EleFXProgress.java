package com.sjydvlp.elefx.component.progress;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.util.function.Function;

/**
 * Element Plus inspired progress indicator for line, circle and dashboard
 * presentations. Percentage is always constrained to the inclusive 0-100 range.
 */
public class EleFXProgress extends Pane implements Themable {

    private static final Paint PRIMARY = Color.web("#409eff");

    private static final Paint SUCCESS = Color.web("#67c23a");

    private static final Paint WARNING = Color.web("#e6a23c");

    private static final Paint EXCEPTION = Color.web("#f56c6c");

    private final HBox lineBox = new HBox();

    private final Pane lineTrack = new Pane();

    private final Rectangle lineBackground = new Rectangle();

    private final Rectangle lineBar = new Rectangle();

    /** Semi-transparent diagonal bands rendered above the filled line bar. */
    private final Rectangle stripeOverlay = new Rectangle();

    /** JavaFX equivalent of Element Plus's overflow: hidden bar container. */
    private final Rectangle lineClip = new Rectangle();

    private final StackPane circleBox = new StackPane();

    private final Arc circleBackground = new Arc();

    private final Arc circleBar = new Arc();

    private final Label text = new Label();

    private final EleFXIcon statusIcon = new EleFXIcon(EleFXIconType.CIRCLE_CHECK, 16);

    /** Reserves a common trailing area so every line track has the same end. */
    private final StackPane textSlot = new StackPane();

    private final DoubleProperty percentage = new SimpleDoubleProperty(this, "percentage", 0);

    private final ObjectProperty<EleFXProgressType> type = new SimpleObjectProperty<>(this, "type",
            EleFXProgressType.LINE);

    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(this, "strokeWidth", 6);

    private final BooleanProperty textInside = new SimpleBooleanProperty(this, "textInside", false);

    private final ObjectProperty<EleFXProgressStatus> status = new SimpleObjectProperty<>(this, "status");

    private final BooleanProperty indeterminate = new SimpleBooleanProperty(this, "indeterminate", false);

    private final BooleanProperty striped = new SimpleBooleanProperty(this, "striped", false);

    private final BooleanProperty stripedFlow = new SimpleBooleanProperty(this, "stripedFlow", false);

    private final DoubleProperty duration = new SimpleDoubleProperty(this, "duration", 3);

    private final ObjectProperty<Paint> color = new SimpleObjectProperty<>(this, "color");

    private final ObjectProperty<Function<Double, Paint>> colorFunction = new SimpleObjectProperty<>(this,
            "colorFunction");

    private final ObservableList<EleFXProgressColorStop> colorStops = FXCollections.observableArrayList();

    private final DoubleProperty circleSize = new SimpleDoubleProperty(this, "circleSize", 126);

    private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);

    private final ObjectProperty<StrokeLineCap> strokeLineCap = new SimpleObjectProperty<>(this, "strokeLineCap",
            StrokeLineCap.ROUND);

    private final ObjectProperty<Function<Double, String>> format = new SimpleObjectProperty<>(this, "format");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private Timeline animation;

    private double animationOffset;

    public EleFXProgress() {
        getStyleClass().add("ele-progress");
        lineBox.getStyleClass().add("ele-progress--line");
        lineBox.setAlignment(Pos.CENTER_LEFT);
        lineBox.setSpacing(8);
        lineTrack.getStyleClass().add("ele-progress-bar__outer");
        lineBackground.getStyleClass().add("ele-progress-bar__background");
        lineBar.getStyleClass().add("ele-progress-bar__inner");
        stripeOverlay.getStyleClass().add("ele-progress-bar__stripe");
        stripeOverlay.setMouseTransparent(true);
        lineTrack.getChildren().addAll(lineBackground, lineBar);
        lineTrack.setClip(lineClip);
        HBox.setHgrow(lineTrack, Priority.ALWAYS);
        textSlot.getStyleClass().add("ele-progress__text-slot");
        textSlot.setAlignment(Pos.CENTER_LEFT);
        textSlot.setMinWidth(40);
        textSlot.setPrefWidth(40);
        textSlot.setMaxWidth(40);
        lineBox.getChildren().addAll(lineTrack, textSlot);
        circleBox.getStyleClass().add("ele-progress--circle");
        circleBackground.getStyleClass().add("ele-progress-circle__track");
        circleBar.getStyleClass().add("ele-progress-circle__path");
        for (Arc arc : new Arc[] {circleBackground, circleBar}) {
            arc.setType(ArcType.OPEN);
            arc.setFill(Color.TRANSPARENT);
            // The arcs use explicitly calculated local coordinates. Leaving
            // them managed would let StackPane apply a second relocation.
            arc.setManaged(false);
        }
        circleBox.getChildren().addAll(circleBackground, circleBar, text);
        text.getStyleClass().add("ele-progress__text");
        text.setMouseTransparent(true);
        statusIcon.getStyleClass().add("ele-progress__status-icon");
        statusIcon.setMouseTransparent(true);
        getChildren().add(lineBox);
        percentage.addListener((o, a, b) -> {
            if (!b.equals(clamp(b.doubleValue())))
                percentage.set(clamp(b.doubleValue()));
            else
                refresh();
        });
        type.addListener((o, a, b) -> refresh());
        strokeWidth.addListener((o, a, b) -> {
            if (b.doubleValue() <= 0)
                strokeWidth.set(6);
            else
                refresh();
        });
        circleSize.addListener((o, a, b) -> {
            if (b.doubleValue() <= 0)
                circleSize.set(126);
            else
                refresh();
        });
        duration.addListener((o, a, b) -> restartAnimation());
        textInside.addListener((o, a, b) -> refresh());
        status.addListener((o, a, b) -> refresh());
        color.addListener((o, a, b) -> refresh());
        colorFunction.addListener((o, a, b) -> refresh());
        colorStops.addListener((javafx.collections.ListChangeListener<EleFXProgressColorStop>) change -> refresh());
        showText.addListener((o, a, b) -> refresh());
        format.addListener((o, a, b) -> refresh());
        content.addListener((o, a, b) -> refresh());
        strokeLineCap.addListener((o, a, b) -> refresh());
        indeterminate.addListener((o, a, b) -> restartAnimation());
        striped.addListener((o, a, b) -> refresh());
        stripedFlow.addListener((o, a, b) -> restartAnimation());
        refresh();
        sceneBuilderIntegration();
    }

    public EleFXProgress(double percentage) {
        this();
        setPercentage(percentage);
    }

    public double getPercentage() {
        return percentage.get();
    }

    public void setPercentage(double value) {
        percentage.set(clamp(value));
    }

    public DoubleProperty percentageProperty() {
        return percentage;
    }

    public EleFXProgressType getType() {
        return type.get();
    }

    public void setType(EleFXProgressType value) {
        type.set(value == null ? EleFXProgressType.LINE : value);
    }

    public ObjectProperty<EleFXProgressType> typeProperty() {
        return type;
    }

    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    public void setStrokeWidth(double value) {
        strokeWidth.set(value <= 0 ? 6 : value);
    }

    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    public boolean isTextInside() {
        return textInside.get();
    }

    public void setTextInside(boolean value) {
        textInside.set(value);
    }

    public BooleanProperty textInsideProperty() {
        return textInside;
    }

    public EleFXProgressStatus getStatus() {
        return status.get();
    }

    public void setStatus(EleFXProgressStatus value) {
        status.set(value);
    }

    public ObjectProperty<EleFXProgressStatus> statusProperty() {
        return status;
    }

    public boolean isIndeterminate() {
        return indeterminate.get();
    }

    public void setIndeterminate(boolean value) {
        indeterminate.set(value);
    }

    public BooleanProperty indeterminateProperty() {
        return indeterminate;
    }

    public boolean isStriped() {
        return striped.get();
    }

    public void setStriped(boolean value) {
        if (!value) stripedFlow.set(false);
        striped.set(value);
    }

    public BooleanProperty stripedProperty() {
        return striped;
    }

    public boolean isStripedFlow() {
        return stripedFlow.get();
    }

    /** Enables flowing diagonal bands and, like Element Plus, also enables stripes. */
    public void setStripedFlow(boolean value) {
        if (value) setStriped(true);
        stripedFlow.set(value);
    }

    public BooleanProperty stripedFlowProperty() {
        return stripedFlow;
    }

    public double getDuration() {
        return duration.get();
    }

    public void setDuration(double value) {
        duration.set(value <= 0 ? 3 : value);
    }

    public DoubleProperty durationProperty() {
        return duration;
    }

    public Paint getColor() {
        return color.get();
    }

    public void setColor(Paint value) {
        color.set(value);
    }

    public ObjectProperty<Paint> colorProperty() {
        return color;
    }

    /**
     * Returns the percentage-to-colour resolver used when no fixed colour has
     * been supplied with {@link #setColor(Paint)}.
     */
    public Function<Double, Paint> getColorFunction() {
        return colorFunction.get();
    }

    public void setColorFunction(Function<Double, Paint> value) {
        colorFunction.set(value);
    }

    public ObjectProperty<Function<Double, Paint>> colorFunctionProperty() {
        return colorFunction;
    }

    /**
     * Mutable percentage colour stops. Their order is not significant: the
     * progress control sorts them before resolving a current colour.
     */
    public ObservableList<EleFXProgressColorStop> getColorStops() {
        return colorStops;
    }

    public void setColorStops(EleFXProgressColorStop... values) {
        colorStops.setAll(values == null ? new EleFXProgressColorStop[0] : values);
    }

    public void clearColorStops() {
        colorStops.clear();
    }

    /** Diameter of circular and dashboard progress indicators. */
    public double getCircleSize() {
        return circleSize.get();
    }

    public void setCircleSize(double value) {
        circleSize.set(value <= 0 ? 126 : value);
    }

    public DoubleProperty circleSizeProperty() {
        return circleSize;
    }

    public boolean isShowText() {
        return showText.get();
    }

    public void setShowText(boolean value) {
        showText.set(value);
    }

    public BooleanProperty showTextProperty() {
        return showText;
    }

    public StrokeLineCap getStrokeLineCap() {
        return strokeLineCap.get();
    }

    public void setStrokeLineCap(StrokeLineCap value) {
        strokeLineCap.set(value == null ? StrokeLineCap.ROUND : value);
    }

    public ObjectProperty<StrokeLineCap> strokeLineCapProperty() {
        return strokeLineCap;
    }

    public Function<Double, String> getFormat() {
        return format.get();
    }

    public void setFormat(Function<Double, String> value) {
        format.set(value);
    }

    public ObjectProperty<Function<Double, String>> formatProperty() {
        return format;
    }

    /** Replaces the percentage label with arbitrary JavaFX content. */
    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.PROGRESS;
    }

    private void refresh() {
        boolean line = getType() == EleFXProgressType.LINE;
        getChildren().setAll(line ? lineBox : circleBox);
        updateLineTrackHeight();
        text.setText(labelText());
        Paint paint = progressPaint();
        lineBar.setFill(paint);
        if (!isIndeterminate()) lineBar.setTranslateX(0);
        circleBar.setStroke(paint);
        circleBar.setStrokeLineCap(getStrokeLineCap());
        updateStatusIcon();
        updateTextNode(line);
        requestLayout();
    }

    /**
     * Equivalent to Element Plus's flex item whose outer bar has an explicit
     * height. Giving the HBox this constraint lets its CENTER alignment place
     * the bar and the trailing text from one shared layout pass.
     */
    private void updateLineTrackHeight() {
        double height = getStrokeWidth();
        lineTrack.setMinHeight(height);
        lineTrack.setPrefHeight(height);
        lineTrack.setMaxHeight(height);
    }

    private void updateTextNode(boolean line) {
        Node display = displayNode();
        // A label/icon can move between the line and circle containers when
        // type changes, so detach it before assigning its new parent.
        lineBox.getChildren().setAll(lineTrack);
        if (isStriped())
            lineTrack.getChildren().setAll(lineBackground, lineBar, stripeOverlay);
        else
            lineTrack.getChildren().setAll(lineBackground, lineBar);
        textSlot.getChildren().clear();
        circleBox.getChildren().setAll(circleBackground, circleBar);
        if (line) {
            if (isShowText()) {
                if (isTextInside())
                    lineTrack.getChildren().setAll(lineBackground, lineBar, display);
                else {
                    textSlot.getChildren().setAll(display);
                    lineBox.getChildren().add(textSlot);
                }
            }
        }
        if (!line && isShowText()) circleBox.getChildren().add(display);
    }

    private Node displayNode() {
        if (getContent() != null) return getContent();
        // Element Plus replaces circular/dashboard status text with an icon.
        // textInside applies only to line progress, where status content stays
        // inside the filled bar as text.
        boolean useStatusIcon = getStatus() != null
                && (getType() != EleFXProgressType.LINE || !isTextInside());
        return useStatusIcon ? statusIcon : text;
    }

    private void updateStatusIcon() {
        EleFXProgressStatus value = getStatus();
        if (value == null) return;
        boolean line = getType() == EleFXProgressType.LINE;
        switch (value) {
            case SUCCESS -> statusIcon.setType(line ? EleFXIconType.CIRCLE_CHECK : EleFXIconType.CHECK);
            case WARNING -> statusIcon.setType(EleFXIconType.WARNING_FILLED);
            case EXCEPTION -> statusIcon.setType(line ? EleFXIconType.CIRCLE_CLOSE : EleFXIconType.CLOSE);
        }
        statusIcon.setFill(progressPaint());
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (getType() == EleFXProgressType.LINE) {
            lineBox.resizeRelocate(0, 0, w, h);
            lineBox.applyCss();
            lineBox.layout();
            double trackWidth = lineTrack.getWidth();
            lineTrack.resize(trackWidth, getStrokeWidth());
            lineClip.setWidth(trackWidth);
            lineClip.setHeight(getStrokeWidth());
            lineBackground.setWidth(trackWidth);
            lineBackground.setHeight(getStrokeWidth());
            lineBar.setWidth(trackWidth * displayedFraction());
            lineBar.setHeight(getStrokeWidth());
            if (isIndeterminate()) {
                // Element Plus animates the inner bar's left edge from -100%
                // to 100% of the track, independently of bar width.
                lineBar.setTranslateX(trackWidth * (2 * animationOffset - 1));
            }
            double radius = getStrokeWidth() / 2;
            lineClip.setArcWidth(radius * 2);
            lineClip.setArcHeight(radius * 2);
            lineBackground.setArcWidth(radius * 2);
            lineBackground.setArcHeight(radius * 2);
            lineBar.setArcWidth(radius * 2);
            lineBar.setArcHeight(radius * 2);
            updateStripeOverlay(radius);
            if (isTextInside() && isShowText() && lineTrack.getChildren().size() > 2) {
                Node label = lineTrack.getChildren().get(lineTrack.getChildren().size() - 1);
                double labelWidth = label.prefWidth(-1);
                double labelHeight = label.prefHeight(-1);
                // Position the label against the filled segment rather than the
                // whole track, matching Element Plus's text-inside treatment.
                label.resizeRelocate(Math.max(0, lineBar.getWidth() - labelWidth - 6),
                        (getStrokeWidth() - labelHeight) / 2, labelWidth, labelHeight);
            }
        } else {
            double size = Math.min(Math.min(w, h), getCircleSize());
            if (size <= 0) return;
            double x = (w - size) / 2, y = (h - size) / 2;
            // Element Plus calculates its SVG radius in percentage space and
            // rounds it down. Preserve the small outer inset that results,
            // rather than allowing the stroke to touch the canvas edge.
            double relativeStrokeWidth = Math.round(getStrokeWidth() / size * 1000d) / 10d;
            double radius = Math.floor(50 - relativeStrokeWidth / 2) * size / 100d;
            circleBox.resizeRelocate(x, y, size, size);
            double start = getType() == EleFXProgressType.DASHBOARD ? 225 : 90;
            double length = getType() == EleFXProgressType.DASHBOARD ? -270 : -360;
            for (Arc arc : new Arc[] {circleBackground, circleBar}) {
                // Arc coordinates are local to circleBox, which was already
                // relocated above. Adding x/y again offsets both rings.
                arc.setCenterX(size / 2);
                arc.setCenterY(size / 2);
                arc.setRadiusX(radius);
                arc.setRadiusY(radius);
                arc.setStartAngle(start);
                arc.setStrokeWidth(getStrokeWidth());
                arc.setStrokeLineCap(getStrokeLineCap());
            }
            circleBackground.setLength(length);
            circleBar.setLength(length * displayedFraction());
            if (isShowText() && circleBox.getChildren().size() > 2) {
                Node label = circleBox.getChildren().get(2);
                double labelWidth = Math.min(size, label.prefWidth(-1));
                double labelHeight = Math.min(size, label.prefHeight(labelWidth));
                label.resizeRelocate((size - labelWidth) / 2, (size - labelHeight) / 2,
                        labelWidth, labelHeight);
            }
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        if (getType() != EleFXProgressType.LINE) return getCircleSize();
        // Match Element Plus's block-level line progress: when its parent has
        // a concrete width, the control occupies it and lets the bar absorb
        // the remaining space before the trailing text slot.
        Parent parent = getParent();
        double availableWidth = parent == null ? 0 : parent.getLayoutBounds().getWidth();
        return Math.max(300, availableWidth);
    }

    @Override
    protected double computeMaxWidth(double height) {
        // Element Plus renders a line progress as a block-level flex container:
        // it fills the available parent width while the bar grows and the
        // trailing text area remains at the trailing edge. Pane's default maximum
        // is its preferred size, so opt in explicitly to that same behaviour.
        return getType() == EleFXProgressType.LINE ? Double.MAX_VALUE : getCircleSize();
    }

    @Override
    protected double computePrefHeight(double width) {
        return getType() == EleFXProgressType.LINE
                ? Math.max(getStrokeWidth(), lineBox.prefHeight(width))
                : getCircleSize();
    }

    private double displayedFraction() {
        // Element Plus keeps the normal percentage width even while its
        // indeterminate animation moves that width across the track.
        return getPercentage() / 100d;
    }

    private String labelText() {
        Function<Double, String> formatter = getFormat();
        return formatter == null ? Math.round(getPercentage()) + "%" : formatter.apply(getPercentage());
    }

    private void updateStripeOverlay(double radius) {
        if (!isStriped()) return;
        stripeOverlay.setWidth(lineBar.getWidth());
        stripeOverlay.setHeight(lineBar.getHeight());
        stripeOverlay.setArcWidth(radius * 2);
        stripeOverlay.setArcHeight(radius * 2);
        stripeOverlay.setTranslateX(lineBar.getTranslateX());
        // Element Plus animates background-position from -100% to 100%,
        // which advances these diagonal bands towards the right.
        double phase = isStripedFlow() ? 20 * animationOffset : 0;
        // CSS's 45deg points towards the upper right. JavaFX's screen Y axis
        // points down, so reverse the gradient's Y coordinates to match it.
        stripeOverlay.setFill(new LinearGradient(phase, 20, phase + 20, 0, false, CycleMethod.REPEAT,
                new Stop(0, Color.rgb(0, 0, 0, .10)),
                new Stop(.25, Color.rgb(0, 0, 0, .10)),
                new Stop(.25, Color.TRANSPARENT),
                new Stop(.5, Color.TRANSPARENT),
                new Stop(.5, Color.rgb(0, 0, 0, .10)),
                new Stop(.75, Color.rgb(0, 0, 0, .10)),
                new Stop(.75, Color.TRANSPARENT),
                new Stop(1, Color.TRANSPARENT)));
    }

    private Paint progressPaint() {
        if (getColor() != null) return getColor();
        Function<Double, Paint> function = getColorFunction();
        if (function != null) {
            Paint resolved = function.apply(getPercentage());
            if (resolved != null) return resolved;
        }
        if (!colorStops.isEmpty()) {
            return colorStops.stream()
                    .sorted(java.util.Comparator.comparingDouble(EleFXProgressColorStop::getPercentage))
                    .filter(stop -> getPercentage() <= stop.getPercentage())
                    .findFirst()
                    .orElseGet(() -> colorStops.stream()
                            .max(java.util.Comparator.comparingDouble(EleFXProgressColorStop::getPercentage))
                            .orElseThrow())
                    .getColor();
        }
        if (getStatus() == null) return PRIMARY;
        return switch (getStatus()) {
            case SUCCESS -> SUCCESS;
            case WARNING -> WARNING;
            case EXCEPTION -> EXCEPTION;
        };
    }

    private static double clamp(double value) {
        return Double.isFinite(value) ? Math.max(0, Math.min(100, value)) : 0;
    }

    private void restartAnimation() {
        if (animation != null) animation.stop();
        if (!isIndeterminate() && !isStripedFlow()) {
            animationOffset = 0;
            refresh();
            return;
        }
        animation = new Timeline(new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.seconds(getDuration()), event -> animationOffset = 0));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.currentTimeProperty().addListener((o, a, b) -> {
            animationOffset = b.toMillis() / (getDuration() * 1000d);
            if (getType() == EleFXProgressType.LINE) {
                if (isIndeterminate()) {
                    lineBar.setTranslateX(lineTrack.getWidth() * (2 * animationOffset - 1));
                }
                updateStripeOverlay(getStrokeWidth() / 2);
            }
        });
        animation.play();
        refresh();
    }
}
