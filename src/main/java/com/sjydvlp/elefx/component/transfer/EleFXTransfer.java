package com.sjydvlp.elefx.component.transfer;

import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.button.EleFXButtonType;
import com.sjydvlp.elefx.component.checkbox.EleFXCheckbox;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Element Plus inspired transfer control. Its {@link #getTargetValues()} list is
 * mutable and can be bound or updated by application code.
 */
public class EleFXTransfer<T> extends HBox implements Themable {

    private final ObservableList<EleFXTransferItem<T>> items = FXCollections.observableArrayList(
            item -> new Observable[] {item.labelProperty(), item.disabledProperty(), item.valueProperty()});

    private final ObservableList<T> targetValues = FXCollections.observableArrayList();

    private final BooleanProperty filterable = new SimpleBooleanProperty(this, "filterable", false);

    private final StringProperty filterPlaceholder = new SimpleStringProperty(this, "filterPlaceholder",
            "Enter keyword");

    private final StringProperty sourceTitle = new SimpleStringProperty(this, "sourceTitle", "List 1");

    private final StringProperty targetTitle = new SimpleStringProperty(this, "targetTitle", "List 2");

    private final StringProperty toLeftText = new SimpleStringProperty(this, "toLeftText", "To left");

    private final StringProperty toRightText = new SimpleStringProperty(this, "toRightText", "To right");

    private final ObjectProperty<Node> toLeftIcon = new SimpleObjectProperty<>(this, "toLeftIcon",
            new EleFXIcon(EleFXIconType.ARROW_LEFT, 14));

    private final ObjectProperty<Node> toRightIcon = new SimpleObjectProperty<>(this, "toRightIcon",
            new EleFXIcon(EleFXIconType.ARROW_RIGHT, 14));

    private final ObjectProperty<EleFXTransferTargetOrder> targetOrder = new SimpleObjectProperty<>(this, "targetOrder",
            EleFXTransferTargetOrder.ORIGINAL);

    private final ObjectProperty<BiPredicate<String, EleFXTransferItem<T>>> filterMethod = new SimpleObjectProperty<>(
            this, "filterMethod");

    private final ObjectProperty<Function<EleFXTransferItem<T>, String>> itemRenderer = new SimpleObjectProperty<>(this,
            "itemRenderer", EleFXTransferItem::getLabel);

    private final ObjectProperty<EventHandler<EleFXTransferEvent<T>>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final Panel sourcePanel = new Panel(false);

    private final Panel targetPanel = new Panel(true);

    private final EleFXButton leftButton = new EleFXButton();

    private final EleFXButton rightButton = new EleFXButton();

    private boolean synchronizing;

    public EleFXTransfer() {
        initialize();
    }

    @SafeVarargs
    public EleFXTransfer(EleFXTransferItem<T>... items) {
        this();
        getItems().addAll(items);
    }

    public ObservableList<EleFXTransferItem<T>> getItems() {
        return items;
    }

    public ObservableList<T> getTargetValues() {
        return targetValues;
    }

    public void setTargetValues(Collection<? extends T> values) {
        targetValues.setAll(values == null ? List.of() : values);
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

    public String getFilterPlaceholder() {
        return filterPlaceholder.get();
    }

    public StringProperty filterPlaceholderProperty() {
        return filterPlaceholder;
    }

    public void setFilterPlaceholder(String value) {
        filterPlaceholder.set(value == null ? "" : value);
    }

    public String getSourceTitle() {
        return sourceTitle.get();
    }

    public StringProperty sourceTitleProperty() {
        return sourceTitle;
    }

    public void setSourceTitle(String value) {
        sourceTitle.set(value == null ? "" : value);
    }

    public String getTargetTitle() {
        return targetTitle.get();
    }

    public StringProperty targetTitleProperty() {
        return targetTitle;
    }

    public void setTargetTitle(String value) {
        targetTitle.set(value == null ? "" : value);
    }

    public String getToLeftText() {
        return toLeftText.get();
    }

    public StringProperty toLeftTextProperty() {
        return toLeftText;
    }

    public void setToLeftText(String value) {
        toLeftText.set(value == null ? "" : value);
    }

    public String getToRightText() {
        return toRightText.get();
    }

    public StringProperty toRightTextProperty() {
        return toRightText;
    }

    public void setToRightText(String value) {
        toRightText.set(value == null ? "" : value);
    }

    /** Icon shown on the button that returns checked items to the source panel. */
    public Node getToLeftIcon() {
        return toLeftIcon.get();
    }

    public ObjectProperty<Node> toLeftIconProperty() {
        return toLeftIcon;
    }

    public void setToLeftIcon(Node value) {
        toLeftIcon.set(value);
    }

    /** Icon shown on the button that moves checked items to the target panel. */
    public Node getToRightIcon() {
        return toRightIcon.get();
    }

    public ObjectProperty<Node> toRightIconProperty() {
        return toRightIcon;
    }

    public void setToRightIcon(Node value) {
        toRightIcon.set(value);
    }

    public EleFXTransferTargetOrder getTargetOrder() {
        return targetOrder.get();
    }

    public ObjectProperty<EleFXTransferTargetOrder> targetOrderProperty() {
        return targetOrder;
    }

    public void setTargetOrder(EleFXTransferTargetOrder value) {
        targetOrder.set(value == null ? EleFXTransferTargetOrder.ORIGINAL : value);
    }

    public BiPredicate<String, EleFXTransferItem<T>> getFilterMethod() {
        return filterMethod.get();
    }

    public ObjectProperty<BiPredicate<String, EleFXTransferItem<T>>> filterMethodProperty() {
        return filterMethod;
    }

    public void setFilterMethod(BiPredicate<String, EleFXTransferItem<T>> value) {
        filterMethod.set(value);
    }

    public Function<EleFXTransferItem<T>, String> getItemRenderer() {
        return itemRenderer.get();
    }

    public ObjectProperty<Function<EleFXTransferItem<T>, String>> itemRendererProperty() {
        return itemRenderer;
    }

    public void setItemRenderer(Function<EleFXTransferItem<T>, String> value) {
        itemRenderer.set(value == null ? EleFXTransferItem::getLabel : value);
    }

    public EventHandler<EleFXTransferEvent<T>> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<EleFXTransferEvent<T>>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<EleFXTransferEvent<T>> value) {
        onChange.set(value);
    }

    public void clearQuery() {
        sourcePanel.query.clear();
        targetPanel.query.clear();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TRANSFER;
    }

    private void initialize() {
        getStyleClass().add("ele-transfer");
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setMaxWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(sourcePanel, operations(), targetPanel);
        items.addListener((ListChangeListener<EleFXTransferItem<T>>) c -> {
            normalizeTargetValues();
            rebuild();
        });
        targetValues.addListener((ListChangeListener<T>) c -> {
            if (!synchronizing) {
                normalizeTargetValues();
                rebuild();
            }
        });
        filterable.addListener((o, old, value) -> rebuild());
        filterPlaceholder.addListener((o, old, value) -> rebuild());
        filterMethod.addListener((o, old, value) -> rebuild());
        itemRenderer.addListener((o, old, value) -> rebuild());
        sourceTitle.addListener((o, old, value) -> rebuild());
        targetTitle.addListener((o, old, value) -> rebuild());
        toLeftText.addListener((o, old, value) -> rebuild());
        toRightText.addListener((o, old, value) -> rebuild());
        toLeftIcon.addListener((o, old, value) -> leftButton.setIcon(value));
        toRightIcon.addListener((o, old, value) -> rightButton.setIcon(value));
        sceneBuilderIntegration();
        rebuild();
    }

    private VBox operations() {
        VBox box = new VBox(10);
        box.getStyleClass().add("ele-transfer__buttons");
        box.setAlignment(Pos.CENTER);
        leftButton.setType(EleFXButtonType.PRIMARY);
        leftButton.getStyleClass().add("ele-transfer__button-left");
        rightButton.setType(EleFXButtonType.PRIMARY);
        rightButton.getStyleClass().add("ele-transfer__button-right");
        leftButton.textProperty().bind(toLeftText);
        rightButton.textProperty().bind(toRightText);
        leftButton.setIcon(getToLeftIcon());
        rightButton.setIcon(getToRightIcon());
        rightButton.setContentDisplay(ContentDisplay.RIGHT);
        leftButton.setOnAction(e -> transfer(targetPanel, sourcePanel, EleFXTransferDirection.LEFT));
        rightButton.setOnAction(e -> transfer(sourcePanel, targetPanel, EleFXTransferDirection.RIGHT));
        box.getChildren().addAll(leftButton, rightButton);
        return box;
    }

    private void transfer(Panel from, Panel to, EleFXTransferDirection direction) {
        List<EleFXTransferItem<T>> moved = from.checked.stream().filter(item -> !item.isDisabled()).toList();
        if (moved.isEmpty()) return;
        synchronizing = true;
        try {
            if (direction == EleFXTransferDirection.RIGHT) {
                List<T> values = moved.stream().map(EleFXTransferItem::getValue).toList();
                if (getTargetOrder() == EleFXTransferTargetOrder.UNSHIFT)
                    targetValues.addAll(0, values);
                else {
                    targetValues.addAll(values);
                    if (getTargetOrder() == EleFXTransferTargetOrder.ORIGINAL) normalizeTargetValues();
                }
            } else
                targetValues.removeAll(moved.stream().map(EleFXTransferItem::getValue).toList());
        } finally {
            synchronizing = false;
        }
        from.checked.clear();
        rebuild();
        EleFXTransferEvent<T> event = new EleFXTransferEvent<>(this, direction,
                moved.stream().map(EleFXTransferItem::getValue).toList());
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }

    private void normalizeTargetValues() {
        if (synchronizing) return;
        synchronizing = true;
        try {
            targetValues.removeIf(value -> items.stream().noneMatch(item -> Objects.equals(item.getValue(), value)));
            if (getTargetOrder() == EleFXTransferTargetOrder.ORIGINAL)
                targetValues.sort(Comparator.comparingInt(this::itemIndex));
        } finally {
            synchronizing = false;
        }
    }

    private int itemIndex(T value) {
        for (int i = 0; i < items.size(); i++)
            if (Objects.equals(items.get(i).getValue(), value)) return i;
        return Integer.MAX_VALUE;
    }

    private void rebuild() {
        sourcePanel.rebuild();
        targetPanel.rebuild();
    }

    private final class Panel extends VBox {

        final boolean target;

        final EleFXCheckbox<Void> all = new EleFXCheckbox<>();

        final Label title = new Label();

        final Region headerSpacer = new Region();

        final Label count = new Label();

        final TextField query = new TextField();

        final StackPane filterWrapper = new StackPane();

        final EleFXIcon searchIcon = new EleFXIcon(EleFXIconType.SEARCH, 14);

        final ListView<EleFXTransferItem<T>> list = new ListView<>();

        final ObservableList<EleFXTransferItem<T>> checked = FXCollections.observableArrayList();

        Panel(boolean target) {
            this.target = target;
            getStyleClass().add("ele-transfer-panel");
            getStyleClass().add(target ? "ele-transfer-panel--target" : "ele-transfer-panel--source");
            setPrefWidth(280);
            setPrefHeight(320);
            HBox header = new HBox(8, all, title, headerSpacer, count);
            header.getStyleClass().add("ele-transfer-panel__header");
            header.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);
            count.getStyleClass().add("ele-transfer-panel__count");
            all.setOnAction(e -> toggleAll());
            query.getStyleClass().add("ele-transfer-panel__filter");
            query.textProperty().addListener((o, old, value) -> rebuild());
            filterWrapper.getStyleClass().add("ele-transfer-panel__filter-wrapper");
            searchIcon.getStyleClass().add("ele-transfer-panel__filter-icon");
            filterWrapper.getChildren().addAll(query, searchIcon);
            StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
            StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 11));
            VBox.setMargin(filterWrapper, new Insets(12, 12, 0, 12));
            list.getStyleClass().add("ele-transfer-panel__list");
            list.setCellFactory(view -> new TransferCell());
            VBox.setVgrow(list, Priority.ALWAYS);
            getChildren().addAll(header, filterWrapper, list);
        }

        void rebuild() {
            filterWrapper.setManaged(isFilterable());
            filterWrapper.setVisible(isFilterable());
            query.setPromptText(getFilterPlaceholder());
            title.setText(target ? getTargetTitle() : getSourceTitle());
            count.setText(checked.size() + "/" + visible().size());
            list.getItems().setAll(visible());
            boolean selectable = list.getItems().stream().anyMatch(item -> !item.isDisabled());
            all.setDisable(!selectable);
            all.setSelected(selectable
                    && list.getItems().stream().filter(item -> !item.isDisabled()).allMatch(checked::contains));
        }

        List<EleFXTransferItem<T>> visible() {
            List<EleFXTransferItem<T>> base = target
                    ? targetValues.stream()
                            .map(value -> items.stream().filter(item -> Objects.equals(item.getValue(), value))
                                    .findFirst().orElse(null))
                            .filter(Objects::nonNull).toList()
                    : items.stream().filter(item -> !targetValues.contains(item.getValue())).toList();
            String text = query.getText() == null ? "" : query.getText().trim();
            if (text.isEmpty()) return base;
            return base.stream()
                    .filter(item -> getFilterMethod() == null
                            ? item.getLabel().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))
                            : getFilterMethod().test(text, item))
                    .toList();
        }

        void toggleAll() {
            boolean select = all.isSelected();
            for (EleFXTransferItem<T> item : list.getItems())
                if (!item.isDisabled()) {
                    if (select && !checked.contains(item))
                        checked.add(item);
                    else if (!select) checked.remove(item);
                }
            rebuild();
        }

        final class TransferCell extends ListCell<EleFXTransferItem<T>> {

            final EleFXCheckbox<EleFXTransferItem<T>> check = new EleFXCheckbox<>();
            {
                check.getStyleClass().add("ele-transfer-panel__item");
                check.setOnAction(e -> {
                    EleFXTransferItem<T> item = getItem();
                    if (item != null) {
                        if (check.isSelected())
                            checked.add(item);
                        else
                            checked.remove(item);
                        rebuild();
                    }
                });
            }

            @Override
            protected void updateItem(EleFXTransferItem<T> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                check.setValue(item);
                check.setText(getItemRenderer().apply(item));
                check.setSelected(checked.contains(item));
                check.setDisable(item.isDisabled());
                setGraphic(check);
            }
        }
    }
}
