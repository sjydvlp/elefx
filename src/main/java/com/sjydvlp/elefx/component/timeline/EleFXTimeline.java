package com.sjydvlp.elefx.component.timeline;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

/** Element Plus inspired vertical timeline composed from {@link EleFXTimelineItem}s. */
public class EleFXTimeline extends VBox implements Themable {

    private final BooleanProperty reverse = new SimpleBooleanProperty(this, "reverse", false);

    private final ObjectProperty<EleFXTimelineMode> mode = new SimpleObjectProperty<>(this, "mode",
            EleFXTimelineMode.START);

    private final ObservableList<EleFXTimelineItem> items = FXCollections.observableArrayList();

    public EleFXTimeline() {
        initialize();
    }

    public EleFXTimeline(EleFXTimelineItem... items) {
        this();
        getItems().addAll(items);
    }

    public boolean isReverse() {
        return reverse.get();
    }

    public BooleanProperty reverseProperty() {
        return reverse;
    }

    public void setReverse(boolean value) {
        reverse.set(value);
    }

    public EleFXTimelineMode getMode() {
        return mode.get();
    }

    public ObjectProperty<EleFXTimelineMode> modeProperty() {
        return mode;
    }

    public void setMode(EleFXTimelineMode value) {
        mode.set(value == null ? EleFXTimelineMode.START : value);
    }

    public ObservableList<EleFXTimelineItem> getItems() {
        return items;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TIMELINE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-timeline");
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        reverse.addListener(ignored -> rebuild());
        mode.addListener(ignored -> rebuild());
        items.addListener((ListChangeListener<EleFXTimelineItem>) ignored -> rebuild());
        rebuild();
        sceneBuilderIntegration();
    }

    private void rebuild() {
        getChildren().clear();
        getStyleClass().removeIf(value -> value.startsWith("ele-timeline--mode-"));
        getStyleClass().add("ele-timeline--mode-" + getMode().name().toLowerCase().replace('_', '-'));
        boolean alternating = getMode() == EleFXTimelineMode.ALTERNATE
                || getMode() == EleFXTimelineMode.ALTERNATE_REVERSE;
        for (int displayIndex = 0; displayIndex < items.size(); displayIndex++) {
            int sourceIndex = isReverse() ? items.size() - 1 - displayIndex : displayIndex;
            EleFXTimelineItem item = items.get(sourceIndex);
            boolean end = switch (getMode()) {
                case END -> true;
                // Element Plus places the first ALTERNATE item on the rail's right.
                case ALTERNATE -> displayIndex % 2 == 0;
                case ALTERNATE_REVERSE -> displayIndex % 2 != 0;
                case START -> false;
            };
            item.configure(end, displayIndex == items.size() - 1, alternating);
            getChildren().add(item);
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        getChildren().forEach(node -> ((EleFXTimelineItem) node).alignCenteredNode());
        for (int index = 0; index + 1 < getChildren().size(); index++) {
            EleFXTimelineItem item = (EleFXTimelineItem) getChildren().get(index);
            EleFXTimelineItem next = (EleFXTimelineItem) getChildren().get(index + 1);
            item.connectTailTo(item.nodeCenterY(), next.getLayoutY() + next.nodeCenterY() - item.getLayoutY());
        }
    }
}
