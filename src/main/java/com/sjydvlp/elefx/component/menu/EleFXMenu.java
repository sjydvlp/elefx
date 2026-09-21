package com.sjydvlp.elefx.component.menu;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Element Plus inspired navigation menu.
 * Supports vertical and horizontal layouts, nested submenus, collapsed vertical navigation,
 * default/open active indexes, unique opening, configurable popup timing, and JavaFX events.
 */
public class EleFXMenu extends VBox implements Themable {

    private final ObservableList<EleFXMenuEntry> items = FXCollections.observableArrayList();

    private final ObjectProperty<EleFXMenuMode> mode = new SimpleObjectProperty<>(this, "mode", EleFXMenuMode.VERTICAL);

    private final BooleanProperty collapse = new SimpleBooleanProperty(this, "collapse", false);

    private final BooleanProperty ellipsis = new SimpleBooleanProperty(this, "ellipsis", true);

    private final BooleanProperty uniqueOpened = new SimpleBooleanProperty(this, "uniqueOpened", false);

    private final BooleanProperty menuTriggerClick = new SimpleBooleanProperty(this, "menuTriggerClick", false);

    private final BooleanProperty closeOnClickOutside = new SimpleBooleanProperty(this, "closeOnClickOutside", false);

    private final StringProperty activeIndex = new SimpleStringProperty(this, "activeIndex", "");

    private final StringProperty backgroundColor = new SimpleStringProperty(this, "backgroundColor", "");

    private final StringProperty textColor = new SimpleStringProperty(this, "textColor", "");

    private final StringProperty activeTextColor = new SimpleStringProperty(this, "activeTextColor", "");

    private final StringProperty popperClass = new SimpleStringProperty(this, "popperClass", "");

    private final DoubleProperty popperOffset = new SimpleDoubleProperty(this, "popperOffset", 6);

    private final ObjectProperty<Duration> showTimeout = new SimpleObjectProperty<>(this, "showTimeout",
            Duration.millis(300));

    private final ObjectProperty<Duration> hideTimeout = new SimpleObjectProperty<>(this, "hideTimeout",
            Duration.millis(300));

    private final ObservableList<String> defaultOpeneds = FXCollections.observableArrayList();

    private final FlowPane horizontalContent = new FlowPane(Orientation.HORIZONTAL);

    private final ObjectProperty<EventHandler<EleFXMenuEvent>> onSelect = new SimpleObjectProperty<>(this, "onSelect");

    private final ObjectProperty<EventHandler<EleFXMenuEvent>> onOpen = new SimpleObjectProperty<>(this, "onOpen");

    private final ObjectProperty<EventHandler<EleFXMenuEvent>> onClose = new SimpleObjectProperty<>(this, "onClose");

    public EleFXMenu() {
        initialize();
    }

    public EleFXMenu(EleFXMenuEntry... entries) {
        this();
        getItems().addAll(entries);
    }

    public ObservableList<EleFXMenuEntry> getItems() {
        return items;
    }

    public EleFXMenuMode getMode() {
        return mode.get();
    }

    public void setMode(EleFXMenuMode value) {
        mode.set(value == null ? EleFXMenuMode.VERTICAL : value);
    }

    public ObjectProperty<EleFXMenuMode> modeProperty() {
        return mode;
    }

    public boolean isCollapse() {
        return collapse.get();
    }

    public void setCollapse(boolean value) {
        collapse.set(value);
    }

    public BooleanProperty collapseProperty() {
        return collapse;
    }

    public boolean isEllipsis() {
        return ellipsis.get();
    }

    public void setEllipsis(boolean value) {
        ellipsis.set(value);
    }

    public BooleanProperty ellipsisProperty() {
        return ellipsis;
    }

    public boolean isUniqueOpened() {
        return uniqueOpened.get();
    }

    public void setUniqueOpened(boolean value) {
        uniqueOpened.set(value);
    }

    public BooleanProperty uniqueOpenedProperty() {
        return uniqueOpened;
    }

    /** Whether horizontal submenus use click rather than Element Plus's default hover trigger. */
    public boolean isMenuTriggerClick() {
        return menuTriggerClick.get();
    }

    public void setMenuTriggerClick(boolean value) {
        menuTriggerClick.set(value);
    }

    public BooleanProperty menuTriggerClickProperty() {
        return menuTriggerClick;
    }

    public boolean isCloseOnClickOutside() {
        return closeOnClickOutside.get();
    }

    public void setCloseOnClickOutside(boolean value) {
        closeOnClickOutside.set(value);
    }

    public BooleanProperty closeOnClickOutsideProperty() {
        return closeOnClickOutside;
    }

    public String getActiveIndex() {
        return activeIndex.get();
    }

    public void setActiveIndex(String value) {
        activeIndex.set(value == null ? "" : value);
    }

    public StringProperty activeIndexProperty() {
        return activeIndex;
    }

    public ObservableList<String> getDefaultOpeneds() {
        return defaultOpeneds;
    }

