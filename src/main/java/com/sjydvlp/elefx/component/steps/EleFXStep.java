package com.sjydvlp.elefx.component.steps;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/**
 * One item in {@link EleFXSteps}. Text properties are convenient defaults;
 * {@link #titleNodeProperty()}, {@link #descriptionNodeProperty()} and
 * {@link #iconProperty()} accept arbitrary JavaFX nodes, like Element Plus slots.
 */
public class EleFXStep extends Pane {

    private static final double CUSTOM_ICON_SIZE = 25;

    private static final double CUSTOM_ICON_HEAD_WIDTH = 40;

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty description = new SimpleStringProperty(this, "description", "");

    private final ObjectProperty<EleFXStepStatus> status = new SimpleObjectProperty<>(this, "status");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> descriptionNode = new SimpleObjectProperty<>(this, "descriptionNode");

    private final StackPane indicator = new StackPane();

    private final Label number = new Label();

    private final EleFXIcon finishIcon = new EleFXIcon(EleFXIconType.CHECK, 16);

    private final EleFXIcon errorIcon = new EleFXIcon(EleFXIconType.CLOSE, 16);

    private final StackPane line = new StackPane();

    private final SVGPath arrow = new SVGPath();

    private final VBox text = new VBox();

    private final Label titleLabel = new Label();

    private final Label titleWidthProbe = new Label();

    private final Label descriptionLabel = new Label();

    private EleFXStepStatus effectiveStatus = EleFXStepStatus.WAIT;

    private int index;

    private boolean last;

    private boolean lineComplete;

    private EleFXStepsDirection direction = EleFXStepsDirection.HORIZONTAL;

    private boolean simple;

    private boolean alignCenter;

    public EleFXStep() {
        initialize();
    }

    public EleFXStep(String title) {
        this();
        setTitle(title);
    }

    public EleFXStep(String title, String description) {
        this(title);
        setDescription(description);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String value) {
        description.set(value == null ? "" : value);
    }

    /** Explicit state; null lets the parent calculate it from its active index. */
    public EleFXStepStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<EleFXStepStatus> statusProperty() {
        return status;
    }

    public void setStatus(EleFXStepStatus value) {
        status.set(value);
    }

    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public Node getDescriptionNode() {
        return descriptionNode.get();
    }

    public ObjectProperty<Node> descriptionNodeProperty() {
        return descriptionNode;
    }

    public void setDescriptionNode(Node value) {
        descriptionNode.set(value);
    }

    void configure(int index, boolean last, boolean lineComplete, EleFXStepStatus calculatedStatus,
                   EleFXStepsDirection direction,
                   boolean simple, boolean alignCenter) {
        this.index = index;
        this.last = last;
        this.lineComplete = lineComplete;
        this.effectiveStatus = getStatus() == null ? calculatedStatus : getStatus();
        this.direction = direction;
        this.simple = simple;
        this.alignCenter = alignCenter;
        refresh();
    }

