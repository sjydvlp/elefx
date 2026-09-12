package com.sjydvlp.elefx.component.datepicker;

import com.sjydvlp.elefx.component.datepickerpanel.EleFXDatePickerPanel;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Complete input-and-popup Date Picker modelled after Element Plus DatePicker.
 * <p>
 * The public API lives in this independent package; the calendar panel is an
 * implementation detail shared with the library's standalone panel component.
 * </p>
 */
public class EleFXDatePicker extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-date-picker";

    private final TextField startInput = new TextField();

    private final TextField endInput = new TextField();

    private final Label separator = new Label("-");

    private final Button clearButton = new Button("×");

    private final Button trigger = new Button();

    private final EleFXDatePickerPanel panel = new EleFXDatePickerPanel();

    private final PopupControl popup = new PopupControl();

    private final ObjectProperty<EleFXDatePickerType> type = new SimpleObjectProperty<>(this, "type",
            EleFXDatePickerType.DATE);

    private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(this, "dateTimeValue");

    private final ObservableList<LocalDate> values = FXCollections.observableArrayList();

    private final ObjectProperty<LocalDate> defaultValue = new SimpleObjectProperty<>(this, "defaultValue");

    private final ObjectProperty<List<LocalDateTime>> defaultTime = new SimpleObjectProperty<>(this, "defaultTime",
            List.of());

    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.ENGLISH);

    private final ObjectProperty<Predicate<LocalDate>> disabledDate = new SimpleObjectProperty<>(this, "disabledDate");

    private final ObjectProperty<Function<LocalDate, String>> cellClassName = new SimpleObjectProperty<>(this,
            "cellClassName");

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", true);

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

    private final BooleanProperty readonly = new SimpleBooleanProperty(this, "readonly", false);

    private final BooleanProperty automaticDropdown = new SimpleBooleanProperty(this, "automaticDropdown", true);

    private final BooleanProperty showFooter = new SimpleBooleanProperty(this, "showFooter", true);

    private final BooleanProperty showConfirm = new SimpleBooleanProperty(this, "showConfirm", true);

    private final BooleanProperty showWeekNumber = new SimpleBooleanProperty(this, "showWeekNumber", false);

    private final BooleanProperty singlePanel = new SimpleBooleanProperty(this, "singlePanel", false);

    private final StringProperty placeholder = new SimpleStringProperty(this, "placeholder", "");

    private final StringProperty startPlaceholder = new SimpleStringProperty(this, "startPlaceholder", "");

    private final StringProperty endPlaceholder = new SimpleStringProperty(this, "endPlaceholder", "");

    private final StringProperty rangeSeparator = new SimpleStringProperty(this, "rangeSeparator", "-");

    private final StringProperty format = new SimpleStringProperty(this, "format", "");

    private final ObservableList<EleFXDatePickerShortcut> shortcuts = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onClear = new SimpleObjectProperty<>(this, "onClear");

    private final ObjectProperty<EventHandler<ActionEvent>> onFocus = new SimpleObjectProperty<>(this, "onFocus");

    private final ObjectProperty<EventHandler<ActionEvent>> onBlur = new SimpleObjectProperty<>(this, "onBlur");

    private final ObjectProperty<EventHandler<ActionEvent>> onVisibleChange = new SimpleObjectProperty<>(this,
            "onVisibleChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onCalendarChange = new SimpleObjectProperty<>(this,
            "onCalendarChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onPanelChange = new SimpleObjectProperty<>(this,
            "onPanelChange");

    private boolean synchronizing;

    private boolean popupVisible;

    public EleFXDatePicker() {
        initialize();
    }

    public EleFXDatePicker(LocalDate value) {
        this();
        setValue(value);
    }

    public EleFXDatePickerType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXDatePickerType> typeProperty() {
        return type;
    }

    public void setType(EleFXDatePickerType value) {
        type.set(value == null ? EleFXDatePickerType.DATE : value);
    }

    public LocalDate getValue() {
        return value.get();
    }

    public ObjectProperty<LocalDate> valueProperty() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value.set(value);
    }

    public LocalDateTime getDateTimeValue() {
        return dateTimeValue.get();
    }

    public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
        return dateTimeValue;
    }

    public void setDateTimeValue(LocalDateTime value) {
        dateTimeValue.set(value);
    }

    public ObservableList<LocalDate> getValues() {
        return values;
    }

    public LocalDate getDefaultValue() {
        return defaultValue.get();
    }

    public ObjectProperty<LocalDate> defaultValueProperty() {
        return defaultValue;
    }

    public void setDefaultValue(LocalDate value) {
        defaultValue.set(value);
    }

    public List<LocalDateTime> getDefaultTime() {
        return defaultTime.get();
    }

    public ObjectProperty<List<LocalDateTime>> defaultTimeProperty() {
        return defaultTime;
    }

    public void setDefaultTime(List<LocalDateTime> value) {
        defaultTime.set(value == null ? List.of() : List.copyOf(value));
    }

    public Locale getLocale() {
        return locale.get();
    }

    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public void setLocale(Locale value) {
        locale.set(value == null ? Locale.ENGLISH : value);
    }

    public Predicate<LocalDate> getDisabledDate() {
        return disabledDate.get();
    }

    public ObjectProperty<Predicate<LocalDate>> disabledDateProperty() {
        return disabledDate;
    }

    public void setDisabledDate(Predicate<LocalDate> value) {
        disabledDate.set(value);
    }

    public Function<LocalDate, String> getCellClassName() {
        return cellClassName.get();
    }

    public ObjectProperty<Function<LocalDate, String>> cellClassNameProperty() {
        return cellClassName;
    }

    public void setCellClassName(Function<LocalDate, String> value) {
        cellClassName.set(value);
    }

    public boolean isClearable() {
        return clearable.get();
    }

    public BooleanProperty clearableProperty() {
        return clearable;
    }

    public void setClearable(boolean value) {
        clearable.set(value);
    }

    public boolean isEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public void setEditable(boolean value) {
        editable.set(value);
    }

    public boolean isReadonly() {
        return readonly.get();
    }

    public BooleanProperty readonlyProperty() {
        return readonly;
    }

    public void setReadonly(boolean value) {
        readonly.set(value);
    }

    public boolean isAutomaticDropdown() {
        return automaticDropdown.get();
    }

    public BooleanProperty automaticDropdownProperty() {
        return automaticDropdown;
    }

    public void setAutomaticDropdown(boolean value) {
        automaticDropdown.set(value);
    }

    public boolean isShowFooter() {
        return showFooter.get();
    }

    public BooleanProperty showFooterProperty() {
        return showFooter;
    }

    public void setShowFooter(boolean value) {
        showFooter.set(value);
    }

    public boolean isShowConfirm() {
        return showConfirm.get();
    }

    public BooleanProperty showConfirmProperty() {
        return showConfirm;
    }

    public void setShowConfirm(boolean value) {
        showConfirm.set(value);
    }

    public boolean isShowWeekNumber() {
        return showWeekNumber.get();
    }

    public BooleanProperty showWeekNumberProperty() {
        return showWeekNumber;
    }

    public void setShowWeekNumber(boolean value) {
        showWeekNumber.set(value);
    }

    public boolean isSinglePanel() {
        return singlePanel.get();
    }

    public BooleanProperty singlePanelProperty() {
        return singlePanel;
    }

    public void setSinglePanel(boolean value) {
        singlePanel.set(value);
    }

    public String getPlaceholder() {
        return placeholder.get();
    }

    public StringProperty placeholderProperty() {
        return placeholder;
    }

    public void setPlaceholder(String value) {
        placeholder.set(value == null ? "" : value);
    }

    public String getStartPlaceholder() {
        return startPlaceholder.get();
    }

    public StringProperty startPlaceholderProperty() {
        return startPlaceholder;
    }

    public void setStartPlaceholder(String value) {
        startPlaceholder.set(value == null ? "" : value);
    }

    public String getEndPlaceholder() {
        return endPlaceholder.get();
    }

    public StringProperty endPlaceholderProperty() {
        return endPlaceholder;
    }

    public void setEndPlaceholder(String value) {
        endPlaceholder.set(value == null ? "" : value);
    }

    public String getRangeSeparator() {
        return rangeSeparator.get();
    }

    public StringProperty rangeSeparatorProperty() {
        return rangeSeparator;
    }

    public void setRangeSeparator(String value) {
        rangeSeparator.set(value == null ? "-" : value);
    }

    public String getFormat() {
        return format.get();
    }

    public StringProperty formatProperty() {
        return format;
    }

    public void setFormat(String value) {
        format.set(value == null ? "" : value);
    }

    public ObservableList<EleFXDatePickerShortcut> getShortcuts() {
        return shortcuts;
    }

    public TextField getStartInput() {
        return startInput;
    }

    public TextField getEndInput() {
        return endInput;
    }

    public EleFXDatePickerPanel getPanel() {
        return panel;
    }

    public boolean isShowing() {
        return popup.isShowing();
    }

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<ActionEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onClearProperty() {
        return onClear;
    }

    public void setOnClear(EventHandler<ActionEvent> value) {
        onClear.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onFocusProperty() {
        return onFocus;
    }

    public void setOnFocus(EventHandler<ActionEvent> value) {
        onFocus.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onBlurProperty() {
        return onBlur;
    }

    public void setOnBlur(EventHandler<ActionEvent> value) {
        onBlur.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onVisibleChangeProperty() {
        return onVisibleChange;
    }

    public void setOnVisibleChange(EventHandler<ActionEvent> value) {
        onVisibleChange.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onCalendarChangeProperty() {
        return onCalendarChange;
    }

    public void setOnCalendarChange(EventHandler<ActionEvent> value) {
        onCalendarChange.set(value);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onPanelChangeProperty() {
        return onPanelChange;
    }

    public void setOnPanelChange(EventHandler<ActionEvent> value) {
        onPanelChange.set(value);
    }

    /** Opens the calendar popup. Equivalent to Element Plus {@code handleOpen}. */
    public void show() {
        if (isDisable() || popup.isShowing() || getScene() == null) return;
        Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds != null) popup.show(this, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    public void handleOpen() {
        show();
    }

    /** Closes the calendar popup. Equivalent to Element Plus {@code handleClose}. */
    public void hide() {
        popup.hide();
    }

    public void handleClose() {
        hide();
    }

    public void focus() {
        startInput.requestFocus();
    }

    public void blur() {
        Platform.runLater(() -> {
            if (getScene() != null && getScene().getRoot() != null) getScene().getRoot().requestFocus();
        });
    }

    public void clear() {
        if (values.isEmpty() && getValue() == null && getDateTimeValue() == null) return;
        synchronizing = true;
        try {
            values.clear();
            value.set(null);
            dateTimeValue.set(null);
            panel.getValues().clear();
        } finally {
            synchronizing = false;
        }
        refreshInputs();
        fire(onClear.get());
        fire(onChange.get());
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.DATE_PICKER;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setSpacing(4);
        startInput.getStyleClass().add("ele-date-picker__input");
        endInput.getStyleClass().add("ele-date-picker__input");
        separator.getStyleClass().add("ele-date-picker__separator");
        clearButton.getStyleClass().add("ele-date-picker__clear");
        trigger.getStyleClass().add("ele-date-picker__trigger");
        trigger.setGraphic(new EleFXIcon(EleFXIconType.ARROW_DOWN, 14));
        trigger.setOnAction(event -> {
            if (popup.isShowing())
                hide();
            else
                show();
        });
        clearButton.setOnAction(event -> clear());
        HBox.setHgrow(startInput, Priority.ALWAYS);
        HBox.setHgrow(endInput, Priority.ALWAYS);
        getChildren().addAll(startInput, separator, endInput, clearButton, trigger);

        StackPane popupRoot = new StackPane(panel);
        popupRoot.getStyleClass().add("ele-date-picker__popup");
        popupRoot.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        popupRoot.getStylesheets().add(EleFXThemes.DATE_PICKER.toData());
        panel.setBordered(false);
        popup.getScene().setRoot(popupRoot);
        popup.setAutoHide(true);
        popup.showingProperty().addListener((obs, oldValue, showing) -> {
            if (popupVisible == showing) return;
            popupVisible = showing;
            fire(onVisibleChange.get());
            if (!showing) refreshInputs();
        });

        startInput.setOnAction(event -> parseInputs());
        endInput.setOnAction(event -> parseInputs());
        startInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) hide();
        });
        endInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) hide();
        });
        startInput.focusedProperty().addListener((obs, oldValue, focused) -> inputFocusChanged(focused));
        endInput.focusedProperty().addListener((obs, oldValue, focused) -> inputFocusChanged(focused));

        type.addListener((obs, oldValue, current) -> configurePanel());
        defaultValue.addListener((obs, oldValue, current) -> panel.setDefaultValue(current));
        locale.addListener((obs, oldValue, current) -> panel.setLocale(current));
        disabledDate.addListener((obs, oldValue, current) -> panel.setDisabledDate(current));
        cellClassName.addListener((obs, oldValue, current) -> panel.setCellClassName(current));
        clearable.addListener((obs, oldValue, current) -> refreshInputs());
        editable.addListener((obs, oldValue, current) -> refreshInputs());
        readonly.addListener((obs, oldValue, current) -> refreshInputs());
        placeholder.addListener((obs, oldValue, current) -> refreshInputs());
        startPlaceholder.addListener((obs, oldValue, current) -> refreshInputs());
        endPlaceholder.addListener((obs, oldValue, current) -> refreshInputs());
        rangeSeparator.addListener((obs, oldValue, current) -> refreshInputs());
        format.addListener((obs, oldValue, current) -> refreshInputs());
        showFooter.addListener((obs, oldValue, current) -> panel.setShowFooter(current));
        showConfirm.addListener((obs, oldValue, current) -> panel.setShowConfirm(current));
        showWeekNumber.addListener((obs, oldValue, current) -> panel.setShowWeekNumber(current));
        singlePanel.addListener((obs, oldValue, current) -> panel.setSinglePanel(current));
        disableProperty().addListener((obs, oldValue, current) -> refreshInputs());

        values.addListener((ListChangeListener<LocalDate>) change -> syncPanelValues());
        value.addListener((obs, oldValue, current) -> syncPanelValue(current));
        dateTimeValue.addListener((obs, oldValue, current) -> {
            if (!synchronizing && current != null) panel.setDateTimeValue(current);
            refreshInputs();
        });
        shortcuts.addListener((ListChangeListener<EleFXDatePickerShortcut>) change -> syncShortcuts());
        panel.getValues().addListener((ListChangeListener<LocalDate>) change -> syncFromPanel());
        panel.dateTimeValueProperty().addListener((obs, oldValue, current) -> {
            if (!synchronizing && current != null) dateTimeValue.set(current);
        });
        panel.setOnChange(event -> {
            syncFromPanel();
            fire(onChange.get());
        });
        panel.setOnClear(event -> {
            syncFromPanel();
            fire(onClear.get());
        });
        panel.setOnCalendarChange(event -> fire(onCalendarChange.get()));
        panel.setOnPanelChange(event -> fire(onPanelChange.get()));

        configurePanel();
        sceneBuilderIntegration();
    }

    private void configurePanel() {
        if (getType() == null) return;
        synchronizing = true;
        try {
            panel.setType(getType().toPanelType());
            panel.setDefaultValue(getDefaultValue());
            panel.setLocale(getLocale());
            panel.setDisabledDate(getDisabledDate());
            panel.setCellClassName(getCellClassName());
            panel.setClearable(isClearable());
            panel.setShowFooter(isShowFooter());
            panel.setShowConfirm(isShowConfirm());
            panel.setShowWeekNumber(isShowWeekNumber());
            panel.setSinglePanel(isSinglePanel());
            panel.getValues().setAll(values);
        } finally {
            synchronizing = false;
        }
        refreshInputs();
    }

    private void syncPanelValues() {
        if (synchronizing) return;
        synchronizing = true;
        try {
            panel.getValues().setAll(values);
            value.set(values.isEmpty() ? null : values.get(0));
        } finally {
            synchronizing = false;
        }
        refreshInputs();
    }

    private void syncPanelValue(LocalDate selected) {
        if (synchronizing) {
            refreshInputs();
            return;
        }
        synchronizing = true;
        try {
            panel.setValue(selected);
            values.setAll(selected == null ? List.of() : List.of(selected));
        } finally {
            synchronizing = false;
        }
        refreshInputs();
    }

    private void syncFromPanel() {
        if (synchronizing) return;
        synchronizing = true;
        try {
            values.setAll(panel.getValues());
            value.set(values.isEmpty() ? null : values.get(0));
            if (panel.getDateTimeValue() != null) dateTimeValue.set(panel.getDateTimeValue());
        } finally {
            synchronizing = false;
        }
        refreshInputs();
        if (!getType().isRange() && !getType().isMultiple() && getType() != EleFXDatePickerType.DATETIME) hide();
    }

    private void syncShortcuts() {
        panel.getShortcuts()
                .setAll(shortcuts.stream()
                        .map(shortcut -> new com.sjydvlp.elefx.component.datepickerpanel.EleFXDatePickerShortcut(
                                shortcut.getText(),
                                shortcut::getValues))
                        .collect(Collectors.toList()));
    }

    private void inputFocusChanged(boolean focused) {
        if (focused) {
            fire(onFocus.get());
            if (isAutomaticDropdown()) show();
        } else {
            parseInputs();
            fire(onBlur.get());
        }
    }

    private void parseInputs() {
        if (!isEditable() || isReadonly() || isDisable()) return;
        try {
            if (getType() == EleFXDatePickerType.DATETIME) {
                setDateTimeValue(LocalDateTime.parse(startInput.getText().trim(), formatter()));
                return;
            }
            if (getType().isRange()) {
                List<LocalDate> parsed = new ArrayList<>();
                if (!startInput.getText().isBlank()) parsed.add(parseDate(startInput.getText()));
                if (!endInput.getText().isBlank()) parsed.add(parseDate(endInput.getText()));
                values.setAll(parsed);
            } else if (getType().isMultiple()) {
                List<LocalDate> parsed = new ArrayList<>();
                for (String part : startInput.getText().split(","))
                    if (!part.isBlank()) parsed.add(parseDate(part));
                values.setAll(parsed);
            } else if (startInput.getText().isBlank()) {
                clear();
            } else {
                setValue(parseDate(startInput.getText()));
            }
        } catch (DateTimeParseException | IllegalArgumentException ignored) {
            refreshInputs();
        }
    }

    private LocalDate parseDate(String text) {
        String candidate = text.trim();
        return switch (getType()) {
            case YEAR, YEARS, YEAR_RANGE -> Year.parse(candidate, formatter()).atDay(1);
            case MONTH, MONTHS, MONTH_RANGE -> YearMonth.parse(candidate, formatter()).atDay(1);
            case QUARTER, QUARTERS, QUARTER_RANGE -> parseQuarter(candidate);
            default -> LocalDate.parse(candidate, formatter());
        };
    }

    private LocalDate parseQuarter(String text) {
        String normalized = text.trim().replace(" ", "");
        int marker = Math.max(normalized.lastIndexOf('Q'), normalized.lastIndexOf('q'));
        if (marker < 1 || marker == normalized.length() - 1)
            throw new DateTimeParseException("Invalid quarter", text, 0);
        int year = Integer.parseInt(normalized.substring(0, marker).replaceAll("[^0-9-]", ""));
        int quarter = Integer.parseInt(normalized.substring(marker + 1));
        if (quarter < 1 || quarter > 4) throw new DateTimeParseException("Invalid quarter", text, marker + 1);
        return LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
    }

    private void refreshInputs() {
        boolean range = getType() != null && getType().isRange();
        boolean inputEditable = isEditable() && !isReadonly() && !isDisable();
        startInput.setEditable(inputEditable);
        endInput.setEditable(inputEditable);
        startInput.setDisable(isDisable());
        endInput.setDisable(isDisable());
        startInput.setPromptText(range ? getStartPlaceholder() : getPlaceholder());
        endInput.setPromptText(getEndPlaceholder());
        separator.setText(getRangeSeparator());
        separator.setVisible(range);
        separator.setManaged(range);
        endInput.setVisible(range);
        endInput.setManaged(range);
        List<LocalDate> selected = List.copyOf(values);
        if (getType() == EleFXDatePickerType.DATETIME && getDateTimeValue() != null)
            startInput.setText(formatter().format(getDateTimeValue()));
        else if (getType().isMultiple())
            startInput.setText(selected.stream().map(this::formatDate).collect(Collectors.joining(", ")));
        else
            startInput.setText(selected.isEmpty() ? "" : formatDate(selected.get(0)));
        if (range) endInput.setText(selected.size() < 2 ? "" : formatDate(selected.get(1)));
        boolean hasValue = !selected.isEmpty() || getDateTimeValue() != null;
        clearButton.setVisible(isClearable() && hasValue && !isDisable());
        clearButton.setManaged(clearButton.isVisible());
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        if (getType().usesQuarter()) return date.getYear() + " Q" + ((date.getMonthValue() - 1) / 3 + 1);
        return formatter().format(date);
    }

    private DateTimeFormatter formatter() {
        String configured = getFormat();
        if (configured == null || configured.isBlank()) configured = defaultFormat();
        return DateTimeFormatter.ofPattern(normalizePattern(configured), getLocale());
    }

    private String defaultFormat() {
        return switch (getType()) {
            case YEAR, YEARS, YEAR_RANGE -> "yyyy";
            case MONTH, MONTHS, MONTH_RANGE -> "yyyy-MM";
            case WEEK -> "YYYY-'W'ww";
            case QUARTER, QUARTERS, QUARTER_RANGE -> "yyyy 'Q'Q";
            case DATETIME, DATETIME_RANGE -> "yyyy-MM-dd HH:mm:ss";
            default -> "yyyy-MM-dd";
        };
    }

    private static String normalizePattern(String pattern) {
        return pattern.replace("YYYY", "yyyy").replace("YY", "yy").replace("DD", "dd");
    }

    private void fire(EventHandler<ActionEvent> handler) {
        if (handler == null) return;
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        handler.handle(event);
    }
}
