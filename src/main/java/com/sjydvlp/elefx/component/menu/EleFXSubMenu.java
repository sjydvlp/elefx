package com.sjydvlp.elefx.component.menu;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

/** Expandable menu entry. It renders inline in an expanded vertical menu and as a popup otherwise. */
public class EleFXSubMenu extends VBox implements EleFXMenuEntry {

    private final StringProperty index = new SimpleStringProperty(this, "index", "");

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    private final DoubleProperty popperOffset = new SimpleDoubleProperty(this, "popperOffset", Double.NaN);

    private final ObservableList<EleFXMenuEntry> items = FXCollections.observableArrayList();

    private final HBox header = new HBox();

    private final Label label = new Label();

    private final Region titleSpacer = new Region();

    private final EleFXIcon arrow = new EleFXIcon(EleFXIconType.ARROW_DOWN, 12);

    private final VBox content = new VBox();

    private final Popup popup = new Popup();

    private final PauseTransition hideDelay = new PauseTransition();

    private final RotateTransition arrowRotation = new RotateTransition(Duration.millis(300), arrow);

    private double arrowTarget = Double.NaN;

    EleFXMenu menu;

    EleFXSubMenu parent;

    public EleFXSubMenu() {
        initialize();
    }

    public EleFXSubMenu(String index, String text, EleFXMenuEntry... entries) {
        this();
        setIndex(index);
        setText(text);
        getItems().addAll(entries);
    }

    public ObservableList<EleFXMenuEntry> getItems() {
        return items;
    }

    public String getIndex() {
        return index.get();
    }

    public void setIndex(String value) {
        index.set(value == null ? "" : value);
    }

    public StringProperty indexProperty() {
        return index;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    public StringProperty textProperty() {
        return text;
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public Node getIcon() {
        return icon.get();
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public boolean isExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
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

    @Override
    public Node node() {
        return this;
    }

    void install(EleFXMenu owner) {
        menu = owner;
        for (EleFXMenuEntry entry : items)
            installEntry(entry);
        refresh();
    }

    private void initialize() {
        getStyleClass().add("ele-sub-menu");
        header.getStyleClass().add("ele-sub-menu__title");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setFocusTraversable(true);
        label.getStyleClass().add("ele-sub-menu__label");
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        arrow.getStyleClass().add("ele-sub-menu__arrow");
        content.getStyleClass().add("ele-sub-menu__content");
        popup.setAutoHide(false);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        getChildren().addAll(header, content);
        header.setOnMouseClicked(e -> toggle());
        header.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                toggle();
                e.consume();
            }
        });
        header.setOnMouseEntered(e -> {
            cancelHideChain();
            if (usesHover()) open();
        });
        header.setOnMouseExited(e -> scheduleHide());
        content.setOnMouseEntered(e -> cancelHideChain());
        content.setOnMouseExited(e -> scheduleHideChain());
        hideDelay.setOnFinished(e -> {
            if (!isPointerInsideBranch()) close();
        });
        items.addListener((javafx.collections.ListChangeListener<EleFXMenuEntry>) c -> refresh());
        text.addListener(o -> refresh());
        titleNode.addListener(o -> refresh());
        icon.addListener(o -> refresh());
        expanded.addListener(o -> refreshState());
        disableProperty().addListener(o -> refreshState());
        refresh();
    }

    void open() {
        if (isDisabled() || isExpanded()) return;
        if (menu != null) menu.open(this);
    }

    void close() {
        if (!isExpanded()) return;
        if (menu != null) menu.close(this);
    }

    private void toggle() {
        if (!isDisabled()) {
            if (isExpanded())
                close();
            else
                open();
        }
    }

    void setExpandedFromMenu(boolean value) {
        expanded.set(value);
        updateContentPlacement();
        refreshState();
    }

    private void updateContentPlacement() {
        boolean inline = menu == null || menu.getMode() == EleFXMenuMode.VERTICAL && !menu.isCollapse();
        if (inline) {
            popup.hide();
            popup.getContent().remove(content);
            if (!getChildren().contains(content)) getChildren().add(content);
            content.getStyleClass().removeAll("ele-menu--popup", "ele-menu--popup-horizontal");
            content.setVisible(isExpanded());
            content.setManaged(isExpanded());
        } else {
            getChildren().remove(content);
            if (!popup.getContent().contains(content)) popup.getContent().add(content);
            if (!content.getStyleClass().contains("ele-menu--popup"))
                content.getStyleClass().add("ele-menu--popup");
            content.getStyleClass().remove("ele-menu--popup-horizontal");
            if (menu.getMode() == EleFXMenuMode.HORIZONTAL)
                content.getStyleClass().add("ele-menu--popup-horizontal");
            content.setVisible(true);
            content.setManaged(true);
            if (isExpanded())
                showPopup();
            else
                popup.hide();
        }
    }