    private void initialize() {
        getStyleClass().add("ele-step");
        indicator.getStyleClass().add("ele-step__head");
        number.getStyleClass().add("ele-step__icon-inner");
        finishIcon.getStyleClass().add("ele-step__icon-inner");
        errorIcon.getStyleClass().add("ele-step__icon-inner");
        line.getStyleClass().add("ele-step__line");
        arrow.setContent("M1 1 L7 7 L1 13");
        arrow.getStyleClass().add("ele-step__arrow");
        line.getChildren().add(arrow);
        text.getStyleClass().add("ele-step__main");
        titleLabel.getStyleClass().add("ele-step__title");
        titleWidthProbe.getStyleClass().addAll("ele-step__title", "ele-step__title-width-probe");
        titleWidthProbe.setManaged(false);
        titleWidthProbe.setVisible(false);
        descriptionLabel.getStyleClass().add("ele-step__description");
        getChildren().addAll(line, indicator, text, titleWidthProbe);
        titleLabel.setWrapText(true);
        descriptionLabel.setWrapText(true);
        InvalidationListener refresh = ignored -> refresh();
        title.addListener(refresh);
        description.addListener(refresh);
        status.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                effectiveStatus = newValue;
            }
            refresh();
        });
        icon.addListener(refresh);
        titleNode.addListener(refresh);
        descriptionNode.addListener(refresh);
        refresh();
    }

    private void refresh() {
        titleLabel.setText(getTitle());
        titleWidthProbe.setText(getTitle());
        descriptionLabel.setText(getDescription());
        Node visual = getIcon();
        if (visual instanceof EleFXIcon customIcon && customIcon.getSize() == EleFXIcon.DEFAULT_SIZE)
            customIcon.setSize(CUSTOM_ICON_SIZE);
        if (visual == null)
            visual = effectiveStatus == EleFXStepStatus.FINISH || effectiveStatus == EleFXStepStatus.SUCCESS
                    ? finishIcon
                    : effectiveStatus == EleFXStepStatus.ERROR ? errorIcon : number;
        number.setText(Integer.toString(index + 1));
        arrow.setVisible(simple && !last);
        arrow.setManaged(simple && !last);
        if (getIcon()instanceof EleFXIcon customIcon) {
            customIcon.setScaleX(simple ? 0.72 : 1);
            customIcon.setScaleY(simple ? 0.72 : 1);
        }
        indicator.getChildren().setAll(visual);
        Node actualTitle = getTitleNode() == null ? titleLabel : getTitleNode();
        Node actualDescription = getDescriptionNode() == null ? descriptionLabel : getDescriptionNode();
        boolean showDescription = !simple && (getDescriptionNode() != null || !getDescription().isBlank());
        actualDescription.setManaged(showDescription);
        actualDescription.setVisible(showDescription);
        text.getChildren().setAll(actualTitle, actualDescription);
        getStyleClass().removeIf(value -> value.startsWith("ele-step--") || value.equals("is-last"));
        getStyleClass().add("ele-step--" + effectiveStatus.name().toLowerCase());
        getStyleClass().add("ele-step--" + direction.name().toLowerCase());
        if (simple) getStyleClass().add("ele-step--simple");
        if (alignCenter) getStyleClass().add("ele-step--center");
        if (getIcon() != null) getStyleClass().add("ele-step--custom-icon");
        if (last) getStyleClass().add("is-last");
        if (lineComplete) getStyleClass().add("ele-step--line-complete");
        text.setAlignment(simple ? Pos.CENTER_LEFT : alignCenter ? Pos.TOP_CENTER : Pos.TOP_LEFT);
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (simple) {
            double availableTextWidth = Math.max(0, w - 26);
            double titleWidth = Math.min(simpleTitleWidth(),
                    last ? availableTextWidth : availableTextWidth / 2);
            indicator.resizeRelocate(0, Math.max(0, (h - 16) / 2), 16, 16);
            text.resizeRelocate(26, getTitleNode() == null ? -2 : 0, titleWidth, h);
            line.resizeRelocate(26 + titleWidth, 0,
                    last ? 0 : Math.max(0, availableTextWidth - titleWidth), h);
            return;
        }
        if (direction == EleFXStepsDirection.VERTICAL) {
            indicator.resizeRelocate(0, 0, 24, 24);
            text.resizeRelocate(34, 0, Math.max(0, w - 34), h);
            line.resizeRelocate(11, 24, 2, last ? 0 : Math.max(0, h - 24));
        } else if (alignCenter) {
            double headWidth = getIcon() == null ? 24 : CUSTOM_ICON_HEAD_WIDTH;
            double indicatorX = Math.max(0, (w - headWidth) / 2);
            indicator.resizeRelocate(indicatorX, 0, headWidth, 24);
            text.resizeRelocate(0, 24, w, Math.max(0, h - 24));
            line.resizeRelocate(indicatorX + headWidth, 11, last ? 0 : Math.max(0, w - 24), 2);
        } else {
            double headWidth = getIcon() == null ? 24 : CUSTOM_ICON_HEAD_WIDTH;
            indicator.resizeRelocate(0, 0, headWidth, 24);
            text.resizeRelocate(0, 24, w, Math.max(0, h - 24));
            line.resizeRelocate(headWidth, 11, last ? 0 : Math.max(0, w - headWidth), 2);
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        if (simple) {
            double availableTextWidth = Math.max(0, width - 26);
            double titleWidth = Math.min(simpleTitleWidth(),
                    last ? availableTextWidth : availableTextWidth / 2);
            return Math.max(20, text.prefHeight(titleWidth));
        }
        if (direction == EleFXStepsDirection.HORIZONTAL)
            return Math.max(getDescriptionNode() != null || !getDescription().isBlank() ? 77 : 62,
                    text.prefHeight(width) + 24);
        return Math.max(24, text.prefHeight(Math.max(0, width - 34))) + (last ? 0 : 24);
    }

    @Override
    protected double computePrefWidth(double height) {
        return simple ? 26 + simpleTitleWidth() : super.computePrefWidth(height);
    }

    private double simpleTitleWidth() {
        double width = text.prefWidth(-1);
        return Math.ceil(getTitleNode() == null ? Math.max(width, titleWidthProbe.prefWidth(-1)) : width);
    }
}
