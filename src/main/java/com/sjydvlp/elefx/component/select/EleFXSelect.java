package com.sjydvlp.elefx.component.select;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An Element Plus inspired select control with optional filtering and multi-selection.
 *
 * <p>
 * The {@linkplain #getOptions() options} carry both the user-visible label and
 * stored value. In multiple mode {@link #getValues()} is the source of truth;
 * in normal mode {@link #getValue()} exposes the sole selected value.
 * </p>
 */
public class EleFXSelect<T> extends HBox implements Themable {

    private static final String STYLE_CLASS = "ele-select";

    private final ObservableList<EleFXSelectOption<T>> options = FXCollections
            .observableArrayList(option -> new Observable[] {option.disabledProperty()});

    private final ObservableList<EleFXSelectOptionGroup<T>> optionGroups = FXCollections.observableArrayList(
            group -> new Observable[] {group.labelProperty(), group.disabledProperty()});

    private final ObservableList<T> values = FXCollections.observableArrayList();

    private final StackPane trigger = new StackPane();

    private final StackPane triggerContent = new StackPane();

    private final Label display = new Label();

    private final TextField triggerInput = new TextField();

    private final FlowPane selectedTags = new FlowPane(4, 2);

    private final EleFXIcon arrow = new EleFXIcon(EleFXIconType.ARROW_DOWN, 12);

    private final RotateTransition arrowTransition = new RotateTransition(Duration.millis(200), arrow);

    private final EleFXIcon clearButton = new EleFXIcon(EleFXIconType.CIRCLE_CLOSE, 12);

    private final PopupControl popup = new PopupControl();

    private final VBox popupContent = new VBox(4);

    private final TextField filterInput = new TextField();

    private final VBox optionList = new VBox();

    private final PopupControl collapsedTagsPopup = new PopupControl();

    private final VBox collapsedTagsPopupRoot = new VBox();

    private final HBox collapsedTagsPopupContent = new HBox(4);

    private final PauseTransition collapsedTagsPopupHide = new PauseTransition(Duration.millis(150));

    private final BooleanProperty multiple = new SimpleBooleanProperty(this, "multiple", false);

    private final BooleanProperty filterable = new SimpleBooleanProperty(this, "filterable", false);

    private final BooleanProperty remote = new SimpleBooleanProperty(this, "remote", false);

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);

    private final ObjectProperty<Node> loadingNode = new SimpleObjectProperty<>(this, "loadingNode");

    private final ObjectProperty<Consumer<String>> remoteMethod = new SimpleObjectProperty<>(this, "remoteMethod");

    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", false);

    private final BooleanProperty collapseTags = new SimpleBooleanProperty(this, "collapseTags", false);

    private final BooleanProperty collapseTagsTooltip = new SimpleBooleanProperty(this, "collapseTagsTooltip", false);

    private final IntegerProperty maxCollapseTags = new SimpleIntegerProperty(this, "maxCollapseTags", 1);

    private final ObjectProperty<EleFXSelectSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXSelectSize.DEFAULT);

    private final StringProperty placeholder = new SimpleStringProperty(this, "placeholder", "Select");

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private boolean popupPositionUpdatePending;

    private final EventHandler<MouseEvent> outsideClickHandler = event -> {
        if (trigger.isFocused() && !isInsideSelect(event.getTarget())) {
            if (isRemote()) filterInput.clear();
            // Let the click finish first: a focusable target should receive focus,
            // while a blank scene area explicitly takes focus from the trigger.
            Platform.runLater(this::clearTriggerFocus);
        }
    };

    public EleFXSelect() {
        initialize();
    }

    @SafeVarargs
    public EleFXSelect(EleFXSelectOption<T>... options) {
        this();
        getOptions().addAll(options);
    }

    public ObservableList<EleFXSelectOption<T>> getOptions() {
        return options;
    }

    /** Groups rendered after any flat {@linkplain #getOptions() options}. */
    public ObservableList<EleFXSelectOptionGroup<T>> getOptionGroups() {
        return optionGroups;
    }

    /** Adds option groups without exposing JavaFX's unchecked generic-varargs warning. */
    @SafeVarargs
    public final boolean addOptionGroups(EleFXSelectOptionGroup<T>... groups) {
        return optionGroups.addAll(List.of(groups));
    }

    /** Returns an unmodifiable view of the selected values. */
    public ObservableList<T> getValues() {
        return FXCollections.unmodifiableObservableList(values);
    }

    public T getValue() {
        return values.isEmpty() ? null : values.get(0);
    }

    public void setValue(T value) {
        if (value == null)
            clear();
        else {
            values.setAll(value);
            updateDisplay();
        }
    }

    public boolean isMultiple() {
        return multiple.get();
    }

    public BooleanProperty multipleProperty() {
        return multiple;
    }

    public void setMultiple(boolean value) {
        multiple.set(value);
    }

    public boolean isFilterable() {
        return filterable.get();
    }

    public BooleanProperty filterableProperty() {
        return filterable;
    }

    public void setFilterable(boolean value) {
        filterable.set(value);
    }

    public boolean isRemote() {
        return remote.get();
    }

    public BooleanProperty remoteProperty() {
        return remote;
    }

    public void setRemote(boolean value) {
        remote.set(value);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean value) {
        loading.set(value);
    }

    /** Node rendered in the dropdown while loading. */
    public Node getLoadingNode() {
        return loadingNode.get();
    }

    public ObjectProperty<Node> loadingNodeProperty() {
        return loadingNode;
    }

    public void setLoadingNode(Node value) {
        loadingNode.set(value);
    }

    public Consumer<String> getRemoteMethod() {
        return remoteMethod.get();
    }

    public ObjectProperty<Consumer<String>> remoteMethodProperty() {
        return remoteMethod;
    }

    public void setRemoteMethod(Consumer<String> value) {
        remoteMethod.set(value);
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

    /** Whether multiple selected tags collapse into a summary tag. */
    public boolean isCollapseTags() {
        return collapseTags.get();
    }

    public BooleanProperty collapseTagsProperty() {
        return collapseTags;
    }

    public void setCollapseTags(boolean value) {
        collapseTags.set(value);
    }

    /** Whether the collapsed summary displays all selected labels in a tooltip. */
    public boolean isCollapseTagsTooltip() {
        return collapseTagsTooltip.get();
    }

    public BooleanProperty collapseTagsTooltipProperty() {
        return collapseTagsTooltip;
    }

    public void setCollapseTagsTooltip(boolean value) {
        collapseTagsTooltip.set(value);
    }

    /** Maximum number of individual tags shown when {@link #isCollapseTags()} is enabled. */
    public int getMaxCollapseTags() {
        return maxCollapseTags.get();
    }

    public IntegerProperty maxCollapseTagsProperty() {
        return maxCollapseTags;
    }

    public void setMaxCollapseTags(int value) {
        maxCollapseTags.set(Math.max(0, value));
    }

    /** Element Plus visual size; the default height is 32px. */
    public EleFXSelectSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXSelectSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXSelectSize value) {
        size.set(value == null ? EleFXSelectSize.DEFAULT : value);
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

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<ActionEvent> handler) {
        onChange.set(handler);
    }

    /** Clears every selected value and closes the dropdown. */
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
        return EleFXThemes.SELECT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        // A select is inline-sized by default. In particular, a VBox must not
        // stretch this wrapper while the trigger retains its preferred width.
        setMaxWidth(Region.USE_PREF_SIZE);
        trigger.getStyleClass().add("ele-select__trigger");
        display.getStyleClass().add("ele-select__display");
        arrow.getStyleClass().add("ele-select__arrow");
        triggerInput.getStyleClass().add("ele-select__input");
        triggerInput.setPromptText(getPlaceholder());
        triggerInput.setFocusTraversable(true);
        triggerInput.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (focused)
                trigger.getStyleClass().add("ele-select__trigger--focused");
            else
                trigger.getStyleClass().remove("ele-select__trigger--focused");
        });
        triggerInput.setOnMouseClicked(event -> {
            if (!popup.isShowing()) showPopup();
            event.consume();
        });
        triggerInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) popup.hide();
        });
        triggerContent.getChildren().addAll(display, triggerInput, selectedTags, arrow, clearButton);
        triggerContent.getStyleClass().add("ele-select__trigger-content");
        triggerContent.setPrefWidth(156);
        triggerContent.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(display, Pos.CENTER_LEFT);
        StackPane.setAlignment(triggerInput, Pos.CENTER_LEFT);
        selectedTags.getStyleClass().add("ele-select__tags");
        selectedTags.setPadding(new Insets(0, 20, 0, 0));
        selectedTags.setPrefWidth(156);
        selectedTags.setPrefWrapLength(156);
        // FlowPane otherwise derives its minimum height from a narrow layout
        // and can force every tag onto a separate theoretical row.
        selectedTags.setMinHeight(0);
        selectedTags.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(selectedTags, Pos.CENTER_LEFT);
        StackPane.setAlignment(arrow, Pos.CENTER_RIGHT);
        StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
        trigger.getChildren().add(triggerContent);
        trigger.setMaxWidth(Double.MAX_VALUE);
        trigger.setFocusTraversable(true);
        trigger.setOnMouseClicked(event -> togglePopup());
        trigger.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                togglePopup();
                event.consume();
            }
        });
        trigger.widthProperty().addListener((observable, oldWidth, newWidth) -> updateTriggerContentWidth());
        trigger.hoverProperty().addListener((observable, oldValue, hovered) -> updateClearButton());
        trigger.localToSceneTransformProperty()
                .addListener((observable, oldTransform, newTransform) -> schedulePopupPositionUpdate());
        trigger.heightProperty()
                .addListener((observable, oldHeight, newHeight) -> schedulePopupPositionUpdate());
        widthProperty().addListener((observable, oldWidth, newWidth) -> schedulePopupPositionUpdate());

        clearButton.getStyleClass().add("ele-select__clear");
        clearButton.setFocusTraversable(false);
        clearButton.setOnMousePressed(MouseEvent::consume);
        clearButton.setOnMouseReleased(MouseEvent::consume);
        clearButton.setOnMouseClicked(event -> {
            trigger.requestFocus();
            clear();
            event.consume();
        });
        getChildren().add(trigger);
        HBox.setHgrow(trigger, Priority.ALWAYS);

        popupContent.getStyleClass().add("ele-select__dropdown");
        popupContent.getStylesheets().add(EleFXThemes.SELECT.toData());
        filterInput.getStyleClass().add("ele-select__filter");
        filterInput.textProperty().bindBidirectional(triggerInput.textProperty());
        filterInput.setPromptText("Search");
        filterInput.textProperty().addListener((observable, oldValue, value) -> {
            if (isRemote() && getRemoteMethod() != null)
                getRemoteMethod().accept(value == null ? "" : value);
            else
                rebuildOptions();
        });
        filterInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                popup.hide();
            else if (event.getCode() == KeyCode.ENTER) selectFirstVisible();
        });
        optionList.getStyleClass().add("ele-select__options");
        ScrollPane scroll = new ScrollPane(optionList);
        scroll.getStyleClass().add("ele-select__scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        popupContent.getChildren().addAll(filterInput, scroll);
        VBox.setMargin(filterInput, new Insets(0, 8, 4, 8));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        popup.getScene().setRoot(popupContent);
        // Anchor the rendered dropdown rather than the PopupWindow's outer
        // bounds, which can include shadow and platform-specific insets.
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> Platform.runLater(this::clearTriggerFocus));
        popup.showingProperty().addListener((observable, oldValue, showing) -> {
            updateArrowRotation(showing);
            updateDisplay();
        });

        Polygon collapsedTagsPopupArrow = new Polygon(0, 8, 8, 0, 16, 8);
        collapsedTagsPopupArrow.getStyleClass().add("ele-select__collapsed-tags-tooltip-arrow");
        collapsedTagsPopupRoot.getStyleClass().add("ele-select__collapsed-tags-tooltip");
        collapsedTagsPopupContent.getStyleClass().add("ele-select__collapsed-tags-tooltip-content");
        collapsedTagsPopupContent.setAlignment(Pos.CENTER_LEFT);
        collapsedTagsPopupRoot.getChildren().addAll(collapsedTagsPopupArrow, collapsedTagsPopupContent);
        collapsedTagsPopupRoot.setAlignment(Pos.TOP_CENTER);
        collapsedTagsPopupRoot.setOnMouseEntered(event -> cancelCollapsedTagsPopupHide());
        collapsedTagsPopupRoot.setOnMouseExited(event -> scheduleCollapsedTagsPopupHide());
        collapsedTagsPopupRoot.getStylesheets().add(EleFXThemes.SELECT.toData());
        collapsedTagsPopup.getScene().setRoot(collapsedTagsPopupRoot);
        collapsedTagsPopup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        collapsedTagsPopup.setAutoHide(false);
        collapsedTagsPopupHide.setOnFinished(event -> collapsedTagsPopup.hide());

        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
        });
        if (getScene() != null) getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);

        options.addListener((ListChangeListener<EleFXSelectOption<T>>) change -> {
            updateDisplay();
            rebuildOptions();
        });
        optionGroups.addListener((ListChangeListener<EleFXSelectOptionGroup<T>>) change -> {
            while (change.next())
                for (EleFXSelectOptionGroup<T> group : change.getAddedSubList())
                    group.getOptions().addListener((ListChangeListener<EleFXSelectOption<T>>) ignored -> {
                        updateDisplay();
                        rebuildOptions();
                    });
            updateDisplay();
            rebuildOptions();
        });
        multiple.addListener((observable, oldValue, value) -> {
            if (!value && values.size() > 1) values.remove(1, values.size());
            updateDisplay();
            rebuildOptions();
        });
        filterable.addListener((observable, oldValue, value) -> updateFilterInputVisibility());
        remote.addListener((observable, oldValue, value) -> {
            updateFilterInputVisibility();
            updateDisplay();
        });
        loading.addListener((observable, oldValue, value) -> rebuildOptions());
        loadingNode.addListener((observable, oldValue, value) -> rebuildOptions());
        clearable.addListener((observable, oldValue, value) -> updateClearButton());
        collapseTags.addListener((observable, oldValue, value) -> updateDisplay());
        collapseTagsTooltip.addListener((observable, oldValue, value) -> updateDisplay());
        maxCollapseTags.addListener((observable, oldValue, value) -> {
            if (value.intValue() < 0)
                maxCollapseTags.set(0);
            else
                updateDisplay();
        });
        size.addListener((observable, oldValue, value) -> updateSizeStyle(oldValue, value));
        placeholder.addListener((observable, oldValue, value) -> updateDisplay());
        updateFilterInputVisibility();
        updateClearButton();
        updateSizeStyle(null, getSize());
        updateDisplay();
        sceneBuilderIntegration();
    }

    private void togglePopup() {
        if (popup.isShowing())
            popup.hide();
        else
            showPopup();
    }

    private void showPopup() {
        rebuildOptions();
        Bounds triggerBounds = trigger.localToScreen(trigger.getBoundsInLocal());
        if (triggerBounds == null) return;
        popup.show(trigger, triggerBounds.getMinX(), triggerBounds.getMaxY() + 4);
        schedulePopupPositionUpdate();
        if (isRemote() || isFilterable())
            triggerInput.requestFocus();
    }

    private void rebuildOptions() {
        optionList.getChildren().clear();
        if (isLoading()) {
            Node content = getLoadingNode();
            if (content == null) {
                Label loadingLabel = new Label("Loading");
                loadingLabel.getStyleClass().add("ele-select__loading");
                content = loadingLabel;
            }
            StackPane loadingContainer = new StackPane(content);
            loadingContainer.getStyleClass().add("ele-select__loading-container");
            loadingContainer.setMaxWidth(Double.MAX_VALUE);
            StackPane.setAlignment(content, Pos.CENTER);
            optionList.getChildren().add(loadingContainer);
            return;
        }
        String query = filterInput.getText() == null ? "" : filterInput.getText().trim().toLowerCase();
        for (EleFXSelectOption<T> option : options)
            addOption(option, false, query);
        for (EleFXSelectOptionGroup<T> group : optionGroups) {
            boolean visible = group.getOptions().stream().anyMatch(option -> query.isEmpty()
                    || option.getLabel().toLowerCase().contains(query));
            if (!visible) continue;
            Label groupLabel = new Label(group.getLabel());
            groupLabel.getStyleClass().add("ele-select__group-label");
            optionList.getChildren().add(groupLabel);
            for (EleFXSelectOption<T> option : group.getOptions())
                addOption(option, group.isDisabled(), query);
        }
        if (optionList.getChildren().isEmpty()) {
            Label empty = new Label("No matching data");
            empty.getStyleClass().add("ele-select__empty");
            optionList.getChildren().add(empty);
        }
    }

    private void addOption(EleFXSelectOption<T> option, boolean groupDisabled, String query) {
        if (!query.isEmpty() && !option.getLabel().toLowerCase().contains(query)) return;
        Button item = new Button();
        item.getStyleClass().add("ele-select__option");
        boolean selected = values.stream().anyMatch(value -> Objects.equals(value, option.getValue()));
        if (selected) item.getStyleClass().add("ele-select__option--selected");
        Label optionLabel = new Label(option.getLabel());
        optionLabel.getStyleClass().add("ele-select__option-label");
        EleFXIcon check = new EleFXIcon(EleFXIconType.CHECK, 14);
        check.getStyleClass().add("ele-select__check");
        check.setVisible(isMultiple() && selected);
        StackPane optionContent = new StackPane(optionLabel, check);
        optionContent.getStyleClass().add("ele-select__option-content");
        optionContent.setPrefWidth(140);
        StackPane.setAlignment(optionLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(check, Pos.CENTER_RIGHT);
        item.setGraphic(optionContent);
        item.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        item.widthProperty()
                .addListener((observable, oldWidth, newWidth) -> updateOptionContentWidth(item, optionContent));
        item.setDisable(groupDisabled || option.isDisabled());
        item.setMaxWidth(Double.MAX_VALUE);
        item.setOnAction(event -> select(option));
        optionList.getChildren().add(item);
    }

    private void selectFirstVisible() {
        for (EleFXSelectOption<T> option : options) {
            if (!option.isDisabled() && (filterInput.getText().isBlank()
                    || option.getLabel().toLowerCase().contains(filterInput.getText().trim().toLowerCase()))) {
                select(option);
                return;
            }
        }
        for (EleFXSelectOptionGroup<T> group : optionGroups) {
            if (group.isDisabled()) continue;
            for (EleFXSelectOption<T> option : group.getOptions()) {
                if (!option.isDisabled() && (filterInput.getText().isBlank()
                        || option.getLabel().toLowerCase().contains(filterInput.getText().trim().toLowerCase()))) {
                    select(option);
                    return;
                }
            }
        }
    }

    private void select(EleFXSelectOption<T> option) {
        if (isMultiple()) {
            if (values.contains(option.getValue()))
                values.remove(option.getValue());
            else
                values.add(option.getValue());
        } else {
            values.setAll(option.getValue());
            filterInput.clear();
            popup.hide();
            // The popup's auto-hide cleanup runs later; request focus after it
            // so selection never transfers focus to another form control.
            Platform.runLater(() -> Platform.runLater(trigger::requestFocus));
        }
        updateDisplay();
        rebuildOptions();
        fireChange();
    }

    private void updateDisplay() {
        updateTagWrapLength();
        boolean showTags = isMultiple() && !values.isEmpty();
        boolean showTriggerInput = !isMultiple() && popup.isShowing() && (isFilterable() || isRemote());
        selectedTags.setVisible(showTags);
        selectedTags.setManaged(showTags);
        triggerInput.setVisible(showTriggerInput);
        triggerInput.setManaged(showTriggerInput);
        display.setVisible(!showTags && !showTriggerInput);
        display.setManaged(!showTags && !showTriggerInput);
        if (values.isEmpty()) {
            display.setText(getPlaceholder());
            display.getStyleClass().remove("ele-select__display--placeholder");
            display.getStyleClass().add("ele-select__display--placeholder");
            updateClearButton();
            return;
        }
        display.getStyleClass().remove("ele-select__display--placeholder");
        display.setText(values.stream().map(this::labelFor).filter(Objects::nonNull)
                .reduce((left, right) -> left + ", " + right).orElse(""));
        rebuildSelectedTags();
        updateClearButton();
    }

    private void rebuildSelectedTags() {
        collapsedTagsPopupHide.stop();
        collapsedTagsPopup.hide();
        selectedTags.getChildren().clear();
        int visibleTagCount = isCollapseTags() ? Math.min(values.size(), getMaxCollapseTags()) : values.size();
        for (int index = 0; index < visibleTagCount; index++)
            selectedTags.getChildren().add(createSelectedTag(values.get(index)));
        if (visibleTagCount < values.size()) addCollapsedTag(visibleTagCount, values.size() - visibleTagCount);
    }

    private HBox createSelectedTag(T value) {
        HBox tag = new HBox(4);
        tag.getStyleClass().add("ele-select__tag");
        tag.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(labelFor(value));
        label.getStyleClass().add("ele-select__tag-label");
        EleFXIcon close = new EleFXIcon(EleFXIconType.CLOSE, 12);
        close.getStyleClass().add("ele-select__tag-close");
        close.setOnMousePressed(MouseEvent::consume);
        close.setOnMouseReleased(MouseEvent::consume);
        close.setOnMouseClicked(event -> {
            trigger.requestFocus();
            values.remove(value);
            updateDisplay();
            rebuildOptions();
            fireChange();
            event.consume();
        });
        tag.getChildren().addAll(label, close);
        return tag;
    }

    private void addCollapsedTag(int firstHiddenTagIndex, int hiddenTagCount) {
        HBox tag = new HBox();
        tag.getStyleClass().addAll("ele-select__tag", "ele-select__tag--collapsed");
        tag.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("+ " + hiddenTagCount);
        label.getStyleClass().add("ele-select__tag-label");
        tag.getChildren().add(label);
        if (isCollapseTagsTooltip()) {
            tag.setOnMouseEntered(event -> {
                cancelCollapsedTagsPopupHide();
                showCollapsedTagsPopup(tag, firstHiddenTagIndex);
            });
            tag.setOnMouseExited(event -> scheduleCollapsedTagsPopupHide());
        }
        selectedTags.getChildren().add(tag);
    }

    private void showCollapsedTagsPopup(Node anchor, int firstHiddenTagIndex) {
        collapsedTagsPopupContent.getChildren().clear();
        for (int index = firstHiddenTagIndex; index < values.size(); index++)
            collapsedTagsPopupContent.getChildren().add(createSelectedTag(values.get(index)));
        Bounds triggerBounds = trigger.localToScreen(trigger.getBoundsInLocal());
        Bounds anchorBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (triggerBounds == null || anchorBounds == null) return;
        if (!collapsedTagsPopup.isShowing())
            collapsedTagsPopup.show(anchor, anchorBounds.getMinX(), triggerBounds.getMaxY() + 2);
        else {
            collapsedTagsPopup.setAnchorX(anchorBounds.getMinX());
            collapsedTagsPopup.setAnchorY(triggerBounds.getMaxY() + 2);
        }
        Platform.runLater(() -> {
            if (!collapsedTagsPopup.isShowing()) return;
            double anchorCenterX = anchorBounds.getMinX() + anchorBounds.getWidth() / 2;
            collapsedTagsPopup.setAnchorX(anchorCenterX - collapsedTagsPopupRoot.getWidth() / 2);
        });
    }

    private void scheduleCollapsedTagsPopupHide() {
        collapsedTagsPopupHide.playFromStart();
    }

    private void cancelCollapsedTagsPopupHide() {
        collapsedTagsPopupHide.stop();
    }

    private String labelFor(T value) {
        for (EleFXSelectOption<T> option : allOptions())
            if (Objects.equals(option.getValue(), value)) return option.getLabel();
        return String.valueOf(value);
    }

    private ObservableList<EleFXSelectOption<T>> allOptions() {
        ObservableList<EleFXSelectOption<T>> result = FXCollections.observableArrayList(options);
        for (EleFXSelectOptionGroup<T> group : optionGroups)
            result.addAll(group.getOptions());
        return result;
    }

    private void updateClearButton() {
        boolean showClearButton = isClearable() && !values.isEmpty() && trigger.isHover();
        clearButton.setVisible(showClearButton);
        arrow.setVisible(!showClearButton);
    }

    private void updateFilterInputVisibility() {
        filterInput.setManaged(false);
        filterInput.setVisible(false);
    }

    private void updateTriggerContentWidth() {
        Insets insets = trigger.getInsets();
        double availableWidth = trigger.getWidth() - insets.getLeft() - insets.getRight();
        if (availableWidth > 0) {
            triggerContent.setPrefWidth(availableWidth);
            selectedTags.setPrefWidth(availableWidth);
            selectedTags.setPrefWrapLength(availableWidth);
        }
    }

    private void updateTagWrapLength() {
        double componentWidth = getWidth() > 0 ? getWidth() : getPrefWidth();
        if (componentWidth == Region.USE_COMPUTED_SIZE || componentWidth <= 0) return;
        Insets insets = trigger.getInsets();
        double availableWidth = componentWidth - insets.getLeft() - insets.getRight();
        if (availableWidth > 0) {
            selectedTags.setPrefWidth(availableWidth);
            selectedTags.setPrefWrapLength(availableWidth);
        }
    }

    private void updateOptionContentWidth(Button item, StackPane content) {
        Insets insets = item.getInsets();
        double availableWidth = item.getWidth() - insets.getLeft() - insets.getRight();
        if (availableWidth > 0) content.setPrefWidth(availableWidth);
    }

    private void updateArrowRotation(boolean expanded) {
        arrowTransition.stop();
        arrowTransition.setFromAngle(arrow.getRotate());
        arrowTransition.setToAngle(expanded ? 180 : 0);
        arrowTransition.playFromStart();
    }

    private void schedulePopupPositionUpdate() {
        if (!popup.isShowing() || popupPositionUpdatePending) return;
        popupPositionUpdatePending = true;
        Platform.runLater(() -> {
            popupPositionUpdatePending = false;
            updatePopupPosition();
        });
    }

    private void updatePopupPosition() {
        if (!popup.isShowing()) return;
        Bounds triggerBounds = trigger.localToScreen(trigger.getBoundsInLocal());
        if (triggerBounds == null) return;
        popup.setAnchorX(triggerBounds.getMinX());
        popup.setAnchorY(triggerBounds.getMaxY() + 4);
    }

    private boolean isInsideSelect(Object target) {
        if (!(target instanceof Node node)) return false;
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == this) return true;
        }
        return false;
    }

    private void clearTriggerFocus() {
        Scene scene = getScene();
        if (scene != null && scene.getRoot() != null)
            scene.getRoot().requestFocus();
        else
            requestFocus();
    }

    private void updateSizeStyle(EleFXSelectSize oldSize, EleFXSelectSize newSize) {
        if (oldSize != null) {
            getStyleClass().remove(oldSize.styleClass());
            popupContent.getStyleClass().remove(oldSize.styleClass());
        }
        EleFXSelectSize resolved = newSize == null ? EleFXSelectSize.DEFAULT : newSize;
        if (!getStyleClass().contains(resolved.styleClass())) getStyleClass().add(resolved.styleClass());
        if (!popupContent.getStyleClass().contains(resolved.styleClass()))
            popupContent.getStyleClass().add(resolved.styleClass());
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }
}
