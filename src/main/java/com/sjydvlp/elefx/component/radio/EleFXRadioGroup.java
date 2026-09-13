package com.sjydvlp.elefx.component.radio;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A value-backed, mutually exclusive collection of {@link EleFXRadio}s.
 * Changes made through either {@link #valueProperty()} or an individual radio
 * are immediately synchronized in both directions.
 */
public class EleFXRadioGroup<T> extends FlowPane implements Themable {

    private static final String STYLE_CLASS = "ele-radio-group";

    private static final String BUTTON_GROUP_STYLE_CLASS = "ele-radio-group--button";

    private static final String BUTTON_FIRST_STYLE_CLASS = "ele-radio-button--first";

    private static final String BUTTON_MIDDLE_STYLE_CLASS = "ele-radio-button--middle";

    private static final String BUTTON_LAST_STYLE_CLASS = "ele-radio-button--last";

    private static final String SIZE_STYLE_PREFIX = "ele-radio-group--size-";

    private final ObservableList<EleFXRadio<T>> radios = FXCollections.observableArrayList();

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<EleFXRadioSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXRadioSize.DEFAULT);

    private final BooleanProperty validateEvent = new SimpleBooleanProperty(this, "validateEvent", true);

    private final StringProperty fill = new SimpleStringProperty(this, "fill", "#409eff");

    private final StringProperty textColor = new SimpleStringProperty(this, "textColor", "#ffffff");

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "");

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private final Map<EleFXRadio<T>, ChangeListener<Boolean>> selectionListeners = new IdentityHashMap<>();

    private final Map<EleFXRadio<T>, Boolean> disabledBeforeGroup = new IdentityHashMap<>();

    private final Map<EleFXRadio<T>, EventHandler<KeyEvent>> navigationHandlers = new IdentityHashMap<>();

    private boolean synchronizing;

    public EleFXRadioGroup() {
        initialize();
    }

    @SafeVarargs
    public EleFXRadioGroup(EleFXRadio<T>... radios) {
        this();
        getRadios().addAll(radios);
    }

    public ObservableList<EleFXRadio<T>> getRadios() {
        return radios;
    }

    public T getValue() {
        return value.get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    /** Alias for JavaFX callers that prefer an explicit selected-value name. */
    public T getSelectedValue() {
        return getValue();
    }

    public ObjectProperty<T> selectedValueProperty() {
        return valueProperty();
    }

    public void setSelectedValue(T value) {
        setValue(value);
    }

    public EleFXRadioSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXRadioSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXRadioSize size) {
        this.size.set(size == null ? EleFXRadioSize.DEFAULT : size);
    }

    public boolean isValidateEvent() {
        return validateEvent.get();
    }

    public BooleanProperty validateEventProperty() {
        return validateEvent;
    }

    public void setValidateEvent(boolean validateEvent) {
        this.validateEvent.set(validateEvent);
    }

    public String getFill() {
        return fill.get();
    }

    public StringProperty fillProperty() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill.set(fill == null || fill.isBlank() ? "#409eff" : fill);
    }

    public String getTextColor() {
        return textColor.get();
    }

    public StringProperty textColorProperty() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor.set(textColor == null || textColor.isBlank() ? "#ffffff" : textColor);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name == null ? "" : name);
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public void setAriaLabel(String ariaLabel) {
        this.ariaLabel.set(ariaLabel == null ? "" : ariaLabel);
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
        return EleFXThemes.RADIO;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setColumnHalignment(HPos.LEFT);
        updateCustomColors();
        fill.addListener(observable -> updateCustomColors());
        textColor.addListener(observable -> updateCustomColors());
        updateSizeStyle(null, getSize());
        size.addListener((observable, oldSize, newSize) -> updateSizeStyle(oldSize, newSize));
        radios.addListener((ListChangeListener<EleFXRadio<T>>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) change.getRemoved().forEach(this::detach);
                if (change.wasAdded()) change.getAddedSubList().forEach(this::attach);
            }
            getChildren().setAll(radios);
            updateButtonGroupStyle();
            syncSelectionFromValue();
        });
        value.addListener((observable, oldValue, newValue) -> {
            if (!synchronizing) {
                syncSelectionFromValue();
                if (!Objects.equals(oldValue, newValue)) fireChange();
            }
        });
        disableProperty().addListener((observable, oldValue, disabled) -> updateDisabledState(disabled));
        sceneBuilderIntegration();
    }

    private void attach(EleFXRadio<T> radio) {
        ChangeListener<Boolean> listener = (observable, oldValue, selected) -> radioSelectionChanged(radio, selected);
        selectionListeners.put(radio, listener);
        radio.selectedProperty().addListener(listener);
        EventHandler<KeyEvent> navigationHandler = event -> navigate(radio, event);
        navigationHandlers.put(radio, navigationHandler);
        radio.addEventHandler(KeyEvent.KEY_PRESSED, navigationHandler);
        if (isDisable()) {
            disabledBeforeGroup.put(radio, radio.isDisable());
            radio.setDisable(true);
        }
        setSelectedSilently(radio, Objects.equals(getValue(), radio.getValue()));
    }

    private void detach(EleFXRadio<T> radio) {
        ChangeListener<Boolean> listener = selectionListeners.remove(radio);
        if (listener != null) radio.selectedProperty().removeListener(listener);
        EventHandler<KeyEvent> navigationHandler = navigationHandlers.remove(radio);
        if (navigationHandler != null) radio.removeEventHandler(KeyEvent.KEY_PRESSED, navigationHandler);
        Boolean previousDisabled = disabledBeforeGroup.remove(radio);
        if (previousDisabled != null) radio.setDisable(previousDisabled);
    }

    private void radioSelectionChanged(EleFXRadio<T> radio, boolean selected) {
        if (synchronizing) return;
        // A selected radio cannot be unselected by a second click; it remains
        // selected until another member (or an external value) replaces it.
        if (!selected && Objects.equals(getValue(), radio.getValue())) {
            setSelectedSilently(radio, true);
            return;
        }
        if (selected) {
            synchronizing = true;
            try {
                for (EleFXRadio<T> candidate : radios) {
                    if (candidate != radio) candidate.setSelected(false);
                }
                value.set(radio.getValue());
            } finally {
                synchronizing = false;
            }
            fireChange();
        }
    }

    private void syncSelectionFromValue() {
        synchronizing = true;
        try {
            for (EleFXRadio<T> radio : radios) {
                radio.setSelected(Objects.equals(getValue(), radio.getValue()));
            }
        } finally {
            synchronizing = false;
        }
    }

    private void setSelectedSilently(EleFXRadio<T> radio, boolean selected) {
        synchronizing = true;
        try {
            radio.setSelected(selected);
        } finally {
            synchronizing = false;
        }
    }

    private void updateDisabledState(boolean disabled) {
        for (EleFXRadio<T> radio : radios) {
            if (disabled) {
                disabledBeforeGroup.put(radio, radio.isDisable());
                radio.setDisable(true);
            } else {
                radio.setDisable(disabledBeforeGroup.getOrDefault(radio, false));
            }
        }
        if (!disabled) disabledBeforeGroup.clear();
    }

    private void updateButtonGroupStyle() {
        boolean buttonGroup = !radios.isEmpty() && radios.stream().allMatch(EleFXRadioButton.class::isInstance);
        if (buttonGroup && !getStyleClass().contains(BUTTON_GROUP_STYLE_CLASS))
            getStyleClass().add(BUTTON_GROUP_STYLE_CLASS);
        if (!buttonGroup) getStyleClass().remove(BUTTON_GROUP_STYLE_CLASS);
        for (int index = 0; index < radios.size(); index++) {
            EleFXRadio<T> radio = radios.get(index);
            radio.getStyleClass().removeAll(BUTTON_FIRST_STYLE_CLASS, BUTTON_MIDDLE_STYLE_CLASS,
                    BUTTON_LAST_STYLE_CLASS);
            if (!buttonGroup) continue;
            radio.getStyleClass().add(index == 0
                    ? BUTTON_FIRST_STYLE_CLASS
                    : index == radios.size() - 1 ? BUTTON_LAST_STYLE_CLASS : BUTTON_MIDDLE_STYLE_CLASS);
        }
    }

    private void updateSizeStyle(EleFXRadioSize oldSize, EleFXRadioSize newSize) {
        if (oldSize != null) getStyleClass().remove(SIZE_STYLE_PREFIX + oldSize.name().toLowerCase());
        EleFXRadioSize effectiveSize = newSize == null ? EleFXRadioSize.DEFAULT : newSize;
        String styleClass = SIZE_STYLE_PREFIX + effectiveSize.name().toLowerCase();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }

    private void navigate(EleFXRadio<T> current, KeyEvent event) {
        if (event.getCode() != KeyCode.LEFT && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN)
            return;
        if (radios.size() < 2) return;
        int currentIndex = radios.indexOf(current);
        int step = event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.UP ? -1 : 1;
        for (int offset = 1; offset < radios.size(); offset++) {
            EleFXRadio<T> candidate = radios.get(Math.floorMod(currentIndex + step * offset, radios.size()));
            if (!candidate.isDisable()) {
                candidate.requestFocus();
                candidate.fire();
                event.consume();
                return;
            }
        }
    }

    private void updateCustomColors() {
        setStyle("-elefx-radio-fill: " + getFill() + "; -elefx-radio-text-color: " + getTextColor() + ";");
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }
}
