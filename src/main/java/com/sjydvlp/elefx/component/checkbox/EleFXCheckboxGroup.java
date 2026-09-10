package com.sjydvlp.elefx.component.checkbox;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/** A value-backed group of {@link EleFXCheckbox} controls. */
public class EleFXCheckboxGroup<T> extends FlowPane implements Themable {

    private static final String STYLE_CLASS = "ele-checkbox-group";

    private static final String BUTTON_GROUP_STYLE_CLASS = "ele-checkbox-group--button";

    private static final String BUTTON_FIRST_STYLE_CLASS = "ele-checkbox-button--first";

    private static final String BUTTON_MIDDLE_STYLE_CLASS = "ele-checkbox-button--middle";

    private static final String BUTTON_LAST_STYLE_CLASS = "ele-checkbox-button--last";

    private final ObservableList<EleFXCheckable<T>> checkboxes = FXCollections.observableArrayList();

    private final ObservableList<T> values = FXCollections.observableArrayList();

    private final Map<EleFXCheckable<T>, ChangeListener<Boolean>> selectionListeners = new IdentityHashMap<>();

    private final Map<EleFXCheckable<T>, Boolean> disabledBeforeGroup = new IdentityHashMap<>();

    private final IntegerProperty min = new SimpleIntegerProperty(this, "min", 0);

    private final IntegerProperty max = new SimpleIntegerProperty(this, "max", Integer.MAX_VALUE);

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private boolean synchronizing;

    public EleFXCheckboxGroup() {
        initialize();
    }

    @SafeVarargs
    public EleFXCheckboxGroup(EleFXCheckable<T>... checkboxes) {
        this();
        getCheckboxes().addAll(checkboxes);
    }

    /** Preserves the original Checkbox varargs API for existing compiled callers. */
    @SafeVarargs
    public EleFXCheckboxGroup(EleFXCheckbox<T>... checkboxes) {
        this();
        getCheckboxes().addAll(checkboxes);
    }

    public ObservableList<EleFXCheckable<T>> getCheckboxes() {
        return checkboxes;
    }

    /** Mutable selected values, suitable for external bindings and updates. */
    public ObservableList<T> getValues() {
        return values;
    }

    public void setValues(Collection<? extends T> values) {
        this.values.setAll(values == null ? java.util.List.of() : values);
    }

    public int getMin() {
        return min.get();
    }

    public IntegerProperty minProperty() {
        return min;
    }

    public void setMin(int min) {
        this.min.set(Math.max(0, min));
    }

    public int getMax() {
        return max.get();
    }

    public IntegerProperty maxProperty() {
        return max;
    }

    public void setMax(int max) {
        this.max.set(max < 0 ? Integer.MAX_VALUE : max);
    }

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<ActionEvent> handler) {
        onChange.set(handler);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CHECKBOX;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setColumnHalignment(HPos.LEFT);
        checkboxes.addListener((ListChangeListener<EleFXCheckable<T>>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) change.getRemoved().forEach(this::detach);
                if (change.wasAdded()) change.getAddedSubList().forEach(this::attach);
            }
            getChildren().setAll(checkboxes.stream().map(checkbox -> (Node) checkbox).toList());
            updateButtonGroupStyle();
        });
        values.addListener((ListChangeListener<T>) change -> {
            if (!synchronizing) {
                syncSelectionFromValues();
                fireChange();
            }
        });
        disableProperty().addListener((observable, oldValue, newValue) -> updateDisabledState(newValue));
        sceneBuilderIntegration();
    }

    private void attach(EleFXCheckable<T> checkbox) {
        ChangeListener<Boolean> listener = (observable, oldValue, selected) -> checkboxSelectionChanged(checkbox,
                selected);
        selectionListeners.put(checkbox, listener);
        checkbox.selectedProperty().addListener(listener);
        if (isDisable()) {
            disabledBeforeGroup.put(checkbox, checkbox.isDisable());
            checkbox.setDisable(true);
        }
        setSelectedSilently(checkbox, values.contains(checkbox.getValue()));
    }

    private void detach(EleFXCheckable<T> checkbox) {
        ChangeListener<Boolean> listener = selectionListeners.remove(checkbox);
        if (listener != null) checkbox.selectedProperty().removeListener(listener);
        Boolean wasDisabled = disabledBeforeGroup.remove(checkbox);
        if (wasDisabled != null) checkbox.setDisable(wasDisabled);
    }

    private void checkboxSelectionChanged(EleFXCheckable<T> checkbox, boolean selected) {
        if (synchronizing) return;
        T value = checkbox.getValue();
        if (selected && !values.contains(value) && values.size() >= getMax()) {
            setSelectedSilently(checkbox, false);
            return;
        }
        if (!selected && values.contains(value) && values.size() <= getMin()) {
            setSelectedSilently(checkbox, true);
            return;
        }
        synchronizing = true;
        try {
            if (selected && !values.contains(value))
                values.add(value);
            else if (!selected) values.remove(value);
        } finally {
            synchronizing = false;
        }
        fireChange();
    }

    private void syncSelectionFromValues() {
        for (EleFXCheckable<T> checkbox : checkboxes) {
            setSelectedSilently(checkbox, values.contains(checkbox.getValue()));
        }
    }

    private void setSelectedSilently(EleFXCheckable<T> checkbox, boolean selected) {
        synchronizing = true;
        try {
            checkbox.setSelected(selected);
        } finally {
            synchronizing = false;
        }
    }

    private void updateDisabledState(boolean disabled) {
        for (EleFXCheckable<T> checkbox : checkboxes) {
            if (disabled) {
                disabledBeforeGroup.put(checkbox, checkbox.isDisable());
                checkbox.setDisable(true);
            } else {
                checkbox.setDisable(disabledBeforeGroup.getOrDefault(checkbox, false));
            }
        }
        if (!disabled) disabledBeforeGroup.clear();
    }

    private void updateButtonGroupStyle() {
        boolean buttonGroup = !checkboxes.isEmpty()
                && checkboxes.stream().allMatch(EleFXCheckboxButton.class::isInstance);
        if (buttonGroup) {
            if (!getStyleClass().contains(BUTTON_GROUP_STYLE_CLASS)) getStyleClass().add(BUTTON_GROUP_STYLE_CLASS);
        } else {
            getStyleClass().remove(BUTTON_GROUP_STYLE_CLASS);
        }

        for (int index = 0; index < checkboxes.size(); index++) {
            EleFXCheckable<T> checkbox = checkboxes.get(index);
            checkbox.getStyleClass().removeAll(BUTTON_FIRST_STYLE_CLASS, BUTTON_MIDDLE_STYLE_CLASS,
                    BUTTON_LAST_STYLE_CLASS);
            if (!buttonGroup) continue;
            if (index == 0)
                checkbox.getStyleClass().add(BUTTON_FIRST_STYLE_CLASS);
            else if (index == checkboxes.size() - 1)
                checkbox.getStyleClass().add(BUTTON_LAST_STYLE_CLASS);
            else
                checkbox.getStyleClass().add(BUTTON_MIDDLE_STYLE_CLASS);
        }
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }
}