    public double getPopperOffset() {
        return popperOffset.get();
    }

    public void setPopperOffset(double value) {
        popperOffset.set(value);
    }

    public DoubleProperty popperOffsetProperty() {
        return popperOffset;
    }

    public Duration getShowTimeout() {
        return showTimeout.get();
    }

    public void setShowTimeout(Duration value) {
        showTimeout.set(validDuration(value));
    }

    public ObjectProperty<Duration> showTimeoutProperty() {
        return showTimeout;
    }

    public Duration getHideTimeout() {
        return hideTimeout.get();
    }

    public void setHideTimeout(Duration value) {
        hideTimeout.set(validDuration(value));
    }

    public ObjectProperty<Duration> hideTimeoutProperty() {
        return hideTimeout;
    }

    public String getBackgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(String value) {
        backgroundColor.set(value == null ? "" : value);
    }

    public StringProperty backgroundColorProperty() {
        return backgroundColor;
    }

    public String getTextColor() {
        return textColor.get();
    }

    public void setTextColor(String value) {
        textColor.set(value == null ? "" : value);
    }

    public StringProperty textColorProperty() {
        return textColor;
    }

    public String getActiveTextColor() {
        return activeTextColor.get();
    }

    public void setActiveTextColor(String value) {
        activeTextColor.set(value == null ? "" : value);
    }

    public StringProperty activeTextColorProperty() {
        return activeTextColor;
    }

    public String getPopperClass() {
        return popperClass.get();
    }

    public void setPopperClass(String value) {
        popperClass.set(value == null ? "" : value);
    }

    public StringProperty popperClassProperty() {
        return popperClass;
    }

    public EventHandler<EleFXMenuEvent> getOnSelect() {
        return onSelect.get();
    }

    public void setOnSelect(EventHandler<EleFXMenuEvent> value) {
        onSelect.set(value);
    }

    public ObjectProperty<EventHandler<EleFXMenuEvent>> onSelectProperty() {
        return onSelect;
    }

    public EventHandler<EleFXMenuEvent> getOnOpen() {
        return onOpen.get();
    }

    public void setOnOpen(EventHandler<EleFXMenuEvent> value) {
        onOpen.set(value);
    }

    public ObjectProperty<EventHandler<EleFXMenuEvent>> onOpenProperty() {
        return onOpen;
    }

    public EventHandler<EleFXMenuEvent> getOnClose() {
        return onClose.get();
    }

    public void setOnClose(EventHandler<EleFXMenuEvent> value) {
        onClose.set(value);
    }

    public ObjectProperty<EventHandler<EleFXMenuEvent>> onCloseProperty() {
        return onClose;
    }

    public void open(String index) {
        findSubmenu(index).ifPresent(this::open);
    }

    public void close(String index) {
        findSubmenu(index).ifPresent(this::close);
    }

    public void updateActiveIndex(String index) {
        setActiveIndex(index);
    }

    public void setDefaultOpeneds(Collection<String> indexes) {
        defaultOpeneds.setAll(indexes == null ? List.of() : indexes);
        refresh();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.MENU;
    }

    boolean usesHover() {
        return getMode() == EleFXMenuMode.HORIZONTAL && !isMenuTriggerClick();
    }

    void select(EleFXMenuItem item) {
        setActiveIndex(item.getIndex());
        closeAll();
        emit(EleFXMenuEvent.SELECT, item.getIndex(), pathFor(item), item);
    }

    void open(EleFXSubMenu sub) {
        if (isUniqueOpened()) for (EleFXSubMenu candidate : submenus())
            if (candidate != sub && candidate.parent == sub.parent) close(candidate);
        sub.setExpandedFromMenu(true);
        emit(EleFXMenuEvent.OPEN, sub.getIndex(), pathFor(sub), null);
    }

    void close(EleFXSubMenu sub) {
        sub.setExpandedFromMenu(false);
        emit(EleFXMenuEvent.CLOSE, sub.getIndex(), pathFor(sub), null);
    }

    List<String> pathFor(Object target) {
        ArrayList<String> path = new ArrayList<>();
        if (target instanceof EleFXSubMenu sub) {
            for (EleFXSubMenu x = sub; x != null; x = x.parent)
                path.add(0, x.getIndex());
        } else if (target instanceof EleFXMenuItem item) {
            EleFXSubMenu owner = ownerOf(item);
            for (EleFXSubMenu x = owner; x != null; x = x.parent)
                path.add(0, x.getIndex());
            path.add(item.getIndex());
        }
        return path;
    }

    private void initialize() {
        getStyleClass().add("ele-menu");
        horizontalContent.getStyleClass().add("ele-menu__horizontal-content");
        items.addListener((javafx.collections.ListChangeListener<EleFXMenuEntry>) c -> refresh());
        mode.addListener(o -> refresh());
        collapse.addListener(o -> refresh());
        activeIndex.addListener(o -> refreshStates());
        backgroundColor.addListener(o -> applyInlineColors());
        textColor.addListener(o -> applyInlineColors());
        activeTextColor.addListener(o -> applyInlineColors());
        sceneBuilderIntegration();
        refresh();
    }

