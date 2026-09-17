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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

    private final StackPane circleBox = new StackPane();

    private final Arc circleBackground = new Arc();

    private final Arc circleBar = new Arc();

    private final Label text = new Label();

    private final EleFXIcon statusIcon = new EleFXIcon(EleFXIconType.CIRCLE_CHECK, 16);

    /** Element Plus reserves a constant trailing area, keeping all tracks aligned. */
    private final StackPane textSlot = new StackPane();

    private final DoubleProperty percentage = new SimpleDoubleProperty(this, "percentage", 0);

    private final ObjectProperty<EleFXProgressType> type = new SimpleObjectProperty<>(this, "type",
            EleFXProgressType.LINE);

    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(this, "strokeWidth", 6);

    private final BooleanProperty textInside = new SimpleBooleanProperty(this, "textInside", false);

    private final ObjectProperty<EleFXProgressStatus> status = new SimpleObjectProperty<>(this, "status");

    private final BooleanProperty indeterminate = new SimpleBooleanProperty(this, "indeterminate", false);

    private final DoubleProperty duration = new SimpleDoubleProperty(this, "duration", 3);

    private final ObjectProperty<Paint> color = new SimpleObjectProperty<>(this, "color");

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
        lineTrack.getChildren().addAll(lineBackground, lineBar);
        HBox.setHgrow(lineTrack, Priority.ALWAYS);
        textSlot.getStyleClass().add("ele-progress__text-slot");
        textSlot.setAlignment(Pos.CENTER_LEFT);
        textSlot.setMinWidth(50);
        textSlot.setPrefWidth(50);
        textSlot.setMaxWidth(50);
        lineBox.getChildren().addAll(lineTrack, textSlot);
        circleBox.getStyleClass().add("ele-progress--circle");
        circleBackground.getStyleClass().add("ele-progress-circle__track");
        circleBar.getStyleClass().add("ele-progress-circle__path");
        for (Arc arc : new Arc[] {circleBackground, circleBar}) {
            arc.setType(ArcType.OPEN);
            arc.setFill(Color.TRANSPARENT);
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
        showText.addListener((o, a, b) -> refresh());
        format.addListener((o, a, b) -> refresh());
        content.addListener((o, a, b) -> refresh());
        strokeLineCap.addListener((o, a, b) -> refresh());
        indeterminate.addListener((o, a, b) -> restartAnimation());
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
        text.setText(labelText());
        Paint paint = progressPaint();
        lineBar.setFill(paint);
        circleBar.setStroke(paint);
        circleBar.setStrokeLineCap(getStrokeLineCap());
        updateStatusIcon();
        updateTextNode(line);
        requestLayout();
    }

    private void updateTextNode(boolean line) {
        Node display = displayNode();
        // A label/icon can move between the line and circle containers when
        // type changes, so detach it before assigning its new parent.
        lineBox.getChildren().setAll(lineTrack);
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
        return !isTextInside() && getStatus() != null ? statusIcon : text;
    }

    private void updateStatusIcon() {
        EleFXProgressStatus value = getStatus();
        if (value == null) return;
        switch (value) {
            case SUCCESS -> statusIcon.setType(EleFXIconType.CIRCLE_CHECK);
            case WARNING -> statusIcon.setType(EleFXIconType.WARNING_FILLED);
            case EXCEPTION -> statusIcon.setType(EleFXIconType.CIRCLE_CLOSE);
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
            double y = Math.max(0, (h - getStrokeWidth()) / 2);
            lineTrack.resizeRelocate(lineTrack.getLayoutX(), y, trackWidth, getStrokeWidth());
            lineBackground.setWidth(trackWidth);
            lineBackground.setHeight(getStrokeWidth());
            lineBar.setWidth(trackWidth * displayedFraction());
            lineBar.setHeight(getStrokeWidth());
            double radius = getStrokeWidth() / 2;
            lineBackground.setArcWidth(radius * 2);
            lineBackground.setArcHeight(radius * 2);
            lineBar.setArcWidth(radius * 2);
            lineBar.setArcHeight(radius * 2);
            if (isTextInside() && isShowText() && lineTrack.getChildren().size() > 2) {
                Node label = lineTrack.getChildren().get(2);
                double labelWidth = label.prefWidth(-1);
                double labelHeight = label.prefHeight(-1);
                // Position the label against the filled segment rather than the
                // whole track, matching Element Plus's text-inside treatment.
                label.resizeRelocate(Math.max(0, lineBar.getWidth() - labelWidth - 6),
                        (getStrokeWidth() - labelHeight) / 2, labelWidth, labelHeight);
            }
        } else {
            double size = Math.min(Math.min(w, h), getCircleSize());
            double x = (w - size) / 2, y = (h - size) / 2, radius = (size - getStrokeWidth()) / 2;
            circleBox.resizeRelocate(x, y, size, size);
            double start = getType() == EleFXProgressType.DASHBOARD ? 225 : 90;
            double length = getType() == EleFXProgressType.DASHBOARD ? -270 : -360;
            for (Arc arc : new Arc[] {circleBackground, circleBar}) {
                arc.setCenterX(x + size / 2);
                arc.setCenterY(y + size / 2);
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
                label.resizeRelocate(x, y, size, size);
            }
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return getType() == EleFXProgressType.LINE ? 300 : getCircleSize();
    }

    @Override
    protected double computePrefHeight(double width) {
        return getType() == EleFXProgressType.LINE ? Math.max(18, getStrokeWidth()) : getCircleSize();
    }

    private double displayedFraction() {
        return isIndeterminate() && getType() == EleFXProgressType.LINE ? .35 : getPercentage() / 100d;
    }

    private String labelText() {
        Function<Double, String> formatter = getFormat();
        return formatter == null ? Math.round(getPercentage()) + "%" : formatter.apply(getPercentage());
    }

    private Paint progressPaint() {
        if (getColor() != null || getStatus() == null) return getColor() == null ? PRIMARY : getColor();
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
        if (!isIndeterminate()) {
            animationOffset = 0;
            refresh();
            return;
        }
        animation = new Timeline(new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.seconds(getDuration()), event -> animationOffset = 0));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.currentTimeProperty().addListener((o, a, b) -> {
            animationOffset = b.toMillis() / (getDuration() * 1000d);
            if (getType() == EleFXProgressType.LINE) lineBar
                    .setTranslateX((lineTrack.getWidth() + lineBar.getWidth()) * animationOffset - lineBar.getWidth());
        });
        animation.play();
        refresh();
    }
}
