package com.sjydvlp.elefx.component.dialog;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

/**
 * An Element Plus inspired dialog window with modal, draggable and composable content areas.
 *
 * <p>
 * Set content through {@link #getBodyChildren()}, and use {@link #setHeader(Node)} and
 * {@link #setFooter(Node)} for the equivalent of Element Plus named slots. The dialog is
 * intentionally a window-owned overlay rather than a layout child; call {@link #show(Window)}
 * once an owner window is available.
 * </p>
 */
public final class EleFXDialog {

    public static final EventType<Event> OPEN = new EventType<>(Event.ANY, "ELEFX_DIALOG_OPEN");

    public static final EventType<Event> OPENED = new EventType<>(Event.ANY, "ELEFX_DIALOG_OPENED");

    public static final EventType<Event> CLOSE = new EventType<>(Event.ANY, "ELEFX_DIALOG_CLOSE");

    public static final EventType<Event> CLOSED = new EventType<>(Event.ANY, "ELEFX_DIALOG_CLOSED");

    public static final EventType<Event> OPEN_AUTO_FOCUS = new EventType<>(Event.ANY, "ELEFX_DIALOG_OPEN_AUTO_FOCUS");

    public static final EventType<Event> CLOSE_AUTO_FOCUS = new EventType<>(Event.ANY, "ELEFX_DIALOG_CLOSE_AUTO_FOCUS");

    @FunctionalInterface
    public interface BeforeCloseHandler {

        void handle(Runnable done);
    }

    public enum Transition {
        FADE,
        SCALE,
        SLIDE,
        BOUNCE
    }

    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", false);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 500);

    private final DoubleProperty top = new SimpleDoubleProperty(this, "top", Double.NaN);

    private final BooleanProperty fullscreen = new SimpleBooleanProperty(this, "fullscreen", false);

    private final BooleanProperty modal = new SimpleBooleanProperty(this, "modal", true);

    private final BooleanProperty modalPenetrable = new SimpleBooleanProperty(this, "modalPenetrable", false);

    private final BooleanProperty lockScroll = new SimpleBooleanProperty(this, "lockScroll", true);

    private final IntegerProperty openDelay = new SimpleIntegerProperty(this, "openDelay", 0);

    private final IntegerProperty closeDelay = new SimpleIntegerProperty(this, "closeDelay", 0);

    private final BooleanProperty closeOnClickModal = new SimpleBooleanProperty(this, "closeOnClickModal", true);

    private final BooleanProperty closeOnPressEscape = new SimpleBooleanProperty(this, "closeOnPressEscape", true);

    private final BooleanProperty showClose = new SimpleBooleanProperty(this, "showClose", true);

    private final BooleanProperty draggable = new SimpleBooleanProperty(this, "draggable", false);

    private final BooleanProperty overflow = new SimpleBooleanProperty(this, "overflow", false);

    private final BooleanProperty center = new SimpleBooleanProperty(this, "center", false);

    private final BooleanProperty alignCenter = new SimpleBooleanProperty(this, "alignCenter", false);

    private final BooleanProperty destroyOnClose = new SimpleBooleanProperty(this, "destroyOnClose", false);

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");

    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer");

    private final ObjectProperty<Node> closeIcon = new SimpleObjectProperty<>(this, "closeIcon");

    private final ObjectProperty<BeforeCloseHandler> beforeClose = new SimpleObjectProperty<>(this, "beforeClose");

    private final ObjectProperty<Transition> transition = new SimpleObjectProperty<>(this, "transition",
            Transition.FADE);

    private final IntegerProperty transitionDuration = new SimpleIntegerProperty(this, "transitionDuration", 200);

    private final StringProperty headerClass = new SimpleStringProperty(this, "headerClass", "");

    private final StringProperty bodyClass = new SimpleStringProperty(this, "bodyClass", "");

    private final StringProperty footerClass = new SimpleStringProperty(this, "footerClass", "");

    private final ObjectProperty<EventHandler<Event>> onOpen = new SimpleObjectProperty<>(this, "onOpen");

    private final ObjectProperty<EventHandler<Event>> onOpened = new SimpleObjectProperty<>(this, "onOpened");

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final ObjectProperty<EventHandler<Event>> onClosed = new SimpleObjectProperty<>(this, "onClosed");

    private final ObjectProperty<EventHandler<Event>> onOpenAutoFocus = new SimpleObjectProperty<>(this,
            "onOpenAutoFocus");

    private final ObjectProperty<EventHandler<Event>> onCloseAutoFocus = new SimpleObjectProperty<>(this,
            "onCloseAutoFocus");