    private void showPopup() {
        if (getScene() == null || popup.isShowing()) return;
        popup.getScene().getStylesheets().setAll(getScene().getStylesheets());
        popup.getScene().getStylesheets().addAll(menu.getStylesheets());
        applyMenuStyle();
        double offset = Double.isNaN(getPopperOffset()) ? menu.getPopperOffset() : getPopperOffset();
        javafx.geometry.Bounds b = header.localToScreen(header.getBoundsInLocal());
        if (b == null) return;
        boolean topLevelHorizontal = menu.getMode() == EleFXMenuMode.HORIZONTAL && parent == null;
        popup.show(header, topLevelHorizontal ? b.getMinX() : b.getMaxX() + offset,
                topLevelHorizontal ? b.getMaxY() + offset : b.getMinY());
    }

    private boolean usesHover() {
        return menu != null && (menu.usesHover() ||
                (menu.getMode() == EleFXMenuMode.VERTICAL && menu.isCollapse()) ||
                (parent != null && menu.getMode() == EleFXMenuMode.HORIZONTAL));
    }

    private void cancelHideChain() {
        for (EleFXSubMenu sub = this; sub != null; sub = sub.parent)
            sub.hideDelay.stop();
    }

    private void scheduleHideChain() {
        for (EleFXSubMenu sub = this; sub != null; sub = sub.parent)
            sub.scheduleHide();
    }

    private void scheduleHide() {
        if (usesHover()) {
            hideDelay.setDuration(menu.getHideTimeout());
            hideDelay.playFromStart();
        }
    }

    private boolean isPointerInsideBranch() {
        if (header.isHover() || content.isHover() && (popup.isShowing() || content.getParent() == this))
            return true;
        return isPointerInsideChildPopup(items);
    }

    private boolean isPointerInsideChildPopup(ObservableList<EleFXMenuEntry> entries) {
        for (EleFXMenuEntry entry : entries) {
            if (entry instanceof EleFXSubMenu sub && sub.isExpanded() && sub.isPointerInsideBranch())
                return true;
            if (entry instanceof EleFXMenuItemGroup group && isPointerInsideChildPopup(group.getItems()))
                return true;
        }
        return false;
    }

    private void refresh() {
        header.getChildren().clear();
        if (getIcon() != null) {
            getIcon().getStyleClass().add("ele-sub-menu__icon");
            header.getChildren().add(getIcon());
        }
        label.setText(getText());
        header.getChildren().add(getTitleNode() == null ? label : getTitleNode());
        header.getChildren().add(titleSpacer);
        header.getChildren().add(arrow);
        content.getChildren().clear();
        for (EleFXMenuEntry e : items) {
            installEntry(e);
            content.getChildren().add(e.node());
        }
        updateContentPlacement();
        refreshState();
    }

    private void installEntry(EleFXMenuEntry e) {
        if (e instanceof EleFXMenuItem item)
            item.menu = menu;
        else if (e instanceof EleFXSubMenu sub) {
            sub.parent = this;
            sub.install(menu);
        } else if (e instanceof EleFXMenuItemGroup group) group.install(menu, this);
    }

    void applyMenuStyle() {
        if (menu != null) content.setStyle(menu.getStyle());
    }

    void refreshState() {
        getStyleClass().removeAll("ele-sub-menu--open", "ele-sub-menu--active",
                "ele-sub-menu--disabled", "ele-sub-menu--collapsed");
        if (isExpanded()) getStyleClass().add("ele-sub-menu--open");
        if (menu != null && containsActiveItem(items, menu.getActiveIndex()))
            getStyleClass().add("ele-sub-menu--active");
        if (isDisabled()) getStyleClass().add("ele-sub-menu--disabled");
        if (menu != null && menu.isCollapse()) getStyleClass().add("ele-sub-menu--collapsed");
        boolean hideArrow = menu != null && menu.isCollapse()
                && menu.getMode() == EleFXMenuMode.VERTICAL && parent == null;
        arrow.setVisible(!hideArrow);
        arrow.setManaged(!hideArrow);
        boolean popupChild = parent != null && menu != null &&
                (menu.getMode() == EleFXMenuMode.HORIZONTAL || menu.isCollapse());
        EleFXIconType arrowType = popupChild ? EleFXIconType.ARROW_RIGHT : EleFXIconType.ARROW_DOWN;
        if (arrow.getType() != arrowType) {
            arrowRotation.stop();
            arrow.setType(arrowType);
            arrow.setRotate(0);
            arrowTarget = 0;
        }
        double target = isExpanded() ? 180 : 0;
        if (Double.isNaN(arrowTarget)) {
            arrow.setRotate(target);
        } else if (arrowTarget != target) {
            arrowRotation.stop();
            arrowRotation.setFromAngle(arrow.getRotate());
            arrowRotation.setToAngle(target);
            arrowRotation.playFromStart();
        }
        arrowTarget = target;
    }

    private boolean containsActiveItem(ObservableList<EleFXMenuEntry> entries, String activeIndex) {
        for (EleFXMenuEntry entry : entries) {
            if (entry instanceof EleFXMenuItem item && !item.getIndex().isEmpty()
                    && item.getIndex().equals(activeIndex))
                return true;
            if (entry instanceof EleFXSubMenu sub && containsActiveItem(sub.getItems(), activeIndex)) return true;
            if (entry instanceof EleFXMenuItemGroup group && containsActiveItem(group.getItems(), activeIndex))
                return true;
        }
        return false;
    }
}
