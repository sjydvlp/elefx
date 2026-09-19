package com.sjydvlp.elefx.component.segmented;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
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
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A single-value selector styled after Element Plus Segmented.
 * Its {@link #valueProperty()} is the JavaFX equivalent of {@code v-model}.
 */
public class EleFXSegmented<T> extends StackPane implements Themable {

    private static final String STYLE_CLASS = "ele-segmented";

    private static final String ITEM_STYLE_CLASS = "ele-segmented__item";

    private static final String SELECTED_STYLE_CLASS = "ele-segmented__item--selected";

    private final ObservableList<EleFXSegmentedItem<T>> items = FXCollections.observableArrayList();

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<EleFXSegmentedSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXSegmentedSize.DEFAULT);

    private final ObjectProperty<EleFXSegmentedDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXSegmentedDirection.HORIZONTAL);

    private final BooleanProperty block = new SimpleBooleanProperty(this, "block", false);

    private final BooleanProperty validateEvent = new SimpleBooleanProperty(this, "validateEvent", true);

    private final StringProperty selectedBackgroundColor = new SimpleStringProperty(this, "selectedBackgroundColor",
            "#409EFF");

    private final StringProperty selectedColor = new SimpleStringProperty(this, "selectedColor", "#ffffff");

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty ariaLabel = new SimpleStringProperty(this, "ariaLabel", "Segmented");

    private final ObjectProperty<Callback<EleFXSegmentedItem<T>, Node>> itemRenderer = new SimpleObjectProperty<>(this,
            "itemRenderer");

    private final ObjectProperty<EventHandler<ActionEvent>> onChange = new SimpleObjectProperty<>(this, "onChange");

    private final Map<EleFXSegmentedItem<T>, Button> buttons = new IdentityHashMap<>();

    private final HBox horizontalItems = new HBox();

    private final VBox verticalItems = new VBox();

    private Pane itemsContainer = horizontalItems;

    private boolean synchronizing;

    public EleFXSegmented() {
        initialize();
    }

    @SafeVarargs
    public EleFXSegmented(T... options) {
        this();
        for (T option : options)
            items.add(new EleFXSegmentedItem<>(option));
    }

    public ObservableList<EleFXSegmentedItem<T>> getItems() {
        return items;
    }

    public void setItems(Collection<? extends EleFXSegmentedItem<T>> items) {
        this.items.setAll(items == null ? java.util.List.of() : items);
    }

    public void setOptions(Collection<? extends T> options) {
        items.setAll(options == null ? java.util.List.of() : options.stream().map(EleFXSegmentedItem<T>::new).toList());
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getSelectedValue() {
        return getValue();
    }

    public void setSelectedValue(T value) {
        setValue(value);
    }

    public ObjectProperty<T> selectedValueProperty() {
        return value;
    }

    public EleFXSegmentedSize getSize() {
        return size.get();
    }

    public void setSize(EleFXSegmentedSize size) {
        this.size.set(size == null ? EleFXSegmentedSize.DEFAULT : size);
    }

    public ObjectProperty<EleFXSegmentedSize> sizeProperty() {
        return size;
    }

    public EleFXSegmentedDirection getDirection() {
        return direction.get();
    }

    public void setDirection(EleFXSegmentedDirection direction) {
        this.direction.set(direction == null ? EleFXSegmentedDirection.HORIZONTAL : direction);
    }

    public ObjectProperty<EleFXSegmentedDirection> directionProperty() {
        return direction;
    }

    public boolean isBlock() {
        return block.get();
    }

    public void setBlock(boolean block) {
        this.block.set(block);
    }

    public BooleanProperty blockProperty() {
        return block;
    }

    public boolean isValidateEvent() {
        return validateEvent.get();
    }

    public void setValidateEvent(boolean validateEvent) {
        this.validateEvent.set(validateEvent);
    }

    public BooleanProperty validateEventProperty() {
        return validateEvent;
    }

    /** Background of the selected segment, matching Element Plus's selected background CSS variable. */
    public String getSelectedBackgroundColor() {
        return selectedBackgroundColor.get();
    }

    public void setSelectedBackgroundColor(String color) {
        selectedBackgroundColor.set(color == null || color.isBlank() ? "#409EFF" : color);
    }

    public StringProperty selectedBackgroundColorProperty() {
        return selectedBackgroundColor;
    }

    /** Text colour of the selected segment, matching Element Plus's selected text CSS variable. */
    public String getSelectedColor() {
        return selectedColor.get();
    }

    public void setSelectedColor(String color) {
        selectedColor.set(color == null || color.isBlank() ? "#ffffff" : color);
    }

    public StringProperty selectedColorProperty() {
        return selectedColor;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name == null ? "" : name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getAriaLabel() {
        return ariaLabel.get();
    }

    public void setAriaLabel(String ariaLabel) {
        this.ariaLabel.set(ariaLabel == null ? "Segmented" : ariaLabel);
    }

    public StringProperty ariaLabelProperty() {
        return ariaLabel;
    }

    public Callback<EleFXSegmentedItem<T>, Node> getItemRenderer() {
        return itemRenderer.get();
    }

    public void setItemRenderer(Callback<EleFXSegmentedItem<T>, Node> renderer) {
        itemRenderer.set(renderer);
    }

    public ObjectProperty<Callback<EleFXSegmentedItem<T>, Node>> itemRendererProperty() {
        return itemRenderer;
    }

    public EventHandler<ActionEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<ActionEvent> handler) {
        onChange.set(handler);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onChangeProperty() {
        return onChange;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SEGMENTED;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        horizontalItems.getStyleClass().add("ele-segmented__group");
        verticalItems.getStyleClass().add("ele-segmented__group");
        String railStyle = "-fx-padding: 0 2px; -fx-background-color: #F5F7FA;"
                + " -fx-background-insets: 0; -fx-background-radius: 4px;";
        horizontalItems.setStyle(railStyle);
        verticalItems.setStyle(railStyle);
        verticalItems.setFillWidth(true);
        setAlignment(Pos.CENTER_LEFT);
        setAccessibleRole(AccessibleRole.RADIO_BUTTON);
        setFocusTraversable(false);
        items.addListener((ListChangeListener<EleFXSegmentedItem<T>>) change -> rebuild());
        value.addListener((o, oldValue, newValue) -> {
            updateSelection();
            if (!synchronizing && !Objects.equals(oldValue, newValue)) fireChange();
        });
        size.addListener((o, oldValue, newValue) -> updateClasses());
        direction.addListener((o, oldValue, newValue) -> rebuild());
        block.addListener((o, oldValue, newValue) -> updateBlock());
        widthProperty().addListener((o, oldValue, newValue) -> updateBlock());
        selectedBackgroundColor.addListener((o, oldValue, newValue) -> updateCustomColors());
        selectedColor.addListener((o, oldValue, newValue) -> updateCustomColors());
        itemRenderer.addListener((o, oldValue, newValue) -> rebuild());
        ariaLabel.addListener((o, oldValue, newValue) -> setAccessibleText(newValue));
        disableProperty().addListener((o, oldValue, newValue) -> updateDisabled());
        updateClasses();
        updateCustomColors();
        setAccessibleText(getAriaLabel());
        sceneBuilderIntegration();
    }

    private void rebuild() {
        updateItemsContainer();
        itemsContainer.getChildren().clear();
        buttons.clear();
        for (EleFXSegmentedItem<T> item : items) {
            Button button = new Button();
            button.getStyleClass().add(ITEM_STYLE_CLASS);
            button.setFocusTraversable(true);
            button.setAccessibleRole(AccessibleRole.RADIO_BUTTON);
            button.setOnAction(event -> select(item));
            button.setOnKeyPressed(event -> navigate(item, event));
            item.labelProperty().addListener((o, oldValue, newValue) -> render(button, item));
            item.valueProperty().addListener((o, oldValue, newValue) -> updateSelection());
            item.graphicProperty().addListener((o, oldValue, newValue) -> render(button, item));
            item.disabledProperty().addListener((o, oldValue, newValue) -> updateDisabled());
            render(button, item);
            buttons.put(item, button);
            itemsContainer.getChildren().add(button);
        }
        updateClasses();
        updateBlock();
        updateDisabled();
        updateSelection();
    }

    private void render(Button button, EleFXSegmentedItem<T> item) {
        if (getItemRenderer() == null) {
            button.setText(item.getLabel());
            button.setGraphic(item.getGraphic());
            button.setContentDisplay(ContentDisplay.LEFT);
        } else {
            button.setText("");
            button.setGraphic(getItemRenderer().call(item));
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    private void select(EleFXSegmentedItem<T> item) {
        if (!isDisable() && !item.isDisabled()) setValue(item.getValue());
    }

    private void updateSelection() {
        synchronizing = true;
        try {
            buttons.forEach((item, button) -> {
                boolean selected = Objects.equals(item.getValue(), getValue());
                if (selected && !button.getStyleClass().contains(SELECTED_STYLE_CLASS))
                    button.getStyleClass().add(SELECTED_STYLE_CLASS);
                if (!selected) button.getStyleClass().remove(SELECTED_STYLE_CLASS);
                button.setAccessibleText(item.getLabel() + (selected ? ", selected" : ""));
            });
        } finally {
            synchronizing = false;
        }
    }

    private void updateClasses() {
        getStyleClass().removeAll(EleFXSegmentedSize.LARGE.styleClass(), EleFXSegmentedSize.DEFAULT.styleClass(),
                EleFXSegmentedSize.SMALL.styleClass(), EleFXSegmentedDirection.HORIZONTAL.styleClass(),
                EleFXSegmentedDirection.VERTICAL.styleClass());
        getStyleClass().addAll(getSize().styleClass(), getDirection().styleClass());
        updateItemsContainer();
    }

    private void updateItemsContainer() {
        Pane expected = getDirection() == EleFXSegmentedDirection.HORIZONTAL ? horizontalItems : verticalItems;
        if (itemsContainer == expected && getChildren().contains(expected)) return;
        itemsContainer = expected;
        getChildren().setAll(itemsContainer);
    }

    private void updateBlock() {
        boolean horizontalBlock = isBlock() && getDirection() == EleFXSegmentedDirection.HORIZONTAL;
        boolean vertical = getDirection() == EleFXSegmentedDirection.VERTICAL;
        // Element Plus permits shrinking/ellipsis only for the explicit block
        // variant. The normal inline control keeps every option's intrinsic
        // width, rather than letting an HBox compress labels to "...".
        setMinWidth(horizontalBlock ? 0 : Region.USE_PREF_SIZE);
        setMaxWidth(horizontalBlock
                ? Double.MAX_VALUE
                : Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
        for (Button button : buttons.values()) {
            button.setPrefWidth(horizontalBlock ? 0 : Region.USE_COMPUTED_SIZE);
            button.setMinWidth(horizontalBlock ? 0 : Region.USE_PREF_SIZE);
            button.setMaxWidth(vertical || isBlock() ? Double.MAX_VALUE : Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(button, horizontalBlock
                    ? Priority.ALWAYS
                    : Priority.NEVER);
        }
        horizontalItems.setMinWidth(horizontalBlock ? 0 : Region.USE_PREF_SIZE);
        horizontalItems.setMaxWidth(horizontalBlock
                ? Double.MAX_VALUE
                : Region.USE_PREF_SIZE);
        requestLayout();
        if (getParent() != null) getParent().requestLayout();
    }

    private void updateCustomColors() {
        setStyle("-elefx-segmented-selected-background: " + getSelectedBackgroundColor()
                + "; -elefx-segmented-selected-color: " + getSelectedColor() + ";");
    }

    private void updateDisabled() {
        buttons.forEach((item, button) -> button.setDisable(isDisable() || item.isDisabled()));
    }

    private void navigate(EleFXSegmentedItem<T> current, javafx.scene.input.KeyEvent event) {
        boolean backwards = event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.UP;
        boolean forwards = event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.DOWN;
        if (!backwards && !forwards) return;
        int index = items.indexOf(current), step = backwards ? -1 : 1;
        for (int offset = 1; offset < items.size(); offset++) {
            EleFXSegmentedItem<T> candidate = items.get(Math.floorMod(index + step * offset, items.size()));
            if (!candidate.isDisabled()) {
                buttons.get(candidate).requestFocus();
                select(candidate);
                event.consume();
                return;
            }
        }
    }

    private void fireChange() {
        ActionEvent event = new ActionEvent(this, this);
        fireEvent(event);
        if (getOnChange() != null) getOnChange().handle(event);
    }
}
