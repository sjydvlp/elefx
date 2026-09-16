package com.sjydvlp.elefx.component.collapse;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Element Plus inspired container for expandable {@link EleFXCollapseItem}s.
 *
 * <p>
 * By default several items can remain open. Set {@link #setAccordion(boolean)} to
 * {@code true} to limit the component to one active item.
 * </p>
 */
public class EleFXCollapse extends VBox implements Themable {

    private final ObservableList<EleFXCollapseItem> items = FXCollections.observableArrayList();

    private final ReadOnlyListWrapper<String> activeNames = new ReadOnlyListWrapper<>(this, "activeNames",
            FXCollections.observableArrayList());

    private final BooleanProperty accordion = new SimpleBooleanProperty(this, "accordion", false);

    private final ObjectProperty<EleFXCollapseIconPosition> expandIconPosition = new SimpleObjectProperty<>(this,
            "expandIconPosition", EleFXCollapseIconPosition.RIGHT);

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this,
            "animationDuration", Duration.millis(200));

    private final ObjectProperty<Supplier<Node>> expandIconFactory = new SimpleObjectProperty<>(this,
            "expandIconFactory");

    private final ObjectProperty<Supplier<Node>> collapseIconFactory = new SimpleObjectProperty<>(this,
            "collapseIconFactory");

    private final ObjectProperty<Callback<EleFXCollapseBeforeChange, Boolean>> beforeCollapse = new SimpleObjectProperty<>(
            this,
            "beforeCollapse");

    private final ObjectProperty<EventHandler<EleFXCollapseEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    public EleFXCollapse() {
        initialize();
    }

    public EleFXCollapse(EleFXCollapseItem... items) {
        this();
        getItems().addAll(items);
    }

    public ObservableList<EleFXCollapseItem> getItems() {
        return items;
    }

    /** Active item identifiers, exposed as a read-only observable list. */
    public ObservableList<String> getActiveNames() {
        return FXCollections.unmodifiableObservableList(activeNames.get());
    }

    public ReadOnlyListProperty<String> activeNamesProperty() {
        return activeNames.getReadOnlyProperty();
    }

    /** Sets active item identifiers. Unknown names are ignored. */
    public void setActiveNames(Collection<String> names) {
        applyActiveNames(names, false);
    }

    public boolean isAccordion() {
        return accordion.get();
    }

    public BooleanProperty accordionProperty() {
        return accordion;
    }

    public void setAccordion(boolean value) {
        accordion.set(value);
    }

    public EleFXCollapseIconPosition getExpandIconPosition() {
        return expandIconPosition.get();
    }

    public ObjectProperty<EleFXCollapseIconPosition> expandIconPositionProperty() {
        return expandIconPosition;
    }

    public void setExpandIconPosition(EleFXCollapseIconPosition value) {
        expandIconPosition.set(value == null ? EleFXCollapseIconPosition.RIGHT : value);
    }

    /** Duration of the item content and expand-icon transitions. Set to {@link Duration#ZERO} to disable them. */
    public Duration getAnimationDuration() {
        return animationDuration.get();
    }

    public ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public void setAnimationDuration(Duration value) {
        if (value == null || value.lessThan(Duration.ZERO))
            throw new IllegalArgumentException("animationDuration must not be null or negative");
        animationDuration.set(value);
    }

    /**
     * Factory for the icon shown by every collapsed item. The supplier must create a fresh node for each item.
     */
    public Supplier<Node> getExpandIconFactory() {
        return expandIconFactory.get();
    }

    public ObjectProperty<Supplier<Node>> expandIconFactoryProperty() {
        return expandIconFactory;
    }

    public void setExpandIconFactory(Supplier<Node> value) {
        expandIconFactory.set(value);
    }

    /**
     * Factory for the icon shown by every expanded item. The supplier must create a fresh node for each item.
     */
    public Supplier<Node> getCollapseIconFactory() {
        return collapseIconFactory.get();
    }

    public ObjectProperty<Supplier<Node>> collapseIconFactoryProperty() {
        return collapseIconFactory;
    }

    public void setCollapseIconFactory(Supplier<Node> value) {
        collapseIconFactory.set(value);
    }

    /** Optional guard invoked before a user-triggered change. Returning {@code false} prevents that change. */
    public Callback<EleFXCollapseBeforeChange, Boolean> getBeforeCollapse() {
        return beforeCollapse.get();
    }

    public ObjectProperty<Callback<EleFXCollapseBeforeChange, Boolean>> beforeCollapseProperty() {
        return beforeCollapse;
    }

    public void setBeforeCollapse(Callback<EleFXCollapseBeforeChange, Boolean> value) {
        beforeCollapse.set(value);
    }

    public EventHandler<EleFXCollapseEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<EleFXCollapseEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<EleFXCollapseEvent> value) {
        onChange.set(value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.COLLAPSE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    void requestToggle(EleFXCollapseItem item) {
        if (item.isDisabled()) return;
        String name = nameOf(item);
        boolean expanding = !activeNames.contains(name);
        List<String> current = List.copyOf(activeNames);
        LinkedHashSet<String> proposed = new LinkedHashSet<>(activeNames);
        if (proposed.contains(name))
            proposed.remove(name);
        else {
            if (isAccordion()) proposed.clear();
            proposed.add(name);
        }
        List<String> next = List.copyOf(proposed);
        Callback<EleFXCollapseBeforeChange, Boolean> guard = getBeforeCollapse();
        if (guard != null && !Boolean.TRUE.equals(guard.call(
                new EleFXCollapseBeforeChange(item, name, expanding, current, next))))
            return;
        applyActiveNames(next, true);
    }

    private void initialize() {
        getStyleClass().add("ele-collapse");
        setFillWidth(true);
        setSpacing(0);
        items.addListener((ListChangeListener<EleFXCollapseItem>) change -> refreshItems());
        accordion.addListener((observable, oldValue, value) -> applyActiveNames(activeNames, false));
        expandIconPosition.addListener((observable, oldValue, value) -> refreshItems());
        animationDuration.addListener((observable, oldValue, value) -> refreshItems());
        expandIconFactory.addListener((observable, oldValue, value) -> refreshItems());
        collapseIconFactory.addListener((observable, oldValue, value) -> refreshItems());
        sceneBuilderIntegration();
    }

    private void refreshItems() {
        getChildren().setAll(items);
        for (int index = 0; index < items.size(); index++) {
            EleFXCollapseItem item = items.get(index);
            item.setIconPosition(getExpandIconPosition());
            item.setAnimationDurationFromCollapse(getAnimationDuration());
            item.setIconsFromCollapse(createIcon(getExpandIconFactory()), createIcon(getCollapseIconFactory()));
            item.getStyleClass().remove("ele-collapse-item--last");
            if (index == items.size() - 1) item.getStyleClass().add("ele-collapse-item--last");
        }
        applyActiveNames(activeNames, false);
    }

    private void applyActiveNames(Collection<String> requested, boolean notify) {
        LinkedHashSet<String> known = new LinkedHashSet<>();
        for (EleFXCollapseItem item : items)
            known.add(nameOf(item));
        LinkedHashSet<String> next = new LinkedHashSet<>();
        if (requested != null) {
            for (String name : requested) {
                if (name != null && known.contains(name)) {
                    next.add(name);
                    if (isAccordion()) break;
                }
            }
        }
        if (next.equals(new LinkedHashSet<>(activeNames))) return;
        activeNames.setAll(next);
        for (EleFXCollapseItem item : items)
            item.setExpandedFromCollapse(next.contains(nameOf(item)));
        if (notify) {
            EleFXCollapseEvent event = new EleFXCollapseEvent(this, this, List.copyOf(next));
            fireEvent(event);
            if (getOnChange() != null) getOnChange().handle(event);
        }
    }

    private String nameOf(EleFXCollapseItem item) {
        int index = items.indexOf(item);
        return item.getName().isBlank() ? Integer.toString(index) : item.getName();
    }

    private static Node createIcon(Supplier<Node> factory) {
        return factory == null ? null : factory.get();
    }
}