    private void refresh() {
        getChildren().clear();
        Node container = getMode() == EleFXMenuMode.HORIZONTAL ? horizontalContent : null;
        if (container != null) {
            horizontalContent.getChildren().clear();
            getChildren().add(container);
        }
        for (EleFXMenuEntry entry : items) {
            install(entry, null);
            (container == null ? getChildren() : horizontalContent.getChildren()).add(entry.node());
        }
        for (String index : defaultOpeneds)
            open(index);
        applyInlineColors();
        refreshStates();
        getStyleClass().removeAll("ele-menu--horizontal", "ele-menu--vertical", "ele-menu--collapse");
        getStyleClass().add(getMode() == EleFXMenuMode.HORIZONTAL ? "ele-menu--horizontal" : "ele-menu--vertical");
        if (isCollapse()) getStyleClass().add("ele-menu--collapse");
    }

    private void install(EleFXMenuEntry entry, EleFXSubMenu parent) {
        if (entry instanceof EleFXMenuItem item)
            item.menu = this;
        else if (entry instanceof EleFXSubMenu sub) {
            sub.parent = parent;
            sub.install(this);
        } else if (entry instanceof EleFXMenuItemGroup group) group.install(this);
    }

    private void refreshStates() {
        for (EleFXMenuItem item : menuItems())
            item.refresh();
    }

    private void applyInlineColors() {
        StringBuilder css = new StringBuilder();
        if (!getBackgroundColor().isBlank())
            css.append("-fx-background-color: ").append(getBackgroundColor()).append(';');
        if (!getTextColor().isBlank()) css.append("-fx-text-fill: ").append(getTextColor()).append(';');
        if (!getActiveTextColor().isBlank())
            css.append("-elefx-menu-active-color: ").append(getActiveTextColor()).append(';');
        setStyle(css.toString());
    }

    private void closeAll() {
        for (EleFXSubMenu sub : submenus())
            close(sub);
    }

    private void emit(javafx.event.EventType<EleFXMenuEvent> type, String index, List<String> path,
                      EleFXMenuItem item) {
        EleFXMenuEvent event = new EleFXMenuEvent(this, this, type, index, path, item);
        fireEvent(event);
        EventHandler<EleFXMenuEvent> handler = type == EleFXMenuEvent.SELECT
                ? getOnSelect()
                : type == EleFXMenuEvent.OPEN ? getOnOpen() : getOnClose();
        if (handler != null) handler.handle(event);
    }

    private java.util.Optional<EleFXSubMenu> findSubmenu(String index) {
        return submenus().stream().filter(s -> s.getIndex().equals(index)).findFirst();
    }

    private List<EleFXSubMenu> submenus() {
        ArrayList<EleFXSubMenu> out = new ArrayList<>();
        collectSubmenus(items, out);
        return out;
    }

    private List<EleFXMenuItem> menuItems() {
        ArrayList<EleFXMenuItem> out = new ArrayList<>();
        collectItems(items, out);
        return out;
    }

    private void collectSubmenus(Collection<EleFXMenuEntry> entries, List<EleFXSubMenu> out) {
        for (EleFXMenuEntry e : entries) {
            if (e instanceof EleFXSubMenu s) {
                out.add(s);
                collectSubmenus(s.getItems(), out);
            } else if (e instanceof EleFXMenuItemGroup g) collectSubmenus(g.getItems(), out);
        }
    }

    private void collectItems(Collection<EleFXMenuEntry> entries, List<EleFXMenuItem> out) {
        for (EleFXMenuEntry e : entries) {
            if (e instanceof EleFXMenuItem i)
                out.add(i);
            else if (e instanceof EleFXSubMenu s)
                collectItems(s.getItems(), out);
            else if (e instanceof EleFXMenuItemGroup g) collectItems(g.getItems(), out);
        }
    }

    private EleFXSubMenu ownerOf(EleFXMenuItem item) {
        return ownerOf(items, item, null);
    }

    private EleFXSubMenu ownerOf(Collection<EleFXMenuEntry> entries, EleFXMenuItem item, EleFXSubMenu parent) {
        for (EleFXMenuEntry entry : entries) {
            if (entry == item) return parent;
            if (entry instanceof EleFXSubMenu submenu) {
                EleFXSubMenu found = ownerOf(submenu.getItems(), item, submenu);
                if (found != null) return found;
            }
            if (entry instanceof EleFXMenuItemGroup group) {
                EleFXSubMenu found = ownerOf(group.getItems(), item, parent);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Duration validDuration(Duration d) {
        if (d == null || d.lessThan(Duration.ZERO))
            throw new IllegalArgumentException("timeout must not be null or negative");
        return d;
    }
}