    private final VBox body = new VBox();

    private final HBox headerBox = new HBox();

    private final HBox footerBox = new HBox();

    private final Label titleLabel = new Label();

    private final Button closeButton = new Button();

    private final Set<String> appliedHeaderClasses = new HashSet<>(), appliedBodyClasses = new HashSet<>(),
            appliedFooterClasses = new HashSet<>();

    private Stage stage;

    private StackPane overlay;

    private VBox dialog;

    private Window owner;

    private boolean closing;

    private double dragX, dragY, dialogX, dialogY;

    public EleFXDialog() {
        initialize();
    }

    public EleFXDialog(Node... content) {
        this();
        body.getChildren().addAll(content);
    }

    public void show() {
        show(null);
    }

    public void show(Window owner) {
        this.owner = owner;
        if (isShowing()) {
            stage.toFront();
            return;
        }
        setShowing(true);
    }

    public void hide() {
        close();
    }

    /** Closes immediately, bypassing {@link #getBeforeClose()}, for application-driven dismissal. */
    public void close() {
        finishClose();
    }

    /** Requests a user-style close, honoring {@link #getBeforeClose()}. */
    public void requestClose() {
        if (stage == null || !stage.isShowing() || closing) return;
        BeforeCloseHandler guard = getBeforeClose();
        if (guard == null)
            finishClose();
        else
            guard.handle(this::finishClose);
    }

    public void handleClose() {
        requestClose();
    }

    public boolean isShowing() {
        return showing.get() && stage != null && stage.isShowing();
    }

    public void resetPosition() {
        if (stage != null) position();
    }

    public ObservableList<Node> getBodyChildren() {
        return body.getChildren();
    }

    public VBox getBody() {
        return body;
    }

    public HBox getHeaderBox() {
        return headerBox;
    }

    public HBox getFooterBox() {
        return footerBox;
    }

    public boolean isShowingPropertyValue() {
        return showing.get();
    }

    public BooleanProperty showingProperty() {
        return showing;
    }

    public void setShowing(boolean value) {
        showing.set(value);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String v) {
        title.set(v == null ? "" : v);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double v) {
        width.set(Math.max(100, v));
    }

    public double getTop() {
        return top.get();
    }

    public DoubleProperty topProperty() {
        return top;
    }

    public void setTop(double v) {
        top.set(v);
    }

    public boolean isFullscreen() {
        return fullscreen.get();
    }

    public BooleanProperty fullscreenProperty() {
        return fullscreen;
    }

    public void setFullscreen(boolean v) {
        fullscreen.set(v);
    }

    public boolean isModal() {
        return modal.get();
    }

    public BooleanProperty modalProperty() {
        return modal;
    }

    public void setModal(boolean v) {
        modal.set(v);
    }

    public boolean isModalPenetrable() {
        return modalPenetrable.get();
    }

    public BooleanProperty modalPenetrableProperty() {
        return modalPenetrable;
    }

    public void setModalPenetrable(boolean v) {
        modalPenetrable.set(v);
    }

    public boolean isLockScroll() {
        return lockScroll.get();
    }

    public BooleanProperty lockScrollProperty() {
        return lockScroll;
    }

    public void setLockScroll(boolean v) {
        lockScroll.set(v);
    }

    public int getOpenDelay() {
        return openDelay.get();
    }

    public IntegerProperty openDelayProperty() {
        return openDelay;
    }

    public void setOpenDelay(int v) {
        openDelay.set(Math.max(0, v));
    }

    public int getCloseDelay() {
        return closeDelay.get();
    }

    public IntegerProperty closeDelayProperty() {
        return closeDelay;
    }

    public void setCloseDelay(int v) {
        closeDelay.set(Math.max(0, v));
    }

    public boolean isCloseOnClickModal() {
        return closeOnClickModal.get();
    }

    public BooleanProperty closeOnClickModalProperty() {
        return closeOnClickModal;
    }

    public void setCloseOnClickModal(boolean v) {
        closeOnClickModal.set(v);
    }

    public boolean isCloseOnPressEscape() {
        return closeOnPressEscape.get();
    }

    public BooleanProperty closeOnPressEscapeProperty() {
        return closeOnPressEscape;
    }

    public void setCloseOnPressEscape(boolean v) {
        closeOnPressEscape.set(v);
    }

    public boolean isShowClose() {
        return showClose.get();
    }

    public BooleanProperty showCloseProperty() {
        return showClose;
    }

    public void setShowClose(boolean v) {
        showClose.set(v);
    }

    public boolean isDraggable() {
        return draggable.get();
    }

