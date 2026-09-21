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

/**
 * One item in {@link EleFXSteps}. Text properties are convenient defaults;
 * {@link #titleNodeProperty()}, {@link #descriptionNodeProperty()} and
 * {@link #iconProperty()} accept arbitrary JavaFX nodes, like Element Plus slots.
 */
public class EleFXStep extends Pane {

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

    private final Pane line = new Pane();

    private final VBox text = new VBox();

    private final Label titleLabel = new Label();

    private final Label descriptionLabel = new Label();

    private EleFXStepStatus effectiveStatus = EleFXStepStatus.WAIT;

    private int index;

    private boolean last;

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

    void configure(int index, boolean last, EleFXStepStatus calculatedStatus, EleFXStepsDirection direction,
                   boolean simple, boolean alignCenter) {
        this.index = index;
        this.last = last;
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
        text.getStyleClass().add("ele-step__main");
        titleLabel.getStyleClass().add("ele-step__title");
        descriptionLabel.getStyleClass().add("ele-step__description");
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
        descriptionLabel.setText(getDescription());
        Node visual = getIcon();
        if (visual == null)
            visual = effectiveStatus == EleFXStepStatus.FINISH || effectiveStatus == EleFXStepStatus.SUCCESS
                    ? finishIcon
                    : effectiveStatus == EleFXStepStatus.ERROR ? errorIcon : number;
        number.setText(Integer.toString(index + 1));
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
        if (last) getStyleClass().add("is-last");
        text.setAlignment(alignCenter ? Pos.TOP_CENTER : Pos.TOP_LEFT);
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth(), h = getHeight();
        if (simple) {
            indicator.resizeRelocate(0, 0, 0, 0);
            text.resizeRelocate(0, 0, Math.max(0, w - (last ? 0 : 20)), h);
            line.resizeRelocate(Math.max(0, w - 16), 9, last ? 0 : 12, 2);
            return;
        }
        if (direction == EleFXStepsDirection.VERTICAL) {
            indicator.resizeRelocate(0, 0, 24, 24);
            text.resizeRelocate(36, 2, Math.max(0, w - 36), h - 2);
            line.resizeRelocate(11, 28, 2, Math.max(0, h - 28));
        } else if (alignCenter) {
            indicator.resizeRelocate(Math.max(0, (w - 24) / 2), 0, 24, 24);
            text.resizeRelocate(0, 32, w, Math.max(0, h - 32));
            line.resizeRelocate(w - 2, 11, last ? 0 : Math.max(0, w / 2), 2);
        } else {
            indicator.resizeRelocate(0, 0, 24, 24);
            text.resizeRelocate(36, 2, Math.max(0, w - 36), h - 2);
            line.resizeRelocate(30, 11, last ? 0 : Math.max(0, w - 30), 2);
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        if (simple) return 32;
        double contentWidth = direction == EleFXStepsDirection.VERTICAL || !alignCenter
                ? Math.max(0, width - 36)
                : width;
        return Math.max(24,
                text.prefHeight(contentWidth) + (direction == EleFXStepsDirection.HORIZONTAL && alignCenter ? 32 : 2))
                + (direction == EleFXStepsDirection.VERTICAL ? 24 : 0);
    }
}
