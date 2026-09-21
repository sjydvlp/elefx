package com.sjydvlp.elefx.component.popconfirm;

import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.button.EleFXButtonSize;
import com.sjydvlp.elefx.component.button.EleFXButtonType;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * An Element Plus-inspired confirmation popover anchored to a reference node.
 *
 * <p>
 * Set {@link #setReference(Node)} to install its click trigger. The default
 * action bar can be replaced with {@link #setActions(Node)}; custom action
 * controls may call {@link #confirm()} and {@link #cancel()}.
 * </p>
 */
public final class EleFXPopconfirm {

    private static final double GAP = 10;

    private final ObjectProperty<Node> reference = new SimpleObjectProperty<>(this, "reference");

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final ObjectProperty<EleFXPopconfirmPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXPopconfirmPlacement.TOP);

    private final ObjectProperty<EleFXPopconfirmEffect> effect = new SimpleObjectProperty<>(this, "effect",
            EleFXPopconfirmEffect.LIGHT);

    private final StringProperty confirmButtonText = new SimpleStringProperty(this, "confirmButtonText", "OK");

    private final StringProperty cancelButtonText = new SimpleStringProperty(this, "cancelButtonText", "Cancel");

    private final ObjectProperty<EleFXButtonType> confirmButtonType = new SimpleObjectProperty<>(this,
            "confirmButtonType", EleFXButtonType.PRIMARY);

    private final ObjectProperty<EleFXButtonType> cancelButtonType = new SimpleObjectProperty<>(this,
            "cancelButtonType", EleFXButtonType.DEFAULT);

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon",
            new EleFXIcon(EleFXIconType.QUESTION_FILLED, 16));

    private final ObjectProperty<Paint> iconColor = new SimpleObjectProperty<>(this, "iconColor",
            Paint.valueOf("#f90"));

    private final BooleanProperty hideIcon = new SimpleBooleanProperty(this, "hideIcon", false);

    private final IntegerProperty hideAfter = new SimpleIntegerProperty(this, "hideAfter", 200);

    private final BooleanProperty teleported = new SimpleBooleanProperty(this, "teleported", true);

    private final BooleanProperty persistent = new SimpleBooleanProperty(this, "persistent", false);

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 150);

    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", false);

    private final ObjectProperty<Node> actions = new SimpleObjectProperty<>(this, "actions");

    private final ObjectProperty<EventHandler<ActionEvent>> onConfirm = new SimpleObjectProperty<>(this, "onConfirm");

    private final ObjectProperty<EventHandler<ActionEvent>> onCancel = new SimpleObjectProperty<>(this, "onCancel");

    private Popup popup;

    private VBox root;

    private HBox message;

    private Region arrow;

    private PauseTransition hideDelay;

    private Scene observedScene;

    private final javafx.event.EventHandler<MouseEvent> referenceClickHandler = e -> Platform.runLater(this::toggle);

    private final javafx.event.EventHandler<MouseEvent> outsideClickHandler = this::hideWhenClickedOutside;

    private final javafx.event.EventHandler<KeyEvent> escapeHandler = e -> {
        if (e.getCode() == KeyCode.ESCAPE && isShowing()) {
            hide();
            e.consume();
        }
    };

    public EleFXPopconfirm() {
        initialize();
    }

    public EleFXPopconfirm(Node reference, String title) {
        this();
        setReference(reference);
        setTitle(title);
    }

    public void show() {
        Node anchor = getReference();
        if (anchor == null || anchor.getScene() == null || anchor.getScene().getWindow() == null) return;
        if (isShowing()) {
            position();
            return;
        }
        createPopup();
        refresh();
        Window owner = anchor.getScene().getWindow();
        root.applyCss();
        root.autosize();
        popup.show(owner, 0, 0);
        position();
        showing.set(true);
        observeScene(anchor.getScene());
    }

    public void hide() {
        if (!isShowing()) return;
        if (hideDelay != null) hideDelay.stop();
        int delay = getHideAfter();
        if (delay == 0)
            hideNow();
        else {
            hideDelay = new PauseTransition(Duration.millis(delay));
            hideDelay.setOnFinished(e -> hideNow());
            hideDelay.play();
        }
    }

    /** Hides the popup immediately, without applying {@link #getHideAfter()}. */
    public void hideNow() {
        if (hideDelay != null) hideDelay.stop();
        if (popup != null) popup.hide();
        showing.set(false);
        unobserveScene();
        if (!isPersistent()) {
            popup = null;
            root = null;
        }
    }

    public void toggle() {
        if (isShowing())
            hide();
        else
            show();
    }

    /**
     * Returns the JavaFX popup used as the Element Plus {@code popperRef}
     * equivalent. It is created on the first call to {@link #show()}.
     */
    public Popup getPopperRef() {
        return popup;
    }

    public void confirm() {
        fire(getOnConfirm());
        hide();
    }

    public void cancel() {
        fire(getOnCancel());
        hide();
    }

    public boolean isShowing() {
        return showing.get() && popup != null && popup.isShowing();
    }

    public Node getReference() {
        return reference.get();
    }

    public ObjectProperty<Node> referenceProperty() {
        return reference;
    }

    public void setReference(Node value) {
        reference.set(value);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public EleFXPopconfirmPlacement getPlacement() {
        return placement.get();
    }

    public ObjectProperty<EleFXPopconfirmPlacement> placementProperty() {
        return placement;
    }

    public void setPlacement(EleFXPopconfirmPlacement value) {
        placement.set(value == null ? EleFXPopconfirmPlacement.TOP : value);
    }

    public EleFXPopconfirmEffect getEffect() {
        return effect.get();
    }

    public ObjectProperty<EleFXPopconfirmEffect> effectProperty() {
        return effect;
    }

    public void setEffect(EleFXPopconfirmEffect value) {
        effect.set(value == null ? EleFXPopconfirmEffect.LIGHT : value);
    }

    public String getConfirmButtonText() {
        return confirmButtonText.get();
    }

    public StringProperty confirmButtonTextProperty() {
        return confirmButtonText;
    }

    public void setConfirmButtonText(String value) {
        confirmButtonText.set(value == null ? "" : value);
    }

    public String getCancelButtonText() {
        return cancelButtonText.get();
    }

    public StringProperty cancelButtonTextProperty() {
        return cancelButtonText;
    }

    public void setCancelButtonText(String value) {
        cancelButtonText.set(value == null ? "" : value);
    }

    public EleFXButtonType getConfirmButtonType() {
        return confirmButtonType.get();
    }

    public ObjectProperty<EleFXButtonType> confirmButtonTypeProperty() {
        return confirmButtonType;
    }

    public void setConfirmButtonType(EleFXButtonType value) {
        confirmButtonType.set(value == null ? EleFXButtonType.PRIMARY : value);
    }

    public EleFXButtonType getCancelButtonType() {
        return cancelButtonType.get();
    }

    public ObjectProperty<EleFXButtonType> cancelButtonTypeProperty() {
        return cancelButtonType;
    }

    public void setCancelButtonType(EleFXButtonType value) {
        cancelButtonType.set(value == null ? EleFXButtonType.DEFAULT : value);
    }

    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    public Paint getIconColor() {
        return iconColor.get();
    }

    public ObjectProperty<Paint> iconColorProperty() {
        return iconColor;
    }

    public void setIconColor(Paint value) {
        iconColor.set(value);
    }

    public boolean isHideIcon() {
        return hideIcon.get();
    }

    public BooleanProperty hideIconProperty() {
        return hideIcon;
    }

    public void setHideIcon(boolean value) {
        hideIcon.set(value);
    }

    public int getHideAfter() {
        return hideAfter.get();
    }

    public IntegerProperty hideAfterProperty() {
        return hideAfter;
    }

    public void setHideAfter(int value) {
        hideAfter.set(Math.max(0, value));
    }

    public boolean isTeleported() {
        return teleported.get();
    }

    public BooleanProperty teleportedProperty() {
        return teleported;
    }

    /** JavaFX popups are window overlays; retained for Element Plus API parity. */
    public void setTeleported(boolean value) {
        teleported.set(value);
    }

    public boolean isPersistent() {
        return persistent.get();
    }

    public BooleanProperty persistentProperty() {
        return persistent;
    }

    public void setPersistent(boolean value) {
        persistent.set(value);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double value) {
        width.set(Math.max(150, value));
    }

    public BooleanProperty showingProperty() {
        return showing;
    }

    public Node getActions() {
        return actions.get();
    }

    public ObjectProperty<Node> actionsProperty() {
        return actions;
    }

    public void setActions(Node value) {
        actions.set(value);
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

    public EventHandler<ActionEvent> getOnCancel() {
        return onCancel.get();
    }

    public ObjectProperty<EventHandler<ActionEvent>> onCancelProperty() {
        return onCancel;
    }

    public void setOnCancel(EventHandler<ActionEvent> value) {
        onCancel.set(value);
    }

    private void initialize() {
        reference.addListener((o, oldValue, newValue) -> {
            if (oldValue != null) oldValue.removeEventHandler(MouseEvent.MOUSE_CLICKED, referenceClickHandler);
            if (newValue != null) newValue.addEventHandler(MouseEvent.MOUSE_CLICKED, referenceClickHandler);
        });
        title.addListener(o -> refresh());
        effect.addListener(o -> refresh());
        placement.addListener(o -> {
            refresh();
            if (isShowing()) position();
        });
        confirmButtonText.addListener(o -> refresh());
        cancelButtonText.addListener(o -> refresh());
        confirmButtonType.addListener(o -> refresh());
        cancelButtonType.addListener(o -> refresh());
        icon.addListener(o -> refresh());
        iconColor.addListener(o -> refresh());
        hideIcon.addListener(o -> refresh());
        width.addListener(o -> {
            refresh();
            if (isShowing()) position();
        });
        actions.addListener(o -> refresh());
    }

    private void createPopup() {
        if (popup != null) return;
        popup = new Popup();
        popup.setAutoHide(false);
        popup.setAutoFix(false);
        root = new VBox();
        root.getStyleClass().add("ele-popconfirm");
        root.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        root.getStylesheets().add(EleFXThemes.POPCONFIRM.toData());
        arrow = new Region();
        arrow.getStyleClass().add("ele-popconfirm__arrow");
        popup.getContent().add(root);
    }

    private void refresh() {
        if (root == null) return;
        root.getStyleClass().removeIf(c -> c.startsWith("ele-popconfirm--"));
        root.getStyleClass().add("ele-popconfirm--" + getEffect().name().toLowerCase());
        root.setMinWidth(getWidth());
        root.setPrefWidth(getWidth());
        message = new HBox();
        message.getStyleClass().add("ele-popconfirm__main");
        message.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        Node currentIcon = getIcon();
        if (!isHideIcon() && currentIcon != null) {
            currentIcon.getStyleClass().add("ele-popconfirm__icon");
            if (currentIcon instanceof EleFXIcon iconNode) iconNode.setColor(getIconColor());
            message.getChildren().add(currentIcon);
        }
        Label label = new Label(getTitle());
        label.getStyleClass().add("ele-popconfirm__message");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        message.getChildren().add(label);
        Node actionNode = getActions();
        if (actionNode == null) actionNode = defaultActions();
        actionNode.getStyleClass().add("ele-popconfirm__actions");
        updateArrowClass();
        root.getChildren().setAll(message, actionNode, arrow);
    }

    private HBox defaultActions() {
        EleFXButton cancel = new EleFXButton(getCancelButtonText(), getCancelButtonType(), EleFXButtonSize.SMALL);
        cancel.getStyleClass().add("ele-popconfirm__cancel");
        cancel.setOnAction(e -> cancel());
        EleFXButton confirm = new EleFXButton(getConfirmButtonText(), getConfirmButtonType(), EleFXButtonSize.SMALL);
        confirm.getStyleClass().add("ele-popconfirm__confirm");
        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> confirm());
        HBox box = new HBox(8, cancel, confirm);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private void updateArrowClass() {
        arrow.getStyleClass().removeIf(c -> c.startsWith("ele-popconfirm__arrow--"));
        String side = switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> "bottom";
            case BOTTOM_START, BOTTOM, BOTTOM_END -> "top";
            case LEFT_START, LEFT, LEFT_END -> "right";
            default -> "left";
        };
        arrow.getStyleClass().add("ele-popconfirm__arrow--" + side);
    }

    private void position() {
        Node anchor = getReference();
        if (anchor == null || root == null || !popup.isShowing()) return;
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b == null) return;
        root.applyCss();
        root.autosize();
        double w = root.prefWidth(-1), h = root.prefHeight(w);
        double x, y;
        EleFXPopconfirmPlacement p = getPlacement();
        boolean top = p == EleFXPopconfirmPlacement.TOP || p == EleFXPopconfirmPlacement.TOP_START
                || p == EleFXPopconfirmPlacement.TOP_END;
        boolean bottom = p == EleFXPopconfirmPlacement.BOTTOM || p == EleFXPopconfirmPlacement.BOTTOM_START
                || p == EleFXPopconfirmPlacement.BOTTOM_END;
        if (top || bottom) {
            x = p.name().endsWith("_START")
                    ? b.getMinX()
                    : p.name().endsWith("_END") ? b.getMaxX() - w : b.getMinX() + (b.getWidth() - w) / 2;
            y = top ? b.getMinY() - h - GAP : b.getMaxY() + GAP;
        } else {
            y = p.name().endsWith("_START")
                    ? b.getMinY()
                    : p.name().endsWith("_END") ? b.getMaxY() - h : b.getMinY() + (b.getHeight() - h) / 2;
            x = p.name().startsWith("LEFT") ? b.getMinX() - w - GAP : b.getMaxX() + GAP;
        }
        Rectangle2D screen = Screen.getScreensForRectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()).get(0)
                .getVisualBounds();
        popup.setX(Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - w)));
        popup.setY(Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - h)));
        positionArrow(w, h, b);
    }

    private void positionArrow(double width, double height, Bounds anchorBounds) {
        double x = Math.max(8,
                Math.min(width - 18, anchorBounds.getMinX() + anchorBounds.getWidth() / 2 - popup.getX() - 5));
        double y = Math.max(8,
                Math.min(height - 18, anchorBounds.getMinY() + anchorBounds.getHeight() / 2 - popup.getY() - 5));
        switch (getPlacement()) {
            case TOP_START, TOP, TOP_END -> arrow.resizeRelocate(x, height - 5, 10, 10);
            case BOTTOM_START, BOTTOM, BOTTOM_END -> arrow.resizeRelocate(x, -5, 10, 10);
            case LEFT_START, LEFT, LEFT_END -> arrow.resizeRelocate(width - 5, y, 10, 10);
            case RIGHT_START, RIGHT, RIGHT_END -> arrow.resizeRelocate(-5, y, 10, 10);
        }
    }

    private void observeScene(Scene scene) {
        if (observedScene == scene) return;
        unobserveScene();
        observedScene = scene;
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escapeHandler);
    }

    private void unobserveScene() {
        if (observedScene != null) {
            observedScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
            observedScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeHandler);
            observedScene = null;
        }
    }

    private void hideWhenClickedOutside(MouseEvent event) {
        Node anchor = getReference();
        if (anchor != null && !isDescendant(event.getTarget(), anchor)) hide();
    }

    private boolean isDescendant(Object candidate, Node parent) {
        if (!(candidate instanceof Node node)) return false;
        for (Node n = node; n != null; n = n.getParent())
            if (n == parent) return true;
        return false;
    }

    private void fire(EventHandler<ActionEvent> handler) {
        if (handler != null) handler.handle(new ActionEvent(this, null));
    }
}