    public BooleanProperty draggableProperty() {
        return draggable;
    }

    public void setDraggable(boolean v) {
        draggable.set(v);
    }

    public boolean isOverflow() {
        return overflow.get();
    }

    public BooleanProperty overflowProperty() {
        return overflow;
    }

    public void setOverflow(boolean v) {
        overflow.set(v);
    }

    public boolean isCenter() {
        return center.get();
    }

    public BooleanProperty centerProperty() {
        return center;
    }

    public void setCenter(boolean v) {
        center.set(v);
    }

    public boolean isAlignCenter() {
        return alignCenter.get();
    }

    public BooleanProperty alignCenterProperty() {
        return alignCenter;
    }

    public void setAlignCenter(boolean v) {
        alignCenter.set(v);
    }

    public boolean isDestroyOnClose() {
        return destroyOnClose.get();
    }

    public BooleanProperty destroyOnCloseProperty() {
        return destroyOnClose;
    }

    public void setDestroyOnClose(boolean v) {
        destroyOnClose.set(v);
    }

    public Node getHeader() {
        return header.get();
    }

    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    public void setHeader(Node v) {
        header.set(v);
    }

    public Node getFooter() {
        return footer.get();
    }

    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    public void setFooter(Node v) {
        footer.set(v);
    }

    public Node getCloseIcon() {
        return closeIcon.get();
    }

    public ObjectProperty<Node> closeIconProperty() {
        return closeIcon;
    }

    public void setCloseIcon(Node v) {
        closeIcon.set(v);
    }

    public BeforeCloseHandler getBeforeClose() {
        return beforeClose.get();
    }

    public ObjectProperty<BeforeCloseHandler> beforeCloseProperty() {
        return beforeClose;
    }

    public void setBeforeClose(BeforeCloseHandler v) {
        beforeClose.set(v);
    }

    public Transition getTransition() {
        return transition.get();
    }

    public ObjectProperty<Transition> transitionProperty() {
        return transition;
    }

    public void setTransition(Transition v) {
        transition.set(v == null ? Transition.FADE : v);
    }

    public int getTransitionDuration() {
        return transitionDuration.get();
    }

    public IntegerProperty transitionDurationProperty() {
        return transitionDuration;
    }

    public void setTransitionDuration(int v) {
        transitionDuration.set(Math.max(0, v));
    }

    public String getHeaderClass() {
        return headerClass.get();
    }

    public StringProperty headerClassProperty() {
        return headerClass;
    }

    public void setHeaderClass(String v) {
        headerClass.set(v == null ? "" : v);
    }

    public String getBodyClass() {
        return bodyClass.get();
    }

    public StringProperty bodyClassProperty() {
        return bodyClass;
    }

    public void setBodyClass(String v) {
        bodyClass.set(v == null ? "" : v);
    }

    public String getFooterClass() {
        return footerClass.get();
    }

    public StringProperty footerClassProperty() {
        return footerClass;
    }

    public void setFooterClass(String v) {
        footerClass.set(v == null ? "" : v);
    }

    public EventHandler<Event> getOnOpen() {
        return onOpen.get();
    }

    public ObjectProperty<EventHandler<Event>> onOpenProperty() {
        return onOpen;
    }

    public void setOnOpen(EventHandler<Event> v) {
        onOpen.set(v);
    }

    public EventHandler<Event> getOnOpened() {
        return onOpened.get();
    }

    public ObjectProperty<EventHandler<Event>> onOpenedProperty() {
        return onOpened;
    }

    public void setOnOpened(EventHandler<Event> v) {
        onOpened.set(v);
    }

    public EventHandler<Event> getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<EventHandler<Event>> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(EventHandler<Event> v) {
        onClose.set(v);
    }

    public EventHandler<Event> getOnClosed() {
        return onClosed.get();
    }

    public ObjectProperty<EventHandler<Event>> onClosedProperty() {
        return onClosed;
    }

    public void setOnClosed(EventHandler<Event> v) {
        onClosed.set(v);
    }

    public EventHandler<Event> getOnOpenAutoFocus() {
        return onOpenAutoFocus.get();
    }

    public ObjectProperty<EventHandler<Event>> onOpenAutoFocusProperty() {
        return onOpenAutoFocus;
    }

    public void setOnOpenAutoFocus(EventHandler<Event> v) {
        onOpenAutoFocus.set(v);
    }

    public EventHandler<Event> getOnCloseAutoFocus() {
        return onCloseAutoFocus.get();
    }

    public ObjectProperty<EventHandler<Event>> onCloseAutoFocusProperty() {
        return onCloseAutoFocus;
    }

