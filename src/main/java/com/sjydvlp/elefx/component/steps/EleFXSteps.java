package com.sjydvlp.elefx.component.steps;

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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/** Element Plus inspired progress-navigation component. */
public class EleFXSteps extends Pane implements Themable {

    private final IntegerProperty active = new SimpleIntegerProperty(this, "active", 0);

    private final ObjectProperty<EleFXStepStatus> processStatus = new SimpleObjectProperty<>(this, "processStatus",
            EleFXStepStatus.PROCESS);

    private final ObjectProperty<EleFXStepStatus> finishStatus = new SimpleObjectProperty<>(this, "finishStatus",
            EleFXStepStatus.FINISH);

    private final ObjectProperty<EleFXStepsDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXStepsDirection.HORIZONTAL);

    private final BooleanProperty alignCenter = new SimpleBooleanProperty(this, "alignCenter", false);

    private final BooleanProperty simple = new SimpleBooleanProperty(this, "simple", false);

    /** Fixed item width horizontally or height vertically; zero uses automatic sizing. */
    private final DoubleProperty space = new SimpleDoubleProperty(this, "space", 0);

    private final ObservableList<EleFXStep> items = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<EleFXStepsChangeEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    public EleFXSteps() {
        initialize();
    }

    public EleFXSteps(EleFXStep... items) {
        this();
        getItems().addAll(items);
    }

    public int getActive() {
        return active.get();
    }

    public IntegerProperty activeProperty() {
        return active;
    }

    public void setActive(int value) {
        active.set(value);
    }

    public EleFXStepStatus getProcessStatus() {
        return processStatus.get();
    }

    public ObjectProperty<EleFXStepStatus> processStatusProperty() {
        return processStatus;
    }

    public void setProcessStatus(EleFXStepStatus value) {
        processStatus.set(value == null ? EleFXStepStatus.PROCESS : value);
    }

    public EleFXStepStatus getFinishStatus() {
        return finishStatus.get();
    }

    public ObjectProperty<EleFXStepStatus> finishStatusProperty() {
        return finishStatus;
    }

    public void setFinishStatus(EleFXStepStatus value) {
        finishStatus.set(value == null ? EleFXStepStatus.FINISH : value);
    }

    public EleFXStepsDirection getDirection() {
        return direction.get();
    }

    public ObjectProperty<EleFXStepsDirection> directionProperty() {
        return direction;
    }

    public void setDirection(EleFXStepsDirection value) {
        direction.set(value == null ? EleFXStepsDirection.HORIZONTAL : value);
    }

    public boolean isAlignCenter() {
        return alignCenter.get();
    }

    public BooleanProperty alignCenterProperty() {
        return alignCenter;
    }

    public void setAlignCenter(boolean value) {
        alignCenter.set(value);
    }

    public boolean isSimple() {
        return simple.get();
    }

    public BooleanProperty simpleProperty() {
        return simple;
    }

    public void setSimple(boolean value) {
        simple.set(value);
    }

    public double getSpace() {
        return space.get();
    }

    public DoubleProperty spaceProperty() {
        return space;
    }

    public void setSpace(double value) {
        if (!Double.isFinite(value) || value < 0)
            throw new IllegalArgumentException("space must be zero or a positive finite number");
        space.set(value);
    }

    public ObservableList<EleFXStep> getItems() {
        return items;
    }

    public EventHandler<EleFXStepsChangeEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<EleFXStepsChangeEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<EleFXStepsChangeEvent> value) {
        onChange.set(value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.STEPS;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-steps");
        setMaxWidth(Double.MAX_VALUE);
        Rectangle viewport = new Rectangle();
        viewport.widthProperty().bind(widthProperty());
        viewport.heightProperty().bind(heightProperty());
        setClip(viewport);
        active.addListener((observable, oldValue, newValue) -> {
            refresh();
            EleFXStepsChangeEvent event = new EleFXStepsChangeEvent(this, this, oldValue.intValue(),
                    newValue.intValue());
            fireEvent(event);
            if (getOnChange() != null) getOnChange().handle(event);
        });
        processStatus.addListener(ignored -> refresh());
        finishStatus.addListener(ignored -> refresh());
        direction.addListener(ignored -> refresh());
        alignCenter.addListener(ignored -> refresh());
        simple.addListener(ignored -> refresh());
        space.addListener(ignored -> requestLayout());
        items.addListener((ListChangeListener<EleFXStep>) ignored -> refresh());
        refresh();
        sceneBuilderIntegration();
    }

    private void refresh() {
        getChildren().setAll(items);
        getStyleClass().removeIf(value -> value.startsWith("ele-steps--"));
        EleFXStepsDirection actualDirection = isSimple() ? EleFXStepsDirection.HORIZONTAL : getDirection();
        getStyleClass().add("ele-steps--" + actualDirection.name().toLowerCase());
        if (isSimple()) getStyleClass().add("ele-steps--simple");
        if (isAlignCenter() && !isSimple()) getStyleClass().add("ele-steps--center");
        for (int i = 0; i < items.size(); i++)
            items.get(i).configure(i, i == items.size() - 1, i + 1 < getActive(),
                    i < getActive() ? getFinishStatus() : i == getActive() ? getProcessStatus() : EleFXStepStatus.WAIT,
                    actualDirection, isSimple(), isAlignCenter() && !isSimple());
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double x = 0, y = 0, w = getWidth();
        if (!isSimple() && getDirection() == EleFXStepsDirection.VERTICAL) {
            for (int i = 0; i < items.size(); i++) {
                EleFXStep item = items.get(i);
                double h = verticalItemHeight(i, w);
                item.resizeRelocate(0, y, w, h);
                y += h;
            }
        } else {
            double inset = isSimple() ? w * 0.08 : 0;
            x = inset;
            double contentWidth = Math.max(0, w - inset * 2);
            double lastWidth = isSimple() && !items.isEmpty()
                    ? Math.min(contentWidth, items.get(items.size() - 1).prefWidth(-1))
                    : 0;
            double itemWidth = !isSimple() && getSpace() > 0
                    ? Math.min(getSpace(), items.isEmpty() ? 0 : contentWidth / items.size())
                    : isSimple()
                            ? (items.size() < 2 ? 0 : Math.max(0, contentWidth - lastWidth) / (items.size() - 1))
                            : (items.isEmpty() ? 0 : contentWidth / items.size());
            for (int i = 0; i < items.size(); i++) {
                EleFXStep item = items.get(i);
                double width = isSimple() && i == items.size() - 1 ? lastWidth : itemWidth;
                item.resizeRelocate(x, getInsets().getTop(), width,
                        Math.max(0, getHeight() - getInsets().getTop() - getInsets().getBottom()));
                x += width;
            }
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        double availableWidth = width < 0 ? computePrefWidth(-1) : width;
        double itemWidth = !isSimple() && getSpace() > 0
                ? Math.min(getSpace(), items.isEmpty() ? 0 : availableWidth / items.size())
                : items.isEmpty() ? availableWidth : availableWidth / items.size();
        if (!isSimple() && getDirection() == EleFXStepsDirection.VERTICAL) {
            double height = 0;
            for (int i = 0; i < items.size(); i++)
                height += verticalItemHeight(i, availableWidth);
            return height;
        }
        return items.stream().mapToDouble(item -> item.prefHeight(itemWidth)).max().orElse(0)
                + getInsets().getTop() + getInsets().getBottom();
    }

    private double verticalItemHeight(int index, double width) {
        double height = items.get(index).prefHeight(width);
        return index == items.size() - 1 ? height : Math.max(getSpace(), height);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computePrefWidth(double height) {
        return !isSimple() && getDirection() == EleFXStepsDirection.VERTICAL
                ? items.stream().mapToDouble(item -> item.prefWidth(height)).max().orElse(0)
                : !isSimple() && getSpace() > 0 ? getSpace() * items.size() : Math.max(1, items.size()) * 160;
    }
}
