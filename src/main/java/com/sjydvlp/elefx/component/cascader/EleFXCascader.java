package com.sjydvlp.elefx.component.cascader;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Element Plus inspired hierarchical selector with a column-based popup. */
public class EleFXCascader<T> extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-cascader";

    private final ObservableList<EleFXCascaderOption<T>> options = FXCollections.observableArrayList();

    private final ObservableList<List<T>> values = FXCollections.observableArrayList();

    private final Button trigger = new Button();

    private final Label triggerText = new Label();

    private final Button clearButton = new Button("×");

    private final PopupControl popup = new PopupControl();

    private final HBox popupContent = new HBox();

    private final BooleanProperty multiple = new SimpleBooleanProperty(this, "multiple", false);

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false);

    private final BooleanProperty showAllLevels = new SimpleBooleanProperty(this, "showAllLevels", true);

    private final StringProperty placeholder = new SimpleStringProperty(this, "placeholder", "Select");

    private final StringProperty separator = new SimpleStringProperty(this, "separator", " / ");

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    public EleFXCascader() {
        initialize();
    }

    @SafeVarargs
    public EleFXCascader(EleFXCascaderOption<T>... options) {
        this();
        this.options.addAll(options);
    }

    public ObservableList<EleFXCascaderOption<T>> getOptions() {
        return options;
    }

    /** Selected value paths; single selection contains zero or one path. */
    public ObservableList<List<T>> getValues() {
        return FXCollections.unmodifiableObservableList(values);
    }

    public List<T> getValue() {
        return values.isEmpty() ? List.of() : List.copyOf(values.get(0));
    }

    public boolean isMultiple() {
        return multiple.get();
    }

    public BooleanProperty multipleProperty() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple.set(multiple);
    }

    public boolean isClearable() {
        return clearable.get();
    }

    public BooleanProperty clearableProperty() {
        return clearable;
    }

    public void setClearable(boolean clearable) {
        this.clearable.set(clearable);
    }

    public boolean isShowAllLevels() {
        return showAllLevels.get();
    }

    public BooleanProperty showAllLevelsProperty() {
        return showAllLevels;
    }

    public void setShowAllLevels(boolean showAllLevels) {
        this.showAllLevels.set(showAllLevels);
    }

    public String getPlaceholder() {
        return placeholder.get();
    }

    public StringProperty placeholderProperty() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder.set(placeholder == null ? "" : placeholder);
    }

    public String getSeparator() {
        return separator.get();
    }

    public StringProperty separatorProperty() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator.set(separator == null ? " / " : separator);
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

    public void clear() {
        if (!values.isEmpty()) {
            values.clear();
            updateDisplay();
            fireChange();
        }
        popup.hide();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CASCADER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        trigger.getStyleClass().add("ele-cascader__trigger");
        EleFXIcon arrow = new EleFXIcon(EleFXIconType.ARROW_DOWN, 12);
        arrow.getStyleClass().add("ele-cascader__arrow");
        StackPane triggerContent = new StackPane();
        triggerContent.getStyleClass().add("ele-cascader__trigger-content");
        StackPane.setAlignment(triggerText, Pos.CENTER_LEFT);
        StackPane.setAlignment(arrow, Pos.CENTER_RIGHT);
        triggerContent.getChildren().addAll(triggerText, arrow);
        triggerContent.setPrefWidth(156);
        trigger.setText(null);
        trigger.setGraphic(triggerContent);
        trigger.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        clearButton.getStyleClass().add("ele-cascader__clear");
        clearButton.setFocusTraversable(false);
        clearButton.setOnAction(event -> clear());
        getChildren().addAll(trigger, clearButton);
        trigger.setOnAction(event -> showMenu());
        popupContent.getStyleClass().add("ele-cascader__menu");
        popupContent.getStylesheets().add(EleFXThemes.CASCADER.toData());
        popup.getScene().setRoot(popupContent);
        popup.setAutoHide(true);
        // A click on an unfocusable area closes the popup but otherwise leaves the
        // trigger button focused. Move focus to the composite control on close so
        // its input-style focus border is cleared as well.
        popup.setOnHidden(event -> requestFocus());
        clearable.addListener((observable, oldValue, value) -> updateClearButton());
        multiple.addListener((observable, oldValue, value) -> {
            if (!value && values.size() > 1) values.remove(1, values.size());
            updateDisplay();
        });
        showAllLevels.addListener(observable -> updateDisplay());
        placeholder.addListener(observable -> updateDisplay());
        separator.addListener(observable -> updateDisplay());
        updateClearButton();
        updateDisplay();
        sceneBuilderIntegration();
    }

    private void showMenu() {
        popupContent.getChildren().clear();
        showColumn(options, List.of(), 0);
        expandSelectedPath();
        javafx.geometry.Bounds bounds = trigger.localToScreen(trigger.getBoundsInLocal());
        if (!options.isEmpty() && bounds != null) popup.show(trigger, bounds.getMinX(), bounds.getMaxY() + 4);
    }

    /** Restores the open columns for the current single-selection path. */
    private void expandSelectedPath() {
        if (values.isEmpty()) return;

        List<T> selectedPath = values.get(0);
        List<EleFXCascaderOption<T>> nodes = options;
        List<EleFXCascaderOption<T>> ancestors = new ArrayList<>();
        for (int level = 0; level < selectedPath.size(); level++) {
            T selectedValue = selectedPath.get(level);
            EleFXCascaderOption<T> selected = nodes.stream()
                    .filter(node -> Objects.equals(node.getValue(), selectedValue))
                    .findFirst()
                    .orElse(null);
            if (selected == null || selected.isLeaf()) return;

            ancestors.add(selected);
            nodes = selected.getChildren();
            showColumn(nodes, ancestors, level + 1);
        }
    }

    private void showColumn(List<EleFXCascaderOption<T>> nodes, List<EleFXCascaderOption<T>> ancestors, int level) {
        while (popupContent.getChildren().size() > level) {
            popupContent.getChildren().remove(level, popupContent.getChildren().size());
        }
        double columnWidth = columnWidth(nodes);
        VBox column = new VBox();
        column.getStyleClass().add("ele-cascader__column");
        column.setMinWidth(columnWidth);
        column.setPrefWidth(columnWidth);
        column.setMaxWidth(columnWidth);
        for (EleFXCascaderOption<T> node : nodes) {
            List<EleFXCascaderOption<T>> path = new ArrayList<>(ancestors);
            path.add(node);
            Button option = new Button(node.getLabel());
            option.getStyleClass().add("ele-cascader__option");
            if (isSelectedPath(path)) {
                option.getStyleClass().add("ele-cascader__option--selected");
            } else if (isSelectedPathPrefix(path)) {
                option.getStyleClass().add("ele-cascader__option--active-path");
            }
            option.setMinWidth(columnWidth);
            option.setPrefWidth(columnWidth);
            option.setMaxWidth(columnWidth);
            option.setDisable(node.isDisabled());
            if (!node.isLeaf()) {
                EleFXIcon arrow = new EleFXIcon(EleFXIconType.ARROW_RIGHT, 10);
                arrow.getStyleClass().add("ele-cascader__option-arrow");
                StackPane optionContent = new StackPane();
                optionContent.getStyleClass().add("ele-cascader__option-content");
                Label optionLabel = new Label(node.getLabel());
                StackPane.setAlignment(optionLabel, Pos.CENTER_LEFT);
                StackPane.setAlignment(arrow, Pos.CENTER_RIGHT);
                optionContent.getChildren().addAll(optionLabel, arrow);
                optionContent.setPrefWidth(columnWidth - 24);
                option.setText(null);
                option.setGraphic(optionContent);
                option.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                option.setOnAction(event -> showColumn(node.getChildren(), path, level + 1));
            } else if (isMultiple()) {
                option.setText((values.contains(valuePath(path)) ? "✓  " : "     ") + node.getLabel());
                option.setOnAction(event -> {
                    toggle(path);
                    showColumn(nodes, ancestors, level);
                });
            } else {
                option.setOnAction(event -> select(path));
            }
            column.getChildren().add(option);
        }
        ScrollPane columnScroll = new ScrollPane(column);
        columnScroll.getStyleClass().add("ele-cascader__column-scroll");
        columnScroll.setFitToWidth(true);
        columnScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        columnScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        columnScroll.setMinWidth(columnWidth);
        columnScroll.setPrefWidth(columnWidth);
        columnScroll.setMaxWidth(columnWidth);
        popupContent.getChildren().add(columnScroll);
    }

    private void select(List<EleFXCascaderOption<T>> path) {
        values.setAll(List.of(valuePath(path)));
        updateDisplay();
        popup.hide();
        fireChange();
    }

    private void toggle(List<EleFXCascaderOption<T>> path) {
        List<T> value = valuePath(path);
        if (values.contains(value))
            values.remove(value);
        else
            values.add(value);
        updateDisplay();
        fireChange();
    }

    private List<T> valuePath(List<EleFXCascaderOption<T>> path) {
        return path.stream().map(EleFXCascaderOption::getValue).toList();
    }

    private boolean isSelectedPath(List<EleFXCascaderOption<T>> path) {
        return values.contains(valuePath(path));
    }

    private boolean isSelectedPathPrefix(List<EleFXCascaderOption<T>> path) {
        List<T> prefix = valuePath(path);
        return values.stream().anyMatch(value -> value.size() > prefix.size()
                && value.subList(0, prefix.size()).equals(prefix));
    }

    private double columnWidth(List<EleFXCascaderOption<T>> nodes) {
        double widest = 0;
        for (EleFXCascaderOption<T> node : nodes) {
            Text measure = new Text(node.getLabel());
            measure.setFont(Font.font(14));
            widest = Math.max(widest, measure.getLayoutBounds().getWidth());
        }
        // 24px horizontal padding, 16px arrow space, and 12px breathing room.
        return Math.max(112, Math.min(240, Math.ceil(widest + 52)));
    }

    private void updateDisplay() {
        if (values.isEmpty()) {
            triggerText.setText(getPlaceholder());
            return;
        }
        List<String> labels = new ArrayList<>();
        for (List<T> path : values)
            labels.add(displayPath(path));
        triggerText.setText(String.join(", ", labels));
    }

    private String displayPath(List<T> valuePath) {
        List<String> labels = new ArrayList<>();
        collectLabels(options, valuePath, 0, labels);
        return isShowAllLevels()
                ? String.join(getSeparator(), labels)
                : labels.isEmpty() ? "" : labels.get(labels.size() - 1);
    }

    private boolean collectLabels(List<EleFXCascaderOption<T>> nodes, List<T> values, int level, List<String> labels) {
        if (level >= values.size()) return true;
        for (EleFXCascaderOption<T> node : nodes) {
            if (Objects.equals(node.getValue(), values.get(level))) {
                labels.add(node.getLabel());
                return collectLabels(node.getChildren(), values, level + 1, labels);
            }
        }
        return false;
    }

    private void updateClearButton() {
        clearButton.setVisible(isClearable());
        clearButton.setManaged(isClearable());
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }
}