    public void setOnCloseAutoFocus(EventHandler<Event> v) {
        onCloseAutoFocus.set(v);
    }

    private void initialize() {
        showing.addListener((observable, wasShowing, nowShowing) -> {
            if (nowShowing && !wasShowing)
                open();
            else if (!nowShowing && wasShowing) close();
        });
        body.getStyleClass().add("ele-dialog__body");
        body.setFillWidth(true);
        headerBox.getStyleClass().add("ele-dialog__header");
        headerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.getStyleClass().add("ele-dialog__footer");
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        titleLabel.getStyleClass().add("ele-dialog__title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        closeButton.getStyleClass().add("ele-dialog__headerbtn");
        closeButton.setAccessibleText("Close dialog");
        closeButton.setOnAction(e -> requestClose());
        closeButton.setGraphic(defaultCloseIcon());
        title.addListener(o -> refreshHeader());
        header.addListener(o -> refreshHeader());
        footer.addListener(o -> refreshFooter());
        closeIcon.addListener(o -> refreshCloseIcon());
        showClose.addListener(o -> refreshHeader());
        center.addListener(o -> refreshAlignment());
        headerClass.addListener(o -> applyClasses(headerBox, headerClass.get(), appliedHeaderClasses));
        bodyClass.addListener(o -> applyClasses(body, bodyClass.get(), appliedBodyClasses));
        footerClass.addListener(o -> applyClasses(footerBox, footerClass.get(), appliedFooterClasses));
        refreshHeader();
        refreshFooter();
    }

    private void open() {
        if (stage != null && stage.isShowing()) return;
        Runnable action = () -> {
            createIfNeeded();
            configureStage();
            position();
            fire(getOnOpen(), OPEN);
            stage.show();
            play(true, () -> {
                focusFirst();
                fire(getOnOpened(), OPENED);
            });
        };
        if (getOpenDelay() == 0)
            action.run();
        else
            delay(getOpenDelay(), action);
    }

    private void createIfNeeded() {
        if (stage != null) return;
        dialog = new VBox(headerBox, body, footerBox);
        dialog.getStyleClass().add("ele-dialog");
        dialog.setMaxHeight(Region.USE_PREF_SIZE);
        overlay = new StackPane(dialog);
        overlay.getStyleClass().add("ele-dialog__wrapper");
        overlay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getTarget() == overlay && isModal() && isCloseOnClickModal()) requestClose();
        });
        headerBox.addEventFilter(MouseEvent.MOUSE_PRESSED, this::startDrag);
        headerBox.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::drag);
        Scene scene = new Scene(overlay, Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.DIALOG.toData());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && isCloseOnPressEscape()) {
                requestClose();
                e.consume();
            }
        });
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            requestClose();
        });
    }

    private void configureStage() {
        if (owner != null && stage.getOwner() == null) stage.initOwner(owner);
        if (isModal() && stage.getModality() == Modality.NONE) stage.initModality(Modality.WINDOW_MODAL);
        overlay.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("modal"), isModal());
        dialog.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("fullscreen"), isFullscreen());
        refreshHeader();
        refreshFooter();
        refreshAlignment();
    }

    private void position() {
        var bounds = owner == null
                ? Screen.getPrimary().getVisualBounds()
                : new javafx.geometry.Rectangle2D(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight());
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        if (isFullscreen()) {
            dialog.setPrefWidth(bounds.getWidth());
            dialog.setPrefHeight(bounds.getHeight());
            dialog.setMaxHeight(Double.MAX_VALUE);
            StackPane.setAlignment(dialog, Pos.CENTER);
            return;
        }
        dialog.setPrefWidth(Math.min(getWidth(), bounds.getWidth() - 32));
        dialog.setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(dialog, isAlignCenter() ? Pos.CENTER : Pos.TOP_CENTER);
        dialog.setTranslateX(0);
        dialog.setTranslateY(isAlignCenter() ? 0 : (Double.isNaN(getTop()) ? bounds.getHeight() * .15 : getTop()));
    }

    private void finishClose() {
        if (stage == null || !stage.isShowing() || closing) return;
        closing = true;
        fire(getOnClose(), CLOSE);
        Runnable action = () -> play(false, () -> {
            stage.hide();
            showing.set(false);
            closing = false;
            if (isDestroyOnClose()) body.getChildren().clear();
            fire(getOnClosed(), CLOSED);
            if (owner != null) owner.requestFocus();
            fire(getOnCloseAutoFocus(), CLOSE_AUTO_FOCUS);
        });
        if (getCloseDelay() == 0)
            action.run();
        else
            delay(getCloseDelay(), action);
    }

    private void play(boolean opening, Runnable finished) {
        Duration d = Duration.millis(getTransitionDuration());
        if (d.lessThanOrEqualTo(Duration.ZERO)) {
            finished.run();
            return;
        }
        dialog.setOpacity(opening ? 0 : 1);
        dialog.setScaleX(opening ? .9 : 1);
        dialog.setScaleY(opening ? .9 : 1);
        dialog.setTranslateY(dialog.getTranslateY() + (opening && getTransition() == Transition.SLIDE ? -32 : 0));
        FadeTransition fade = new FadeTransition(d, dialog);
        fade.setToValue(opening ? 1 : 0);
        if (getTransition() == Transition.FADE) {
            fade.setOnFinished(e -> finished.run());
            fade.play();
            return;
        }
        javafx.animation.Transition extra;
        if (getTransition() == Transition.SLIDE) {
            TranslateTransition t = new TranslateTransition(d, dialog);
            t.setByY(opening ? 32 : -32);
            extra = t;
        } else {
            ScaleTransition s = new ScaleTransition(d, dialog);
            s.setToX(opening ? 1 : (getTransition() == Transition.BOUNCE ? .3 : .9));
            s.setToY(opening ? 1 : (getTransition() == Transition.BOUNCE ? .3 : .9));
            extra = s;
        }
        ParallelTransition all = new ParallelTransition(fade, extra);
        all.setOnFinished(e -> finished.run());
        all.play();
    }

    private void refreshHeader() {
        headerBox.getChildren().setAll(getHeader() == null ? titleLabel : getHeader());
        if (getHeader() == null) titleLabel.setText(getTitle());
        if (isShowClose()) headerBox.getChildren().add(closeButton);
        boolean present = getHeader() != null || !getTitle().isBlank() || isShowClose();
        headerBox.setVisible(present);
        headerBox.setManaged(present);
        refreshAlignment();
    }

    private void refreshFooter() {
        boolean present = getFooter() != null;
        if (present)
            footerBox.getChildren().setAll(getFooter());
        else
            footerBox.getChildren().clear();
        footerBox.setVisible(present);
        footerBox.setManaged(present);
        refreshAlignment();
    }

    private void refreshCloseIcon() {
        closeButton.setGraphic(getCloseIcon() == null ? defaultCloseIcon() : getCloseIcon());
    }

    private Node defaultCloseIcon() {
        EleFXIcon icon = new EleFXIcon(EleFXIconType.CLOSE, 18);
        icon.setMouseTransparent(true);
        return icon;
    }

    private void refreshAlignment() {
        if (isCenter()) {
            headerBox.setAlignment(Pos.CENTER);
            footerBox.setAlignment(Pos.CENTER);
        } else {
            headerBox.setAlignment(Pos.CENTER_LEFT);
            footerBox.setAlignment(Pos.CENTER_RIGHT);
        }
    }

    private void startDrag(MouseEvent e) {
        if (!isDraggable() || isFullscreen() || e.getTarget() == closeButton) return;
        dragX = e.getScreenX();
        dragY = e.getScreenY();
        dialogX = dialog.getTranslateX();
        dialogY = dialog.getTranslateY();
    }

    private void drag(MouseEvent e) {
        if (!isDraggable() || isFullscreen()) return;
        double x = dialogX + e.getScreenX() - dragX, y = dialogY + e.getScreenY() - dragY;
        if (!isOverflow()) {
            double w = Math.max(dialog.getWidth(), getWidth()), h = dialog.getHeight();
            x = Math.max(-(stage.getWidth() - w) / 2, Math.min((stage.getWidth() - w) / 2, x));
            y = Math.max(0, Math.min(stage.getHeight() - h, y));
        }
        dialog.setTranslateX(x);
        dialog.setTranslateY(y);
    }

    private static void delay(int millis, Runnable action) {
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(millis));
        pause.setOnFinished(e -> action.run());
        pause.play();
    }

    private void focusFirst() {
        closeButton.requestFocus();
        fire(getOnOpenAutoFocus(), OPEN_AUTO_FOCUS);
    }

    private static void fire(EventHandler<Event> handler, EventType<Event> type) {
        if (handler != null) handler.handle(new Event(type));
    }

    private static void applyClasses(Node node, String value, Set<String> applied) {
        node.getStyleClass().removeAll(applied);
        applied.clear();
        for (String c : value.trim().split("\\s+"))
            if (!c.isBlank()) applied.add(c);
        node.getStyleClass().addAll(applied);
    }
}
