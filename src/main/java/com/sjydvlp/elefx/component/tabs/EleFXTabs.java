package com.sjydvlp.elefx.component.tabs;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Element Plus inspired tabs. {@link #valueProperty()} selects a pane by its name;
 * panes with no name receive their zero-based ordinal as their name.
 */
public class EleFXTabs extends BorderPane implements Themable {

    private final ObservableList<EleFXTabPane> panes = FXCollections.observableArrayList();

    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(this, "value");

    private final ObjectProperty<Object> defaultValue = new SimpleObjectProperty<>(this, "defaultValue");

    private final ObjectProperty<EleFXTabsType> type = new SimpleObjectProperty<>(this, "type", EleFXTabsType.DEFAULT);

    private final ObjectProperty<EleFXTabsPosition> tabPosition = new SimpleObjectProperty<>(this, "tabPosition",
            EleFXTabsPosition.TOP);

    private final BooleanProperty closable = new SimpleBooleanProperty(this, "closable", false);

    private final BooleanProperty addable = new SimpleBooleanProperty(this, "addable", false);

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", false);

    private final BooleanProperty stretch = new SimpleBooleanProperty(this, "stretch", false);

    private final ObjectProperty<BiPredicate<Object, Object>> beforeLeave = new SimpleObjectProperty<>(this,
            "beforeLeave", (oldName, newName) -> true);

    private final ObjectProperty<Callback<EleFXTabPane, Node>> labelRenderer = new SimpleObjectProperty<>(this,
            "labelRenderer");

    private final ObjectProperty<Node> addGraphic = new SimpleObjectProperty<>(this, "addGraphic");

    private final ObjectProperty<EventHandler<EleFXTabsEvent>> onTabClick = new SimpleObjectProperty<>(this,
            "onTabClick");

    private final ObjectProperty<EventHandler<EleFXTabsEvent>> onTabChange = new SimpleObjectProperty<>(this,
            "onTabChange");

    private final ObjectProperty<EventHandler<EleFXTabsEvent>> onTabAdd = new SimpleObjectProperty<>(this, "onTabAdd");

    private final ObjectProperty<EventHandler<EleFXTabsEvent>> onTabRemove = new SimpleObjectProperty<>(this,
            "onTabRemove");

    private final ObjectProperty<EventHandler<EleFXTabsEvent>> onEdit = new SimpleObjectProperty<>(this, "onEdit");

    private final HBox horizontalNav = new HBox() {

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            layoutActiveLine();
        }
    };

    private final StackPane horizontalHeader = new StackPane();

    private final VBox verticalNav = new VBox() {

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            layoutActiveLine();
        }
    };

    private final StackPane content = new StackPane();

    private final Region activeLine = new Region();

    private final Map<EleFXTabPane, Button> tabButtons = new IdentityHashMap<>();

    private final Map<EleFXTabPane, CloseAnimation> closeAnimations = new IdentityHashMap<>();

    private final Map<EleFXTabPane, StackPane> paneNodes = new IdentityHashMap<>();

    private boolean rebuilding;

    private boolean bypassBeforeLeave;

    private static final Duration CLOSE_TRANSITION_DURATION = Duration.millis(300);

    public EleFXTabs() {
        initialize();
    }

    public EleFXTabs(EleFXTabPane... tabs) {
        this();
        getPanes().addAll(tabs);
    }

    public ObservableList<EleFXTabPane> getPanes() {
        return panes;
    }

    public Object getValue() {
        return value.get();
    }

    public void setValue(Object value) {
        this.value.set(value);
    }

    public ObjectProperty<Object> valueProperty() {
        return value;
    }

    /** Alias for value, useful when reading the Element Plus API as a JavaFX property. */
    public Object getCurrentName() {
        return getValue();
    }

    public void setCurrentName(Object value) {
        setValue(value);
    }

    public ObjectProperty<Object> currentNameProperty() {
        return value;
    }

    public Object getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(Object value) {
        defaultValue.set(value);
    }

    public ObjectProperty<Object> defaultValueProperty() {
        return defaultValue;
    }

    public EleFXTabsType getType() {
        return type.get();
    }

    public void setType(EleFXTabsType value) {
        type.set(value == null ? EleFXTabsType.DEFAULT : value);
    }

    public ObjectProperty<EleFXTabsType> typeProperty() {
        return type;
    }

    public EleFXTabsPosition getTabPosition() {
        return tabPosition.get();
    }

    public void setTabPosition(EleFXTabsPosition value) {
        tabPosition.set(value == null ? EleFXTabsPosition.TOP : value);
    }

    public ObjectProperty<EleFXTabsPosition> tabPositionProperty() {
        return tabPosition;
    }

    public boolean isClosable() {
        return closable.get();
    }

    public void setClosable(boolean value) {
        closable.set(value);
    }

    public BooleanProperty closableProperty() {
        return closable;
    }

    public boolean isAddable() {
        return addable.get();
    }

    public void setAddable(boolean value) {
        addable.set(value);
    }

    public BooleanProperty addableProperty() {
        return addable;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public void setEditable(boolean value) {
        editable.set(value);
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isStretch() {
        return stretch.get();
    }

    public void setStretch(boolean value) {
        stretch.set(value);
    }

    public BooleanProperty stretchProperty() {
        return stretch;
    }

    public BiPredicate<Object, Object> getBeforeLeave() {
        return beforeLeave.get();
    }

    public void setBeforeLeave(BiPredicate<Object, Object> value) {
        beforeLeave.set(value == null ? (oldName, newName) -> true : value);
    }

    public ObjectProperty<BiPredicate<Object, Object>> beforeLeaveProperty() {
        return beforeLeave;
    }

    public Callback<EleFXTabPane, Node> getLabelRenderer() {
        return labelRenderer.get();
    }

    public void setLabelRenderer(Callback<EleFXTabPane, Node> value) {
        labelRenderer.set(value);
    }

    public ObjectProperty<Callback<EleFXTabPane, Node>> labelRendererProperty() {
        return labelRenderer;
    }

    public Node getAddGraphic() {
        return addGraphic.get();
    }

    public void setAddGraphic(Node value) {
        addGraphic.set(value);
    }

    public ObjectProperty<Node> addGraphicProperty() {
        return addGraphic;
    }

    public EventHandler<EleFXTabsEvent> getOnTabClick() {
        return onTabClick.get();
    }

    public void setOnTabClick(EventHandler<EleFXTabsEvent> value) {
        onTabClick.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTabsEvent>> onTabClickProperty() {
        return onTabClick;
    }

    public EventHandler<EleFXTabsEvent> getOnTabChange() {
        return onTabChange.get();
    }

    public void setOnTabChange(EventHandler<EleFXTabsEvent> value) {
        onTabChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTabsEvent>> onTabChangeProperty() {
        return onTabChange;
    }

    public EventHandler<EleFXTabsEvent> getOnTabAdd() {
        return onTabAdd.get();
    }

    public void setOnTabAdd(EventHandler<EleFXTabsEvent> value) {
        onTabAdd.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTabsEvent>> onTabAddProperty() {
        return onTabAdd;
    }

    public EventHandler<EleFXTabsEvent> getOnTabRemove() {
        return onTabRemove.get();
    }

    public void setOnTabRemove(EventHandler<EleFXTabsEvent> value) {
        onTabRemove.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTabsEvent>> onTabRemoveProperty() {
        return onTabRemove;
    }

    public EventHandler<EleFXTabsEvent> getOnEdit() {
        return onEdit.get();
    }

    public void setOnEdit(EventHandler<EleFXTabsEvent> value) {
        onEdit.set(value);
    }

    public ObjectProperty<EventHandler<EleFXTabsEvent>> onEditProperty() {
        return onEdit;
    }

    /** Requests a new tab. Applications add the pane and select it in the edit handler. */
    public void addTab() {
        if (canAdd()) {
            emit(EleFXTabsEvent.TAB_ADD, null, null, "add", null);
            emit(EleFXTabsEvent.EDIT, null, null, "add", null);
        }
    }

    /** Requests removal of a closable pane. Applications update the selection and pane list in the edit handler. */
    public void removeTab(EleFXTabPane pane) {
        if (pane != null && panes.contains(pane) && canClose(pane)) remove(pane);
    }

    /** Brings the currently selected button into view/focus in the JavaFX navigation chain. */
    public void scrollToActiveTab() {
        EleFXTabPane pane = selectedPane();
        if (pane != null) tabButtons.get(pane).requestFocus();
    }

    public void removeFocus() {
        if (getScene() != null) getScene().getRoot().requestFocus();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TABS;
    }

    private void initialize() {
        getStyleClass().add("ele-tabs");
        horizontalNav.getStyleClass().add("ele-tabs__nav");
        horizontalNav.setAlignment(Pos.CENTER_LEFT);
        horizontalHeader.getChildren().add(horizontalNav);
        verticalNav.getStyleClass().add("ele-tabs__nav");
        activeLine.getStyleClass().add("ele-tabs__active-line");
        activeLine.setManaged(false);
        activeLine.setMouseTransparent(true);
        content.getStyleClass().add("ele-tabs__content");
        panes.addListener((ListChangeListener<EleFXTabPane>) c -> rebuild());
        value.addListener((o, oldValue, newValue) -> selectChanged(oldValue, newValue));
        defaultValue.addListener((o, oldValue, newValue) -> {
            if (getValue() == null) setValue(newValue);
        });
        type.addListener(o -> {
            updateClasses();
            horizontalNav.requestLayout();
            verticalNav.requestLayout();
        });
        tabPosition.addListener(o -> rebuild());
        closable.addListener(o -> rebuild());
        addable.addListener(o -> rebuild());
        editable.addListener(o -> rebuild());
        stretch.addListener(o -> updateStretch());
        labelRenderer.addListener(o -> rebuild());
        addGraphic.addListener(o -> rebuild());
        setFocusTraversable(false);
        updateClasses();
        sceneBuilderIntegration();
    }

    private void rebuild() {
        if (rebuilding) return;
        rebuilding = true;
        try {
            tabButtons.clear();
            closeAnimations.values().forEach(CloseAnimation::stop);
            closeAnimations.clear();
            paneNodes.clear();
            horizontalNav.getChildren().clear();
            horizontalHeader.getChildren().removeIf(node -> node != horizontalNav);
            horizontalNav.setPadding(Insets.EMPTY);
            verticalNav.getChildren().clear();
            content.getChildren().clear();
            for (int i = 0; i < panes.size(); i++) {
                EleFXTabPane pane = panes.get(i);
                if (pane.getName() == null) pane.setName(i);
                install(pane);
                Button button = createButton(pane);
                if (i == 0) button.getStyleClass().add("ele-tabs__item--first");
                if (i == panes.size() - 1) button.getStyleClass().add("ele-tabs__item--last");
                tabButtons.put(pane, button);
                nav().getChildren().add(button);
                StackPane holder = new StackPane();
                holder.getStyleClass().add("ele-tab-pane");
                paneNodes.put(pane, holder);
                content.getChildren().add(holder);
            }
            if (canAdd()) {
                Button addButton = createAddButton();
                if (nav() == horizontalNav) {
                    horizontalNav.setPadding(new Insets(0, 30, 0, 0));
                    StackPane.setAlignment(addButton, Pos.CENTER_RIGHT);
                    StackPane.setMargin(addButton, new Insets(10, 0, 10, 0));
                    horizontalHeader.getChildren().add(addButton);
                } else {
                    VBox.setMargin(addButton, new Insets(10, 0, 0, 0));
                    verticalNav.getChildren().add(addButton);
                }
            }
            nav().getChildren().add(activeLine);
            if (panes.isEmpty()) setValue(null);
            if (getValue() == null && !panes.isEmpty())
                setValue(getDefaultValue() != null ? getDefaultValue() : panes.get(0).getName());
            if (selectedPane() == null && !panes.isEmpty()) setValue(panes.get(0).getName());
            place();
            updateClasses();
            updateStretch();
            refreshSelection();
        } finally {
            rebuilding = false;
        }
    }

    private void install(EleFXTabPane pane) {
        pane.labelProperty().addListener(o -> rebuild());
        pane.nameProperty().addListener(o -> rebuild());
        pane.contentProperty().addListener(o -> refreshSelection());
        pane.labelNodeProperty().addListener(o -> rebuild());
        pane.disabledProperty().addListener(o -> refreshSelection());
        pane.closableProperty().addListener(o -> rebuild());
        pane.lazyProperty().addListener(o -> refreshSelection());
    }

    private Button createButton(EleFXTabPane pane) {
        Button button = new Button();
        button.getStyleClass().add("ele-tabs__item");
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setFocusTraversable(true);
        renderLabel(button, pane);
        button.hoverProperty().addListener(o -> updateCloseButton(pane, button));
        button.fontProperty().addListener(o -> horizontalNav.requestLayout());
        if (button.getGraphic() != null)
            button.getGraphic().layoutBoundsProperty().addListener(o -> horizontalNav.requestLayout());
        button.setDisable(isDisable() || pane.isDisabled());
        button.setOnAction(e -> activate(pane, null));
        button.setOnKeyPressed(e -> navigate(pane, e));
        return button;
    }

    private void renderLabel(Button button, EleFXTabPane pane) {
        Node visual = getLabelRenderer() == null ? pane.getLabelNode() : getLabelRenderer().call(pane);
        if (canClose(pane)) {
            Button close = new Button();
            close.getStyleClass().add("ele-tabs__close");
            close.setGraphic(new EleFXIcon(EleFXIconType.CLOSE, 12));
            close.setFocusTraversable(false);
            close.setOnAction(e -> {
                e.consume();
                remove(pane);
            });
            Node label = visual;
            if (label == null) {
                javafx.scene.control.Label textLabel = new javafx.scene.control.Label(pane.getLabel());
                textLabel.getStyleClass().add("ele-tabs__label");
                label = textLabel;
            }
            StackPane closeSlot = new StackPane(close);
            closeSlot.setAlignment(Pos.CENTER_LEFT);
            StackPane.setMargin(close, new Insets(0, 0, 0, 5));
            closeSlot.setMinWidth(0);
            closeSlot.setPrefWidth(0);
            closeSlot.setMaxWidth(Region.USE_PREF_SIZE);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(closeSlot.widthProperty());
            clip.heightProperty().bind(closeSlot.heightProperty());
            closeSlot.setClip(clip);
            close.setOpacity(0);
            HBox box = new HBox(label, closeSlot);
            box.setAlignment(Pos.CENTER);
            closeAnimations.put(pane, new CloseAnimation(button, closeSlot, close));
            button.getStyleClass().add("ele-tabs__item--closable");
            updateCloseButton(pane, button);
            button.setText("");
            button.setGraphic(box);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            button.setText(visual == null ? pane.getLabel() : "");
            button.setGraphic(visual);
        }
    }

    private Button createAddButton() {
        Button b = new Button();
        b.getStyleClass().add("ele-tabs__new-tab");
        b.setGraphic(getAddGraphic() == null ? new EleFXIcon(EleFXIconType.PLUS, 12) : getAddGraphic());
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        b.setOnAction(e -> addTab());
        return b;
    }

    private void place() {
        setTop(null);
        setBottom(null);
        setLeft(null);
        setRight(null);
        setCenter(content);
        switch (getTabPosition()) {
            case TOP -> setTop(horizontalHeader);
            case BOTTOM -> setBottom(horizontalHeader);
            case LEFT -> setLeft(nav());
            case RIGHT -> setRight(nav());
        }
    }

    private Pane nav() {
        return getTabPosition() == EleFXTabsPosition.LEFT || getTabPosition() == EleFXTabsPosition.RIGHT
                ? verticalNav
                : horizontalNav;
    }

    private void activate(EleFXTabPane pane, javafx.scene.input.InputEvent event) {
        if (pane.isDisabled() || Objects.equals(getValue(), pane.getName())) return;
        if (!getBeforeLeave().test(getValue(), pane.getName())) return;
        Object old = getValue();
        bypassBeforeLeave = true;
        try {
            setValue(pane.getName());
        } finally {
            bypassBeforeLeave = false;
        }
        emit(EleFXTabsEvent.TAB_CLICK, pane, old, null, event);
    }

    private void selectChanged(Object oldName, Object newName) {
        if (rebuilding || Objects.equals(oldName, newName)) return;
        EleFXTabPane selected = selectedPane();
        if (selected == null || selected.isDisabled()
                || (!bypassBeforeLeave && !getBeforeLeave().test(oldName, newName))) {
            if (!Objects.equals(oldName, newName)) value.set(oldName);
            return;
        }
        refreshSelection();
        emit(EleFXTabsEvent.TAB_CHANGE, selected, oldName, null, null);
    }

    private void refreshSelection() {
        for (EleFXTabPane pane : panes) {
            boolean active = Objects.equals(pane.getName(), getValue());
            Button button = tabButtons.get(pane);
            StackPane holder = paneNodes.get(pane);
            if (button != null) {
                toggle(button, "ele-tabs__item--active", active);
                button.setDisable(isDisable() || pane.isDisabled());
                updateCloseButton(pane, button);
            }
            if (holder != null) {
                holder.setVisible(active);
                holder.setManaged(active);
                if (active || !pane.isLazy())
                    holder.getChildren().setAll(pane.getContent() == null ? new Region() : pane.getContent());
            }
        }
        horizontalNav.requestLayout();
        verticalNav.requestLayout();
    }

    private void updateCloseButton(EleFXTabPane pane, Button tabButton) {
        CloseAnimation animation = closeAnimations.get(pane);
        if (animation == null) return;
        boolean active = Objects.equals(pane.getName(), getValue());
        animation.update(active || tabButton.isHover(), active, tabButton.getScene() != null);
    }

    private static final class CloseAnimation {

        private final Button tabButton;

        private final StackPane closeSlot;

        private final Button closeButton;

        private final DoubleProperty horizontalPadding = new SimpleDoubleProperty(20);

        private Timeline transition;

        private double targetWidth = Double.NaN;

        private double targetPadding = Double.NaN;

        private CloseAnimation(Button tabButton, StackPane closeSlot, Button closeButton) {
            this.tabButton = tabButton;
            this.closeSlot = closeSlot;
            this.closeButton = closeButton;
            horizontalPadding.addListener((o, oldValue, value) -> tabButton
                    .setPadding(new Insets(0, value.doubleValue(), 0, value.doubleValue())));
        }

        private void update(boolean show, boolean active, boolean animate) {
            double width = show ? 19 : 0;
            double padding = show && !active ? 13 : 20;
            if (width == targetWidth && padding == targetPadding) return;
            targetWidth = width;
            targetPadding = padding;
            stop();
            if (!animate) {
                closeSlot.setPrefWidth(width);
                closeButton.setOpacity(show ? 1 : 0);
                horizontalPadding.set(padding);
                return;
            }
            transition = new Timeline(new KeyFrame(CLOSE_TRANSITION_DURATION,
                    new KeyValue(closeSlot.prefWidthProperty(), width, Interpolator.EASE_BOTH),
                    new KeyValue(closeButton.opacityProperty(), show ? 1 : 0, Interpolator.EASE_BOTH),
                    new KeyValue(horizontalPadding, padding, Interpolator.EASE_BOTH)));
            transition.play();
        }

        private void stop() {
            if (transition != null) transition.stop();
        }
    }

    private void remove(EleFXTabPane pane) {
        if (!panes.contains(pane)) return;
        Object activeName = getValue();
        emit(EleFXTabsEvent.TAB_REMOVE, pane, activeName, "remove", null);
        emit(EleFXTabsEvent.EDIT, pane, activeName, "remove", null);
    }

    private void navigate(EleFXTabPane current, KeyEvent event) {
        boolean prior = event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.UP;
        boolean next = event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.DOWN;
        if (!prior && !next) return;
        int start = panes.indexOf(current), step = prior ? -1 : 1;
        for (int offset = 1; offset < panes.size(); offset++) {
            EleFXTabPane candidate = panes.get(Math.floorMod(start + step * offset, panes.size()));
            if (!candidate.isDisabled()) {
                tabButtons.get(candidate).requestFocus();
                activate(candidate, event);
                event.consume();
                return;
            }
        }
    }

    private void updateClasses() {
        getStyleClass().removeAll(EleFXTabsType.DEFAULT.styleClass(), EleFXTabsType.CARD.styleClass(),
                EleFXTabsType.BORDER_CARD.styleClass(), EleFXTabsPosition.TOP.styleClass(),
                EleFXTabsPosition.RIGHT.styleClass(), EleFXTabsPosition.BOTTOM.styleClass(),
                EleFXTabsPosition.LEFT.styleClass());
        getStyleClass().addAll(getType().styleClass(), getTabPosition().styleClass());
    }

    private void updateStretch() {
        for (Button b : tabButtons.values()) {
            HBox.setHgrow(b, isStretch() && nav() == horizontalNav ? Priority.ALWAYS : Priority.NEVER);
            b.setMaxWidth(nav() == verticalNav || isStretch() ? Double.MAX_VALUE : Region.USE_COMPUTED_SIZE);
        }
    }

    private void layoutActiveLine() {
        if (getType() != EleFXTabsType.DEFAULT && getType() != EleFXTabsType.CARD
                && getType() != EleFXTabsType.BORDER_CARD) {
            activeLine.setVisible(false);
            return;
        }
        Button button = tabButtons.get(selectedPane());
        if (button == null || button.getWidth() <= 0) {
            activeLine.setVisible(false);
            return;
        }
        if (getType() == EleFXTabsType.DEFAULT && nav() == verticalNav) {
            double x = getTabPosition() == EleFXTabsPosition.RIGHT ? 0 : verticalNav.getWidth() - 2;
            activeLine.resizeRelocate(x, button.getLayoutY(), 2, button.getHeight());
            activeLine.setVisible(true);
            return;
        }
        if (getType() == EleFXTabsType.CARD || getType() == EleFXTabsType.BORDER_CARD) {
            if (nav() == horizontalNav) {
                double y = getTabPosition() == EleFXTabsPosition.BOTTOM ? 0 : horizontalNav.getHeight() - 1;
                activeLine.resizeRelocate(button.getLayoutX(), y, button.getWidth(), 1);
            } else {
                double x = getTabPosition() == EleFXTabsPosition.RIGHT ? 0 : verticalNav.getWidth() - 1;
                activeLine.resizeRelocate(x, button.getLayoutY(), 1, button.getHeight());
            }
            activeLine.setVisible(true);
            return;
        }
        double labelWidth = 0;
        if (button.getText() != null && !button.getText().isEmpty()) {
            Text text = new Text(button.getText());
            text.setFont(button.getFont());
            labelWidth = text.getLayoutBounds().getWidth();
        }
        if (button.getGraphic() != null) {
            double graphicWidth = button.getGraphic().getLayoutBounds().getWidth();
            labelWidth += graphicWidth + (labelWidth > 0 ? button.getGraphicTextGap() : 0);
        }
        labelWidth = Math.min(labelWidth, button.getWidth());
        double leftPadding = button.getPadding().getLeft();
        double rightPadding = button.getPadding().getRight();
        double contentWidth = Math.max(0, button.getWidth() - leftPadding - rightPadding);
        double x = button.getLayoutX() + leftPadding + Math.max(0, (contentWidth - labelWidth) / 2);
        double y = getTabPosition() == EleFXTabsPosition.BOTTOM
                ? button.getLayoutY()
                : button.getLayoutY() + button.getHeight() - 2;
        activeLine.resizeRelocate(x, y, labelWidth, 2);
        activeLine.setVisible(true);
    }

    private EleFXTabPane selectedPane() {
        return panes.stream().filter(p -> Objects.equals(p.getName(), getValue())).findFirst().orElse(null);
    }

    private boolean canAdd() {
        return getType() == EleFXTabsType.CARD && (isAddable() || isEditable());
    }

    private boolean canClose(EleFXTabPane pane) {
        return getType() == EleFXTabsType.CARD && (isClosable() || isEditable() || pane.isClosable());
    }

    private static void toggle(Node node, String style, boolean enabled) {
        if (enabled && !node.getStyleClass().contains(style)) node.getStyleClass().add(style);
        if (!enabled) node.getStyleClass().remove(style);
    }

    private void emit(javafx.event.EventType<EleFXTabsEvent> type, EleFXTabPane pane, Object oldName, String action,
                      javafx.scene.input.InputEvent input) {
        EleFXTabsEvent event = new EleFXTabsEvent(this, this, type, pane, oldName, action, input);
        fireEvent(event);
        EventHandler<EleFXTabsEvent> handler = type == EleFXTabsEvent.TAB_CLICK
                ? getOnTabClick()
                : type == EleFXTabsEvent.TAB_CHANGE
                        ? getOnTabChange()
                        : type == EleFXTabsEvent.TAB_ADD
                                ? getOnTabAdd()
                                : type == EleFXTabsEvent.TAB_REMOVE ? getOnTabRemove() : getOnEdit();
        if (handler != null) handler.handle(event);
    }
}
