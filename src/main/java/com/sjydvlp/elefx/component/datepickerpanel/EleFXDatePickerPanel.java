package com.sjydvlp.elefx.component.datepickerpanel;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/** Standalone Element Plus inspired date-selection panel with single, multiple and range modes. */
public class EleFXDatePickerPanel extends VBox implements Themable {

    private static final String STYLE_CLASS = "ele-date-picker-panel";

    private static final double DAY_COLUMN_WIDTH = 38;

    private static final double PERIOD_COLUMN_WIDTH = 68;

    private static final double CONTENT_WIDTH = 278;

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(this, "dateTimeValue");

    private final ObservableList<LocalDate> values = FXCollections.observableArrayList();

    private final ObjectProperty<EleFXDatePickerPanelType> type = new SimpleObjectProperty<>(this, "type",
            EleFXDatePickerPanelType.DATE);

    private final ObjectProperty<YearMonth> displayedMonth = new SimpleObjectProperty<>(this, "displayedMonth",
            YearMonth.now());

    private final ObjectProperty<LocalDate> defaultValue = new SimpleObjectProperty<>(this, "defaultValue");

    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.ENGLISH);

    private final ObjectProperty<Predicate<LocalDate>> disabledDate = new SimpleObjectProperty<>(this, "disabledDate");

    private final ObjectProperty<Function<LocalDate, String>> cellClassName = new SimpleObjectProperty<>(this,
            "cellClassName");

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", true);

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", true);

    private final BooleanProperty showFooter = new SimpleBooleanProperty(this, "showFooter", false);

    private final BooleanProperty showConfirm = new SimpleBooleanProperty(this, "showConfirm", false);

    private final BooleanProperty showWeekNumber = new SimpleBooleanProperty(this, "showWeekNumber", false);

    private final BooleanProperty singlePanel = new SimpleBooleanProperty(this, "singlePanel", false);

    private final ObservableList<EleFXDatePickerShortcut> shortcuts = FXCollections.observableArrayList();

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onCalendarChange = new SimpleObjectProperty<>(this,
            "onCalendarChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onPanelChange = new SimpleObjectProperty<>(this,
            "onPanelChange");

    private final ObjectProperty<EventHandler<ActionEvent>> onClear = new SimpleObjectProperty<>(this, "onClear");

    private final ObjectProperty<EventHandler<ActionEvent>> onConfirm = new SimpleObjectProperty<>(this, "onConfirm");

    private final Label title = new Label();

    private final GridPane grid = new GridPane();

    private final HBox header = new HBox(8);

    private final HBox quarterRange = new HBox(16);

    private final HBox timeBar = new HBox(4);

    private final TextField dateInput = new TextField();

    private final TextField timeInput = new TextField();

    private final TextField rangeEndDateInput = new TextField();

    private final TextField rangeEndTimeInput = new TextField();

    private final PopupControl timePopup = new PopupControl();

    private final VBox timePopupContent = new VBox(8);

    private final HBox timePickerColumns = new HBox(4);

    private final HBox timePickerFooter = new HBox(8);

    private final ListView<String> hourSpinner = timePicker(24);

    private final ListView<String> minuteSpinner = timePicker(60);

    private final ListView<String> secondSpinner = timePicker(60);

    private Button previous;

    private Button next;

    private Button previousYear;

    private Button nextYear;

    private final HBox shortcutBar = new HBox(6);

    private final HBox footer = new HBox(8);

    private boolean synchronizingValues;

    private boolean synchronizingTime;

    private LocalTime pendingTime;

    private LocalTime rangeStartTime = LocalTime.MIDNIGHT;

    private LocalTime rangeEndTime = LocalTime.MIDNIGHT;

    private String pendingTimeText;

    private TextField activeTimeInput;

    private LocalDate hoveredWeekStart;

    private final Map<LocalDate, Button> weekCells = new HashMap<>();

    public EleFXDatePickerPanel() {
        initialize();
    }

    public EleFXDatePickerPanel(LocalDate value) {
        this();
        setValue(value);
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

    public EleFXDatePickerPanelType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXDatePickerPanelType> typeProperty() {
        return type;
    }

    public void setType(EleFXDatePickerPanelType value) {
        type.set(value == null ? EleFXDatePickerPanelType.DATE : value);
    }

    public YearMonth getDisplayedMonth() {
        return displayedMonth.get();
    }

    public ObjectProperty<YearMonth> displayedMonthProperty() {
        return displayedMonth;
    }

    public void setDisplayedMonth(YearMonth value) {
        displayedMonth.set(value == null ? YearMonth.now() : value);
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

    public boolean isBordered() {
        return bordered.get();
    }

    public BooleanProperty borderedProperty() {
        return bordered;
    }

    public void setBordered(boolean value) {
        bordered.set(value);
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

    public ObservableList<EleFXDatePickerShortcut> getShortcuts() {
        return shortcuts;
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

    public ObjectProperty<EventHandler<ActionEvent>> onClearProperty() {
        return onClear;
    }

    public void setOnClear(EventHandler<ActionEvent> value) {
        onClear.set(value);
    }

    public EventHandler<ActionEvent> getOnConfirm() {
        return onConfirm.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onConfirmProperty() {
        return onConfirm;
    }

    public void setOnConfirm(EventHandler<ActionEvent> value) {
        onConfirm.set(value);
    }

    public LocalTime getRangeStartTime() {
        return rangeStartTime;
    }

    public LocalTime getRangeEndTime() {
        return rangeEndTime;
    }

    public void setRangeTimes(LocalTime start, LocalTime end) {
        rangeStartTime = start == null ? LocalTime.MIDNIGHT : start;
        rangeEndTime = end == null ? LocalTime.MIDNIGHT : end;
        if (getType() == EleFXDatePickerPanelType.DATETIME_RANGE) {
            timeInput.setText(TIME_INPUT_FORMATTER.format(rangeStartTime));
            rangeEndTimeInput.setText(TIME_INPUT_FORMATTER.format(rangeEndTime));
        }
    }

    public void clear() {
        if (values.isEmpty() && getValue() == null) return;
        setSelectedValues(List.of());
        fire(onClear.get());
        fireChange();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        // The public default stylesheet contains the date-picker rules and is
        // available in every EleFX distribution.
        return EleFXThemes.DEFAULT;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setPadding(new Insets(12));
        setSpacing(8);
        setPrefWidth(322);
        setMinWidth(Region.USE_PREF_SIZE);
        setMaxWidth(Region.USE_PREF_SIZE);
        setAlignment(Pos.TOP_CENTER);
        previousYear = navMonths(EleFXIconType.D_ARROW_LEFT, -12);
        previous = nav(EleFXIconType.ARROW_LEFT, -1);
        next = nav(EleFXIconType.ARROW_RIGHT, 1);
        nextYear = navMonths(EleFXIconType.D_ARROW_RIGHT, 12);
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().addAll(previousYear, previous, title, next, nextYear);
        header.getStyleClass().add("ele-date-picker-panel__header");
        header.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("ele-date-picker-panel__table");
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);
        shortcutBar.getStyleClass().add("ele-date-picker-panel__shortcuts");
        shortcutBar.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("ele-date-picker-panel__footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        quarterRange.getStyleClass().add("ele-date-picker-panel__range");
        quarterRange.setAlignment(Pos.TOP_CENTER);
        timeBar.getStyleClass().add("ele-date-picker-panel__time");
        timeBar.setAlignment(Pos.CENTER);
        dateInput.getStyleClass().add("ele-date-picker-panel__date-input");
        dateInput.setEditable(false);
        dateInput.setFocusTraversable(false);
        dateInput.setMouseTransparent(true);
        dateInput.setPromptText("Select date");
        rangeEndDateInput.getStyleClass().add("ele-date-picker-panel__date-input");
        rangeEndDateInput.setEditable(false);
        rangeEndDateInput.setFocusTraversable(false);
        rangeEndDateInput.setMouseTransparent(true);
        rangeEndDateInput.setPromptText("Select date");
        timeInput.getStyleClass().add("ele-date-picker-panel__time-input");
        timeInput.setEditable(false);
        timeInput.setFocusTraversable(false);
        timeInput.setPromptText("Select time");
        timeInput.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            dateInput.getStyleClass().remove("ele-date-picker-panel__date-input--focused");
            showTimePicker(timeInput);
            event.consume();
        });
        rangeEndTimeInput.getStyleClass().add("ele-date-picker-panel__time-input");
        rangeEndTimeInput.setEditable(false);
        rangeEndTimeInput.setFocusTraversable(false);
        rangeEndTimeInput.setPromptText("Select time");
        rangeEndTimeInput.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            rangeEndDateInput.getStyleClass().remove("ele-date-picker-panel__date-input--focused");
            showTimePicker(rangeEndTimeInput);
            event.consume();
        });
        timeBar.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (dateInput.getBoundsInParent().contains(event.getX(), event.getY())
                    && !dateInput.getStyleClass().contains("ele-date-picker-panel__date-input--focused"))
                dateInput.getStyleClass().add("ele-date-picker-panel__date-input--focused");
        });
        timeBar.getChildren().addAll(dateInput, timeInput, rangeEndDateInput, rangeEndTimeInput);
        timePopupContent.getStyleClass().add("ele-date-picker-panel__time-popup");
        timePickerColumns.getStyleClass().add("ele-date-picker-panel__time-columns");
        timePickerColumns.getChildren().addAll(hourSpinner, minuteSpinner, secondSpinner);
        timePickerFooter.getStyleClass().add("ele-date-picker-panel__time-footer");
        timePickerFooter.setAlignment(Pos.CENTER_RIGHT);
        Button cancelTime = new Button("Cancel");
        cancelTime.getStyleClass().add("ele-date-picker-panel__time-action");
        cancelTime.setOnAction(event -> cancelTimeSelection());
        Button confirmTime = new Button("OK");
        confirmTime.getStyleClass().addAll("ele-date-picker-panel__time-action",
                "ele-date-picker-panel__time-action--primary");
        confirmTime.setOnAction(event -> confirmTimeSelection());
        timePickerFooter.getChildren().addAll(cancelTime, confirmTime);
        timePopupContent.getChildren().addAll(timePickerColumns, timePickerFooter);
        timePopupContent.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        timePopup.getScene().setRoot(timePopupContent);
        timePopup.setAutoHide(true);
        timePopup.setOnAutoHide(event -> restorePendingTime());
        getChildren().addAll(timeBar, header, grid, quarterRange, shortcutBar, footer);
        value.addListener((o, old, selected) -> {
            updateDateInput();
            if (!synchronizingValues) {
                if (selected != null) setDisplayedMonth(YearMonth.from(selected));
                setSelectedValues(selected == null ? List.of() : List.of(selected));
            }
        });
        dateTimeValue.addListener((o, old, current) -> {
            if (current == null) return;
            if (!current.toLocalDate().equals(getValue())) setValue(current.toLocalDate());
            setTime(current.toLocalTime());
        });
        hourSpinner.getSelectionModel().selectedItemProperty().addListener((o, old, current) -> timeSelectionChanged());
        minuteSpinner.getSelectionModel().selectedItemProperty()
                .addListener((o, old, current) -> timeSelectionChanged());
        secondSpinner.getSelectionModel().selectedItemProperty()
                .addListener((o, old, current) -> timeSelectionChanged());
        values.addListener((ListChangeListener<LocalDate>) o -> {
            if (!synchronizingValues) syncValueAndRepaint();
        });
        type.addListener((o, old, current) -> {
            updateNavigationIcons();
            setSelectedValues(List.of());
        });
        displayedMonth.addListener(o -> repaint());
        locale.addListener(o -> repaint());
        defaultValue.addListener((o, old, current) -> {
            if (values.isEmpty() && current != null)
                setDisplayedMonth(YearMonth.from(current));
            else
                repaint();
        });
        disabledDate.addListener(o -> repaint());
        cellClassName.addListener(o -> repaint());
        disableProperty().addListener(o -> repaint());
        bordered.addListener(o -> updateBorder());
        showFooter.addListener(o -> updateFooter());
        showConfirm.addListener(o -> updateFooter());
        showWeekNumber.addListener(o -> repaint());
        singlePanel.addListener(o -> repaint());
        shortcuts.addListener((ListChangeListener<EleFXDatePickerShortcut>) o -> updateShortcuts());
        updateBorder();
        updateNavigationIcons();
        updateShortcuts();
        updateFooter();
        updateDateInput();
        if (getDefaultValue() != null) setDisplayedMonth(YearMonth.from(getDefaultValue()));
        repaint();
        sceneBuilderIntegration();
    }

    private Button nav(EleFXIconType icon, int direction) {
        return nav(icon, () -> shiftPanel(direction));
    }

    private Button navMonths(EleFXIconType icon, int months) {
        return nav(icon, () -> setDisplayedMonth(getDisplayedMonth().plusMonths(months)));
    }

    private Button nav(EleFXIconType icon, Runnable navigation) {
        Button button = new Button();
        button.getStyleClass().add("ele-date-picker-panel__nav");
        button.setGraphic(new EleFXIcon(icon, 14));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setOnAction(e -> {
            navigation.run();
            fire(onPanelChange.get());
        });
        return button;
    }

    private ListView<String> timePicker(int count) {
        ListView<String> picker = new ListView<>();
        for (int value = 0; value < count; value++)
            picker.getItems().add(String.format("%02d", value));
        picker.getStyleClass().add("ele-date-picker-panel__time-picker");
        picker.setPrefWidth(76);
        picker.setPrefHeight(192);
        picker.setFocusTraversable(false);
        picker.getSelectionModel().selectFirst();
        return picker;
    }

    private void showTimePicker(TextField input) {
        activeTimeInput = input;
        if (getType() == EleFXDatePickerPanelType.DATETIME_RANGE)
            setTime(input == rangeEndTimeInput ? rangeEndTime : rangeStartTime);
        pendingTime = selectedTime();
        pendingTimeText = input.getText();
        javafx.geometry.Bounds bounds = input.localToScreen(input.getBoundsInLocal());
        if (bounds != null) timePopup.show(input, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    private void setTime(LocalTime time) {
        synchronizingTime = true;
        try {
            selectTime(hourSpinner, time.getHour());
            selectTime(minuteSpinner, time.getMinute());
            selectTime(secondSpinner, time.getSecond());
        } finally {
            synchronizingTime = false;
        }
        updateTimeInput();
    }

    private void selectTime(ListView<String> picker, int value) {
        picker.getSelectionModel().select(value);
        picker.scrollTo(value);
    }

    private void timeSelectionChanged() {
        updateTimeInput();
    }

    private LocalTime selectedTime() {
        String hour = hourSpinner.getSelectionModel().getSelectedItem();
        String minute = minuteSpinner.getSelectionModel().getSelectedItem();
        String second = secondSpinner.getSelectionModel().getSelectedItem();
        return hour == null || minute == null || second == null
                ? LocalTime.MIDNIGHT
                : LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second));
    }

    private void cancelTimeSelection() {
        restorePendingTime();
        timePopup.hide();
    }

    private void restorePendingTime() {
        if (pendingTime == null) return;
        setTime(pendingTime);
        (activeTimeInput == null ? timeInput : activeTimeInput).setText(pendingTimeText == null ? "" : pendingTimeText);
    }

    private void confirmTimeSelection() {
        boolean changed = updateDateTime();
        if (getType() == EleFXDatePickerPanelType.DATETIME_RANGE) {
            if (activeTimeInput == rangeEndTimeInput)
                rangeEndTime = selectedTime();
            else
                rangeStartTime = selectedTime();
            changed = true;
        }
        if (changed) fireChange();
        pendingTime = selectedTime();
        timePopup.hide();
    }

    private void updateTimeInput() {
        String hour = hourSpinner.getSelectionModel().getSelectedItem();
        String minute = minuteSpinner.getSelectionModel().getSelectedItem();
        String second = secondSpinner.getSelectionModel().getSelectedItem();
        if (hour != null && minute != null && second != null)
            (activeTimeInput == null ? timeInput : activeTimeInput).setText(hour + ":" + minute + ":" + second);
    }

    private void updateDateInput() {
        LocalDate selected = getValue();
        dateInput.setText(selected == null ? "" : DATE_INPUT_FORMATTER.format(selected));
        boolean dateTimeRange = getType() == EleFXDatePickerPanelType.DATETIME_RANGE;
        if (dateTimeRange && selected != null) {
            rangeEndDateInput.setText(DATE_INPUT_FORMATTER.format(values.size() == 2 ? values.get(1) : selected));
            if (values.size() == 1) resetRangeTimes();
        } else {
            rangeEndDateInput.clear();
            if (selected == null) timeInput.clear();
            rangeEndTimeInput.clear();
        }
    }

    private void resetRangeTimes() {
        rangeStartTime = LocalTime.MIDNIGHT;
        rangeEndTime = LocalTime.MIDNIGHT;
        synchronizingTime = true;
        try {
            selectTime(hourSpinner, 0);
            selectTime(minuteSpinner, 0);
            selectTime(secondSpinner, 0);
        } finally {
            synchronizingTime = false;
        }
        activeTimeInput = null;
        timeInput.setText("00:00:00");
        rangeEndTimeInput.setText("00:00:00");
    }

    private boolean updateDateTime() {
        if (getType() != EleFXDatePickerPanelType.DATETIME || getValue() == null) return false;
        LocalDateTime next = LocalDateTime.of(getValue(), selectedTime());
        if (next.equals(getDateTimeValue())) return false;
        dateTimeValue.set(next);
        return true;
    }

    private void updateNavigationIcons() {
        boolean annualView = getType().usesYearGrid() || getType().usesMonthGrid() || getType().usesQuarterGrid();
        previous.setGraphic(new EleFXIcon(annualView ? EleFXIconType.D_ARROW_LEFT : EleFXIconType.ARROW_LEFT, 14));
        next.setGraphic(new EleFXIcon(annualView ? EleFXIconType.D_ARROW_RIGHT : EleFXIconType.ARROW_RIGHT, 14));
        previousYear.setVisible(!annualView);
        previousYear.setManaged(!annualView);
        nextYear.setVisible(!annualView);
        nextYear.setManaged(!annualView);
    }

    private void shiftPanel(int direction) {
        EleFXDatePickerPanelType current = getType();
        int months = current.usesYearGrid()
                ? direction * 120
                : (current.usesMonthGrid() || current.usesQuarterGrid()) ? direction * 12 : direction;
        setDisplayedMonth(getDisplayedMonth().plusMonths(months));
    }

    private void repaint() {
        grid.getChildren().clear();
        weekCells.clear();
        boolean dualPanelView = (getType() == EleFXDatePickerPanelType.QUARTER_RANGE
                || getType() == EleFXDatePickerPanelType.DATE_RANGE
                || getType() == EleFXDatePickerPanelType.DATETIME_RANGE
                || getType() == EleFXDatePickerPanelType.MONTH_RANGE
                || getType() == EleFXDatePickerPanelType.YEAR_RANGE) && !isSinglePanel();
        header.setVisible(!dualPanelView);
        header.setManaged(!dualPanelView);
        grid.setVisible(!dualPanelView);
        grid.setManaged(!dualPanelView);
        quarterRange.setVisible(dualPanelView);
        quarterRange.setManaged(dualPanelView);
        boolean dateTimeView = getType() == EleFXDatePickerPanelType.DATETIME
                || getType() == EleFXDatePickerPanelType.DATETIME_RANGE;
        boolean dateTimeRangeView = getType() == EleFXDatePickerPanelType.DATETIME_RANGE;
        timeBar.setVisible(dateTimeView);
        timeBar.setManaged(dateTimeView);
        boolean compactDateTimeRange = dateTimeRangeView && isSinglePanel();
        if (compactDateTimeRange) {
            if (!timeBar.getStyleClass().contains("ele-date-picker-panel__time--compact-range"))
                timeBar.getStyleClass().add("ele-date-picker-panel__time--compact-range");
        } else
            timeBar.getStyleClass().remove("ele-date-picker-panel__time--compact-range");
        rangeEndDateInput.setVisible(dateTimeRangeView);
        rangeEndDateInput.setManaged(dateTimeRangeView);
        rangeEndTimeInput.setVisible(dateTimeRangeView);
        rangeEndTimeInput.setManaged(dateTimeRangeView);
        setPrefWidth(dualPanelView ? 596 : 322);
        if (dualPanelView) {
            if (getType() == EleFXDatePickerPanelType.DATE_RANGE
                    || getType() == EleFXDatePickerPanelType.DATETIME_RANGE)
                paintDateTimeRange();
            else if (getType() == EleFXDatePickerPanelType.MONTH_RANGE)
                paintMonthRange();
            else if (getType() == EleFXDatePickerPanelType.YEAR_RANGE)
                paintYearRange();
            else
                paintQuarterRange();
            return;
        }
        quarterRange.getChildren().clear();
        if (getType().usesMonthGrid())
            paintMonths();
        else if (getType().usesYearGrid())
            paintYears();
        else if (getType().usesQuarterGrid())
            paintQuarters();
        else
            paintDates();
    }

    private void prepareHeader(String text, double width) {
        title.setText(text);
        header.setPrefWidth(width);
        header.setMaxWidth(width);
    }

    private void paintDates() {
        configureGrid(isShowWeekNumber() ? 8 : 7, DAY_COLUMN_WIDTH, 0, 0);
        prepareHeader(
                getDisplayedMonth().getYear() + " "
                        + getDisplayedMonth().getMonth().getDisplayName(TextStyle.FULL, getLocale()),
                isShowWeekNumber()
                        ? CONTENT_WIDTH + DAY_COLUMN_WIDTH + 2
                        : CONTENT_WIDTH);
        int offset = isShowWeekNumber() ? 1 : 0;
        if (isShowWeekNumber()) addWeekLabel("Wk", 0, 0);
        for (int column = 0; column < 7; column++) {
            Label label = new Label(
                    DayOfWeek.of(column == 0 ? 7 : column).getDisplayName(TextStyle.SHORT, getLocale()));
            label.getStyleClass().add("ele-date-picker-panel__weekday");
            label.setMinWidth(DAY_COLUMN_WIDTH);
            label.setAlignment(Pos.CENTER);
            grid.add(label, column + offset, 0);
        }
        LocalDate first = getDisplayedMonth().atDay(1), cursor = first.minusDays(first.getDayOfWeek().getValue() % 7);
        for (int index = 0; index < 42; index++, cursor = cursor.plusDays(1)) {
            LocalDate date = cursor;
            if (isShowWeekNumber() && index % 7 == 0)
                addWeekLabel(String.valueOf(date.get(WeekFields.of(getLocale()).weekOfWeekBasedYear())), 0,
                        index / 7 + 1);
            Button cell = dateCell(String.valueOf(date.getDayOfMonth()), date);
            if (!YearMonth.from(date).equals(getDisplayedMonth()))
                cell.getStyleClass().add("ele-date-picker-panel__cell--other-month");
            if (date.equals(LocalDate.now())) cell.getStyleClass().add("ele-date-picker-panel__cell--today");
            grid.add(cell, index % 7 + offset, index / 7 + 1);
        }
    }

    private void paintMonths() {
        configureGrid(4, 68, 3, 48);
        prepareHeader(String.valueOf(getDisplayedMonth().getYear()), CONTENT_WIDTH);
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(getDisplayedMonth().getYear(), month, 1);
            grid.add(periodCell(date.getMonth().getDisplayName(TextStyle.SHORT, getLocale()), date, 54),
                    (month - 1) % 4, (month - 1) / 4);
        }
    }

    private void paintQuarters() {
        configureGrid(4, 68, 1, 48);
        prepareHeader(String.valueOf(getDisplayedMonth().getYear()), CONTENT_WIDTH);
        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate date = LocalDate.of(getDisplayedMonth().getYear(), (quarter - 1) * 3 + 1, 1);
            grid.add(periodCell("Q" + quarter, date, 60), quarter - 1, 0);
        }
    }

    private void paintQuarterRange() {
        int leftYear = getDisplayedMonth().getYear();
        quarterRange.getChildren().setAll(createQuarterRangePanel(leftYear, true),
                createQuarterRangePanel(leftYear + 1, false));
    }

    private void paintMonthRange() {
        int leftYear = getDisplayedMonth().getYear();
        quarterRange.getChildren().setAll(createMonthRangePanel(leftYear, true),
                createMonthRangePanel(leftYear + 1, false));
    }

    private void paintYearRange() {
        int firstYear = Math.floorDiv(getDisplayedMonth().getYear(), 10) * 10;
        quarterRange.getChildren().setAll(createYearRangePanel(firstYear, true),
                createYearRangePanel(firstYear + 10, false));
    }

    private void paintDateTimeRange() {
        YearMonth leftMonth = getDisplayedMonth();
        quarterRange.getChildren().setAll(createDateRangePanel(leftMonth, true),
                createDateRangePanel(leftMonth.plusMonths(1), false));
    }

    private VBox createDateRangePanel(YearMonth month, boolean left) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("ele-date-picker-panel__range-panel");
        panel.setPrefWidth(CONTENT_WIDTH);

        Label rangeTitle = new Label(month.getYear() + " "
                + month.getMonth().getDisplayName(TextStyle.FULL, getLocale()));
        rangeTitle.setMaxWidth(Double.MAX_VALUE);
        rangeTitle.setAlignment(Pos.CENTER);
        HBox rangeHeader = new HBox(8);
        rangeHeader.getStyleClass().add("ele-date-picker-panel__header");
        rangeHeader.setAlignment(Pos.CENTER);
        if (left) rangeHeader.getChildren().addAll(navMonths(EleFXIconType.D_ARROW_LEFT, -12),
                nav(EleFXIconType.ARROW_LEFT, -1));
        rangeHeader.getChildren().add(rangeTitle);
        HBox.setHgrow(rangeTitle, Priority.ALWAYS);
        if (!left) rangeHeader.getChildren().addAll(nav(EleFXIconType.ARROW_RIGHT, 1),
                navMonths(EleFXIconType.D_ARROW_RIGHT, 12));

        GridPane rangeGrid = new GridPane();
        rangeGrid.setAlignment(Pos.CENTER);
        rangeGrid.setHgap(0);
        rangeGrid.setVgap(getType() == EleFXDatePickerPanelType.DATE_RANGE
                || getType() == EleFXDatePickerPanelType.DATETIME_RANGE ? 6 : 2);
        for (int column = 0; column < 7; column++) {
            Label label = new Label(DayOfWeek.of(column == 0 ? 7 : column)
                    .getDisplayName(TextStyle.SHORT, getLocale()));
            label.getStyleClass().add("ele-date-picker-panel__weekday");
            label.setMinWidth(DAY_COLUMN_WIDTH);
            label.setAlignment(Pos.CENTER);
            rangeGrid.add(label, column, 0);
        }
        LocalDate first = month.atDay(1);
        LocalDate cursor = first.minusDays(first.getDayOfWeek().getValue() % 7);
        for (int index = 0; index < 42; index++, cursor = cursor.plusDays(1)) {
            LocalDate date = cursor;
            Button cell = dateCell(String.valueOf(date.getDayOfMonth()), date);
            if (!YearMonth.from(date).equals(month)) {
                cell.getStyleClass().add("ele-date-picker-panel__cell--other-month");
                cell.getStyleClass().removeAll("ele-date-picker-panel__cell--selected",
                        "ele-date-picker-panel__cell--in-range", "ele-date-picker-panel__cell--range-start",
                        "ele-date-picker-panel__cell--range-end");
            }
            if (date.equals(LocalDate.now())) cell.getStyleClass().add("ele-date-picker-panel__cell--today");
            rangeGrid.add(cell, index % 7, index / 7 + 1);
        }
        panel.getChildren().addAll(rangeHeader, rangeGrid);
        return panel;
    }

    private VBox createQuarterRangePanel(int year, boolean left) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("ele-date-picker-panel__range-panel");
        panel.setPrefWidth(CONTENT_WIDTH);
        Label rangeTitle = new Label(String.valueOf(year));
        rangeTitle.setMaxWidth(Double.MAX_VALUE);
        rangeTitle.setAlignment(Pos.CENTER);
        HBox rangeHeader = new HBox(8);
        rangeHeader.getStyleClass().add("ele-date-picker-panel__header");
        rangeHeader.setAlignment(Pos.CENTER);
        if (left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_LEFT, -1));
        rangeHeader.getChildren().add(rangeTitle);
        HBox.setHgrow(rangeTitle, Priority.ALWAYS);
        if (!left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_RIGHT, 1));
        GridPane rangeGrid = new GridPane();
        rangeGrid.setAlignment(Pos.CENTER);
        for (int column = 0; column < 4; column++)
            rangeGrid.getColumnConstraints().add(new ColumnConstraints(68));
        rangeGrid.getRowConstraints().add(new RowConstraints(48));
        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate date = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
            rangeGrid.add(periodCell("Q" + quarter, date, 68), quarter - 1, 0);
        }
        panel.getChildren().addAll(rangeHeader, rangeGrid);
        return panel;
    }

    private VBox createMonthRangePanel(int year, boolean left) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("ele-date-picker-panel__range-panel");
        panel.setPrefWidth(CONTENT_WIDTH);
        Label rangeTitle = new Label(String.valueOf(year));
        rangeTitle.setMaxWidth(Double.MAX_VALUE);
        rangeTitle.setAlignment(Pos.CENTER);
        HBox rangeHeader = new HBox(8);
        rangeHeader.getStyleClass().add("ele-date-picker-panel__header");
        rangeHeader.setAlignment(Pos.CENTER);
        if (left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_LEFT, -1));
        rangeHeader.getChildren().add(rangeTitle);
        HBox.setHgrow(rangeTitle, Priority.ALWAYS);
        if (!left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_RIGHT, 1));
        GridPane rangeGrid = periodRangeGrid(3);
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(year, month, 1);
            rangeGrid.add(periodCell(date.getMonth().getDisplayName(TextStyle.SHORT, getLocale()), date, 68),
                    (month - 1) % 4, (month - 1) / 4);
        }
        panel.getChildren().addAll(rangeHeader, rangeGrid);
        return panel;
    }

    private VBox createYearRangePanel(int firstYear, boolean left) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("ele-date-picker-panel__range-panel");
        panel.setPrefWidth(CONTENT_WIDTH);
        Label rangeTitle = new Label(firstYear + " - " + (firstYear + 9));
        rangeTitle.setMaxWidth(Double.MAX_VALUE);
        rangeTitle.setAlignment(Pos.CENTER);
        HBox rangeHeader = new HBox(8);
        rangeHeader.getStyleClass().add("ele-date-picker-panel__header");
        rangeHeader.setAlignment(Pos.CENTER);
        if (left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_LEFT, -1));
        rangeHeader.getChildren().add(rangeTitle);
        HBox.setHgrow(rangeTitle, Priority.ALWAYS);
        if (!left) rangeHeader.getChildren().add(nav(EleFXIconType.D_ARROW_RIGHT, 1));
        GridPane rangeGrid = periodRangeGrid(3);
        for (int index = 0; index < 10; index++) {
            LocalDate date = LocalDate.of(firstYear + index, 1, 1);
            rangeGrid.add(periodCell(String.valueOf(date.getYear()), date, 68), index % 4, index / 4);
        }
        panel.getChildren().addAll(rangeHeader, rangeGrid);
        return panel;
    }

    private GridPane periodRangeGrid(int rows) {
        GridPane rangeGrid = new GridPane();
        rangeGrid.setAlignment(Pos.CENTER);
        rangeGrid.setVgap(getType() == EleFXDatePickerPanelType.MONTH_RANGE
                || getType() == EleFXDatePickerPanelType.YEAR_RANGE ? 6 : 0);
        for (int column = 0; column < 4; column++)
            rangeGrid.getColumnConstraints().add(new ColumnConstraints(68));
        for (int row = 0; row < rows; row++)
            rangeGrid.getRowConstraints().add(new RowConstraints(48));
        return rangeGrid;
    }

    private void paintYears() {
        configureGrid(4, 68, 3, 48);
        int firstYear = Math.floorDiv(getDisplayedMonth().getYear(), 10) * 10;
        prepareHeader(firstYear + " - " + (firstYear + 9), CONTENT_WIDTH);
        for (int index = 0; index < 10; index++) {
            LocalDate date = LocalDate.of(firstYear + index, 1, 1);
            Button cell = periodCell(String.valueOf(date.getYear()), date, 60);
            grid.add(cell, index % 4, index / 4);
        }
    }

    private Button dateCell(String text, LocalDate date) {
        Button cell = createCell(text, date);
        double width = getType() == EleFXDatePickerPanelType.WEEK || getType().isRange() ? DAY_COLUMN_WIDTH : 24;
        boolean expandedRangeDate = getType() == EleFXDatePickerPanelType.DATE_RANGE
                || getType() == EleFXDatePickerPanelType.DATETIME_RANGE;
        double height = expandedRangeDate ? 30 : 24;
        if (expandedRangeDate)
            cell.getStyleClass().add("ele-date-picker-panel__cell--expanded-range-date");
        cell.setMinSize(width, height);
        cell.setPrefSize(width, height);
        return cell;
    }

    private Button periodCell(String text, LocalDate date, double width) {
        Button cell = createCell(text, date);
        cell.getStyleClass().add("ele-date-picker-panel__period-cell");
        if (getType().usesMonthGrid())
            cell.getStyleClass().add("ele-date-picker-panel__period-cell--month");
        else if (getType().usesYearGrid())
            cell.getStyleClass().add("ele-date-picker-panel__period-cell--year");
        else if (getType().usesQuarterGrid())
            cell.getStyleClass().add("ele-date-picker-panel__period-cell--quarter");
        if (getType().isRange())
            cell.getStyleClass().add("ele-date-picker-panel__period-cell--range");
        if (isCurrentPeriod(date)) cell.getStyleClass().add("ele-date-picker-panel__cell--today");
        // A range background must cover the whole grid column; otherwise the narrower
        // month/year button leaves a visible gap between adjacent periods.
        double cellWidth = getType().isRange() ? PERIOD_COLUMN_WIDTH : width;
        double cellHeight = getType().isRange() ? 48 : 36;
        cell.setMinSize(cellWidth, cellHeight);
        cell.setPrefSize(cellWidth, cellHeight);
        cell.setMaxSize(cellWidth, cellHeight);
        return cell;
    }

    private boolean isCurrentPeriod(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (getType().usesYearGrid()) return date.getYear() == today.getYear();
        if (getType().usesMonthGrid()) return date.getYear() == today.getYear()
                && date.getMonthValue() == today.getMonthValue();
        return getType().usesQuarterGrid()
                && date.getYear() == today.getYear()
                && (date.getMonthValue() - 1) / 3 == (today.getMonthValue() - 1) / 3;
    }

    private Button createCell(String text, LocalDate date) {
        Button cell = new Button(text);
        cell.getStyleClass().add("ele-date-picker-panel__cell");
        if (getType() == EleFXDatePickerPanelType.WEEK) cell.getStyleClass().add("ele-date-picker-panel__cell--week");
        cell.setFocusTraversable(false);
        GridPane.setHalignment(cell, HPos.CENTER);
        if (values.contains(date)) cell.getStyleClass().add("ele-date-picker-panel__cell--selected");
        if (isInSelectedRange(date) || isRangeEndpointPeriod(date))
            cell.getStyleClass().add("ele-date-picker-panel__cell--in-range");
        if ((getType().isRange() || getType() == EleFXDatePickerPanelType.WEEK) && values.size() == 2) {
            LocalDate first = values.get(0);
            LocalDate second = values.get(1);
            LocalDate start = first.isBefore(second) ? first : second;
            LocalDate end = first.isBefore(second) ? second : first;
            if (date.equals(start)) cell.getStyleClass().add("ele-date-picker-panel__cell--range-start");
            if (date.equals(end)) cell.getStyleClass().add("ele-date-picker-panel__cell--range-end");
        }
        if (getType() == EleFXDatePickerPanelType.WEEK && weekStart(date).equals(hoveredWeekStart)) {
            cell.getStyleClass().add("ele-date-picker-panel__cell--week-hover");
            if (date.equals(hoveredWeekStart))
                cell.getStyleClass().add("ele-date-picker-panel__cell--week-hover-start");
            else if (date.equals(hoveredWeekStart.plusDays(6)))
                cell.getStyleClass().add("ele-date-picker-panel__cell--week-hover-end");
        }
        String customClass = getCellClassName() == null ? null : getCellClassName().apply(date);
        if (customClass != null && !customClass.isBlank()) cell.getStyleClass().add(customClass);
        cell.setDisable(isDisable() || (getDisabledDate() != null && getDisabledDate().test(date)));
        cell.setOnAction(e -> {
            select(date);
            fire(onCalendarChange.get());
            fireChange();
        });
        if (getType() == EleFXDatePickerPanelType.WEEK) weekCells.put(date, cell);
        cell.setOnMouseEntered(e -> {
            if (getType() == EleFXDatePickerPanelType.WEEK && !weekStart(date).equals(hoveredWeekStart)) {
                hoveredWeekStart = weekStart(date);
                applyWeekHover();
            }
        });
        cell.setOnMouseExited(e -> {
            if (getType() == EleFXDatePickerPanelType.WEEK && hoveredWeekStart != null) {
                hoveredWeekStart = null;
                applyWeekHover();
            }
        });
        return cell;
    }

    private void configureGrid(int columns, double columnWidth, int rows, double rowHeight) {
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        for (int column = 0; column < columns; column++)
            grid.getColumnConstraints().add(new ColumnConstraints(columnWidth));
        for (int row = 0; row < rows; row++)
            grid.getRowConstraints().add(new RowConstraints(rowHeight));
        boolean dayTable = columns >= 7;
        boolean periodGrid = getType().usesMonthGrid() || getType().usesYearGrid();
        boolean expandedDaySpacing = getType() == EleFXDatePickerPanelType.DATE
                || getType() == EleFXDatePickerPanelType.DATES
                || getType() == EleFXDatePickerPanelType.DATETIME
                || getType() == EleFXDatePickerPanelType.WEEK;
        grid.setHgap(dayTable && !getType().isRange() ? 2 : 0);
        grid.setVgap(dayTable ? expandedDaySpacing ? 6 : 2 : periodGrid ? 6 : 0);
    }

    private boolean isInSelectedRange(LocalDate date) {
        if (!(getType().isRange() || getType() == EleFXDatePickerPanelType.WEEK) || values.size() != 2)
            return false;
        LocalDate first = values.get(0);
        LocalDate second = values.get(1);
        LocalDate start = first.isBefore(second) ? first : second;
        LocalDate end = first.isBefore(second) ? second : first;
        return date.isAfter(start) && date.isBefore(end);
    }

    private boolean isRangeEndpointPeriod(LocalDate date) {
        return getType().isRange() && (getType().usesMonthGrid() || getType().usesYearGrid()
                || getType().usesQuarterGrid())
                && values.size() == 2 && (date.equals(values.get(0)) || date.equals(values.get(1)));
    }

    private void select(LocalDate date) {
        List<LocalDate> next = new ArrayList<>(values);
        if (getType() == EleFXDatePickerPanelType.WEEK) {
            LocalDate start = weekStart(date);
            next = new ArrayList<>(List.of(start, start.plusDays(6)));
        } else if (getType().isRange()) {
            if (next.size() != 1)
                next = new ArrayList<>(List.of(date));
            else {
                next.add(date);
                next.sort(Comparator.naturalOrder());
            }
        } else if (getType().isMultiple()) {
            if (next.contains(date))
                next.remove(date);
            else
                next.add(date);
        } else
            next = new ArrayList<>(List.of(date));
        setSelectedValues(next);
        if (getType() == EleFXDatePickerPanelType.DATETIME) updateDateTime();
    }

    private LocalDate weekStart(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() % 7);
    }

    private void applyWeekHover() {
        for (Map.Entry<LocalDate, Button> entry : weekCells.entrySet()) {
            boolean hovered = entry.getKey().minusDays(entry.getKey().getDayOfWeek().getValue() % 7)
                    .equals(hoveredWeekStart);
            entry.getValue().getStyleClass().removeAll("ele-date-picker-panel__cell--week-hover",
                    "ele-date-picker-panel__cell--week-hover-start", "ele-date-picker-panel__cell--week-hover-end");
            if (hovered) {
                entry.getValue().getStyleClass().add("ele-date-picker-panel__cell--week-hover");
                if (entry.getKey().equals(hoveredWeekStart))
                    entry.getValue().getStyleClass().add("ele-date-picker-panel__cell--week-hover-start");
                else if (entry.getKey().equals(hoveredWeekStart.plusDays(6)))
                    entry.getValue().getStyleClass().add("ele-date-picker-panel__cell--week-hover-end");
            }
        }
    }

    private void setSelectedValues(List<LocalDate> selection) {
        synchronizingValues = true;
        values.setAll(selection == null ? List.of() : selection);
        value.set(values.isEmpty() ? null : values.get(0));
        synchronizingValues = false;
        updateDateInput();
        if (!values.isEmpty() && !getType().isMultiple() && !getType().isRange()
                && !getType().usesYearGrid())
            setDisplayedMonth(YearMonth.from(values.get(0)));
        repaint();
    }

    private void syncValueAndRepaint() {
        synchronizingValues = true;
        value.set(values.isEmpty() ? null : values.get(0));
        synchronizingValues = false;
        repaint();
    }

    private void addWeekLabel(String text, int column, int row) {
        Label label = new Label(text);
        label.getStyleClass().add("ele-date-picker-panel__week-number");
        label.setMinWidth(DAY_COLUMN_WIDTH);
        label.setAlignment(Pos.CENTER);
        grid.add(label, column, row);
    }

    private void updateShortcuts() {
        shortcutBar.getChildren().clear();
        for (EleFXDatePickerShortcut shortcut : shortcuts) {
            Button button = new Button(shortcut.getText());
            button.getStyleClass().add("ele-date-picker-panel__shortcut");
            button.setOnAction(e -> {
                setSelectedValues(shortcut.getValues());
                fire(onCalendarChange.get());
                fireChange();
            });
            shortcutBar.getChildren().add(button);
        }
        shortcutBar.setVisible(!shortcuts.isEmpty());
        shortcutBar.setManaged(!shortcuts.isEmpty());
    }

    private void updateFooter() {
        footer.getChildren().clear();
        if (isShowConfirm()) {
            Button ok = new Button("OK");
            ok.getStyleClass().addAll("ele-date-picker-panel__action", "ele-date-picker-panel__action--primary");
            ok.setOnAction(e -> {
                fireChange();
                fire(onConfirm.get());
            });
            footer.getChildren().add(ok);
        }
        boolean visible = isShowFooter() && isShowConfirm();
        footer.setVisible(visible);
        footer.setManaged(visible);
    }

    private void updateBorder() {
        if (isBordered())
            getStyleClass().remove("ele-date-picker-panel--borderless");
        else if (!getStyleClass().contains("ele-date-picker-panel--borderless"))
            getStyleClass().add("ele-date-picker-panel--borderless");
    }

    private void fireChange() {
        fire(onChange.get());
    }

    private void fire(EventHandler<ActionEvent> handler) {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (handler != null) handler.handle(event);
    }
}
