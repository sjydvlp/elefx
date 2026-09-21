package com.sjydvlp.elefx.component.drawer;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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
 * An Element Plus-inspired, window-owned sliding panel.
 *
 * <p>
 * Call {@link #show(Window)} to show the drawer over an owner. Add normal content through
 * {@link #getBodyChildren()}, or replace the title/header/footer areas with the corresponding
 * properties. {@code size} accepts Element Plus-style values such as {@code "30%"} and
 * {@code "420px"}.
 * </p>
 */
public final class EleFXDrawer {

    public static final EventType<Event> OPEN = new EventType<>(Event.ANY, "ELEFX_DRAWER_OPEN");

    public static final EventType<Event> OPENED = new EventType<>(Event.ANY, "ELEFX_DRAWER_OPENED");

    public static final EventType<Event> CLOSE = new EventType<>(Event.ANY, "ELEFX_DRAWER_CLOSE");

    public static final EventType<Event> CLOSED = new EventType<>(Event.ANY, "ELEFX_DRAWER_CLOSED");

    public static final EventType<Event> OPEN_AUTO_FOCUS = new EventType<>(Event.ANY, "ELEFX_DRAWER_OPEN_AUTO_FOCUS");

    public static final EventType<Event> CLOSE_AUTO_FOCUS = new EventType<>(Event.ANY, "ELEFX_DRAWER_CLOSE_AUTO_FOCUS");

    public static final EventType<Event> RESIZE_START = new EventType<>(Event.ANY, "ELEFX_DRAWER_RESIZE_START");

    public static final EventType<Event> RESIZE = new EventType<>(Event.ANY, "ELEFX_DRAWER_RESIZE");

    public static final EventType<Event> RESIZE_END = new EventType<>(Event.ANY, "ELEFX_DRAWER_RESIZE_END");

    @FunctionalInterface
    public interface BeforeCloseHandler {

        void handle(Runnable done);
    }

    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", false);

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty size = new SimpleStringProperty(this, "size", "30%");

    private final ObjectProperty<EleFXDrawerDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXDrawerDirection.RTL);

    private final BooleanProperty modal = new SimpleBooleanProperty(this, "modal", true);

    private final BooleanProperty modalPenetrable = new SimpleBooleanProperty(this, "modalPenetrable", false);

    private final BooleanProperty lockScroll = new SimpleBooleanProperty(this, "lockScroll", true);

    private final BooleanProperty closeOnClickModal = new SimpleBooleanProperty(this, "closeOnClickModal", true);

    private final BooleanProperty closeOnPressEscape = new SimpleBooleanProperty(this, "closeOnPressEscape", true);

    private final BooleanProperty showClose = new SimpleBooleanProperty(this, "showClose", true);

    private final BooleanProperty withHeader = new SimpleBooleanProperty(this, "withHeader", true);

    private final BooleanProperty destroyOnClose = new SimpleBooleanProperty(this, "destroyOnClose", false);

    private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", false);

    private final IntegerProperty openDelay = new SimpleIntegerProperty(this, "openDelay", 0);

    private final IntegerProperty closeDelay = new SimpleIntegerProperty(this, "closeDelay", 0);

    private final IntegerProperty transitionDuration = new SimpleIntegerProperty(this, "transitionDuration", 300);

    private final IntegerProperty zIndex = new SimpleIntegerProperty(this, "zIndex", 2000);

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");

    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer");

    private final ObjectProperty<Node> closeIcon = new SimpleObjectProperty<>(this, "closeIcon");

    private final ObjectProperty<BeforeCloseHandler> beforeClose = new SimpleObjectProperty<>(this, "beforeClose");

    private final StringProperty headerClass = new SimpleStringProperty(this, "headerClass", "");

    private final StringProperty bodyClass = new SimpleStringProperty(this, "bodyClass", "");

    private final StringProperty footerClass = new SimpleStringProperty(this, "footerClass", "");

    private final StringProperty modalClass = new SimpleStringProperty(this, "modalClass", "");

    private final ObjectProperty<EventHandler<Event>> onOpen = new SimpleObjectProperty<>(this, "onOpen");

    private final ObjectProperty<EventHandler<Event>> onOpened = new SimpleObjectProperty<>(this, "onOpened");

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final ObjectProperty<EventHandler<Event>> onClosed = new SimpleObjectProperty<>(this, "onClosed");

    private final ObjectProperty<EventHandler<Event>> onOpenAutoFocus = new SimpleObjectProperty<>(this,
            "onOpenAutoFocus");

    private final ObjectProperty<EventHandler<Event>> onCloseAutoFocus = new SimpleObjectProperty<>(this,
            "onCloseAutoFocus");

    private final ObjectProperty<EventHandler<Event>> onResizeStart = new SimpleObjectProperty<>(this, "onResizeStart");

    private final ObjectProperty<EventHandler<Event>> onResize = new SimpleObjectProperty<>(this, "onResize");

    private final ObjectProperty<EventHandler<Event>> onResizeEnd = new SimpleObjectProperty<>(this, "onResizeEnd");

    private final VBox body = new VBox();

    private final HBox headerBox = new HBox();

    private final HBox footerBox = new HBox();

    private final Label titleLabel = new Label();

    private final Button closeButton = new Button();

    private final Set<String> appliedHeaderClasses = new HashSet<>(), appliedBodyClasses = new HashSet<>(),
            appliedFooterClasses = new HashSet<>();

    private Stage stage;

    private StackPane overlay;

    private VBox drawer;

    private Region resizeHandle;

    private Window owner;

    private boolean closing;

    private double resizeStartCoordinate, resizeStartSize;

    private final Set<String> appliedModalClasses = new HashSet<>();

    public EleFXDrawer() {
        initialize();
    }

    public EleFXDrawer(Node... content) {
        this();
        body.getChildren().addAll(content);
    }

    public void show() {
        show(null);
    }

    public void show(Window value) {
        owner = value;
        if (isShowing()) {
            stage.toFront();
            return;
        }
        setShowing(true);
    }

    public void hide() {
        close();
    }

    /** Closes immediately, bypassing the optional user-close guard. */
    public void close() {
        finishClose();
    }

    /** Requests a user-close and invokes {@link #getBeforeClose()} when one is installed. */
    public void requestClose() {
        if (stage == null || !stage.isShowing() || closing) return;
        var guard = getBeforeClose();
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

    public BooleanProperty showingProperty() {
        return showing;
    }

    public boolean isShowingPropertyValue() {
        return showing.get();
    }

    public void setShowing(boolean v) {
        showing.set(v);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String v) {
        title.set(v == null ? "" : v);
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public String getSize() {
        return size.get();
    }

    public void setSize(String v) {
        size.set(v == null || v.isBlank() ? "30%" : v.trim());
    }

    public void setSize(double v) {
        setSize(v + "px");
    }

    public ObjectProperty<EleFXDrawerDirection> directionProperty() {
        return direction;
    }

    public EleFXDrawerDirection getDirection() {
        return direction.get();
    }

    public void setDirection(EleFXDrawerDirection v) {
        direction.set(v == null ? EleFXDrawerDirection.RTL : v);
    }

    public BooleanProperty modalProperty() {
        return modal;
    }

    public boolean isModal() {
        return modal.get();
    }

    public void setModal(boolean v) {
        modal.set(v);
    }

    public BooleanProperty modalPenetrableProperty() {
        return modalPenetrable;
    }

    public boolean isModalPenetrable() {
        return modalPenetrable.get();
    }

    public void setModalPenetrable(boolean v) {
        modalPenetrable.set(v);
    }

    public BooleanProperty lockScrollProperty() {
        return lockScroll;
    }

    public boolean isLockScroll() {
        return lockScroll.get();
    }

    public void setLockScroll(boolean v) {
        lockScroll.set(v);
    }

    public BooleanProperty closeOnClickModalProperty() {
        return closeOnClickModal;
    }

    public boolean isCloseOnClickModal() {
        return closeOnClickModal.get();
    }

    public void setCloseOnClickModal(boolean v) {
        closeOnClickModal.set(v);
    }

    public BooleanProperty closeOnPressEscapeProperty() {
        return closeOnPressEscape;
    }

    public boolean isCloseOnPressEscape() {
        return closeOnPressEscape.get();
    }

    public void setCloseOnPressEscape(boolean v) {
        closeOnPressEscape.set(v);
    }

    public BooleanProperty showCloseProperty() {
        return showClose;
    }

    public boolean isShowClose() {
        return showClose.get();
    }

    public void setShowClose(boolean v) {
        showClose.set(v);
    }

    public BooleanProperty withHeaderProperty() {
        return withHeader;
    }

    public boolean isWithHeader() {
        return withHeader.get();
    }

    public void setWithHeader(boolean v) {
        withHeader.set(v);
    }

    public BooleanProperty destroyOnCloseProperty() {
        return destroyOnClose;
    }

    public boolean isDestroyOnClose() {
        return destroyOnClose.get();
    }

    public void setDestroyOnClose(boolean v) {
        destroyOnClose.set(v);
    }

    public BooleanProperty resizableProperty() {
        return resizable;
    }

    public boolean isResizable() {
        return resizable.get();
    }

    public void setResizable(boolean v) {
        resizable.set(v);
    }

    public IntegerProperty openDelayProperty() {
        return openDelay;
    }

    public int getOpenDelay() {
        return openDelay.get();
    }

    public void setOpenDelay(int v) {
        openDelay.set(Math.max(0, v));
    }

    public IntegerProperty closeDelayProperty() {
        return closeDelay;
    }

    public int getCloseDelay() {
        return closeDelay.get();
    }

    public void setCloseDelay(int v) {
        closeDelay.set(Math.max(0, v));
    }

    public IntegerProperty transitionDurationProperty() {
        return transitionDuration;
    }

    public int getTransitionDuration() {
        return transitionDuration.get();
    }

    public void setTransitionDuration(int v) {
        transitionDuration.set(Math.max(0, v));
    }

    public IntegerProperty zIndexProperty() {
        return zIndex;
    }

    public int getZIndex() {
        return zIndex.get();
    }

    public void setZIndex(int v) {
        zIndex.set(v);
    }

    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    public Node getHeader() {
        return header.get();
    }

    public void setHeader(Node v) {
        header.set(v);
    }

    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    public Node getFooter() {
        return footer.get();
    }

    public void setFooter(Node v) {
        footer.set(v);
    }

    public ObjectProperty<Node> closeIconProperty() {
        return closeIcon;
    }

    public Node getCloseIcon() {
        return closeIcon.get();
    }

    public void setCloseIcon(Node v) {
        closeIcon.set(v);
    }

    public ObjectProperty<BeforeCloseHandler> beforeCloseProperty() {
        return beforeClose;
    }

    public BeforeCloseHandler getBeforeClose() {
        return beforeClose.get();
    }

    public void setBeforeClose(BeforeCloseHandler v) {
        beforeClose.set(v);
    }

    public StringProperty headerClassProperty() {
        return headerClass;
    }

    public String getHeaderClass() {
        return headerClass.get();
    }

    public void setHeaderClass(String v) {
        headerClass.set(v == null ? "" : v);
    }

    public StringProperty bodyClassProperty() {
        return bodyClass;
    }

    public String getBodyClass() {
        return bodyClass.get();
    }

    public void setBodyClass(String v) {
        bodyClass.set(v == null ? "" : v);
    }

    public StringProperty footerClassProperty() {
        return footerClass;
    }

    public String getFooterClass() {
        return footerClass.get();
    }

    public void setFooterClass(String v) {
        footerClass.set(v == null ? "" : v);
    }

    public StringProperty modalClassProperty() {
        return modalClass;
    }

    public String getModalClass() {
        return modalClass.get();
    }

    public void setModalClass(String v) {
        modalClass.set(v == null ? "" : v);
    }

    public ObjectProperty<EventHandler<Event>> onOpenProperty() {
        return onOpen;
    }

    public EventHandler<Event> getOnOpen() {
        return onOpen.get();
    }

    public void setOnOpen(EventHandler<Event> v) {
        onOpen.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onOpenedProperty() {
        return onOpened;
    }

    public EventHandler<Event> getOnOpened() {
        return onOpened.get();
    }

    public void setOnOpened(EventHandler<Event> v) {
        onOpened.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onCloseProperty() {
        return onClose;
    }

    public EventHandler<Event> getOnClose() {
        return onClose.get();
    }

    public void setOnClose(EventHandler<Event> v) {
        onClose.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onClosedProperty() {
        return onClosed;
    }

    public EventHandler<Event> getOnClosed() {
        return onClosed.get();
    }

    public void setOnClosed(EventHandler<Event> v) {
        onClosed.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onOpenAutoFocusProperty() {
        return onOpenAutoFocus;
    }

    public EventHandler<Event> getOnOpenAutoFocus() {
        return onOpenAutoFocus.get();
    }

    public void setOnOpenAutoFocus(EventHandler<Event> v) {
        onOpenAutoFocus.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onCloseAutoFocusProperty() {
        return onCloseAutoFocus;
    }

    public EventHandler<Event> getOnCloseAutoFocus() {
        return onCloseAutoFocus.get();
    }

    public void setOnCloseAutoFocus(EventHandler<Event> v) {
        onCloseAutoFocus.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onResizeStartProperty() {
        return onResizeStart;
    }

    public EventHandler<Event> getOnResizeStart() {
        return onResizeStart.get();
    }

    public void setOnResizeStart(EventHandler<Event> v) {
        onResizeStart.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onResizeProperty() {
        return onResize;
    }

    public EventHandler<Event> getOnResize() {
        return onResize.get();
    }

    public void setOnResize(EventHandler<Event> v) {
        onResize.set(v);
    }

    public ObjectProperty<EventHandler<Event>> onResizeEndProperty() {
        return onResizeEnd;
    }

    public EventHandler<Event> getOnResizeEnd() {
        return onResizeEnd.get();
    }

    public void setOnResizeEnd(EventHandler<Event> v) {
        onResizeEnd.set(v);
    }

    private void initialize() {
        showing.addListener((o, old, value) -> {
            if (value && !old)
                open();
            else if (!value && old) finishClose();
        });
        body.getStyleClass().add("ele-drawer__body");
        body.setFillWidth(true);
        VBox.setVgrow(body, Priority.ALWAYS);
        headerBox.getStyleClass().add("ele-drawer__header");
        headerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.getStyleClass().add("ele-drawer__footer");
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        titleLabel.getStyleClass().add("ele-drawer__title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        closeButton.getStyleClass().add("ele-drawer__close-btn");
        closeButton.setAccessibleText("Close drawer");
        closeButton.setGraphic(defaultCloseIcon());
        closeButton.setOnAction(e -> requestClose());
        title.addListener(o -> refreshHeader());
        header.addListener(o -> refreshHeader());
        footer.addListener(o -> refreshFooter());
        closeIcon.addListener(o -> refreshCloseIcon());
        showClose.addListener(o -> refreshHeader());
        withHeader.addListener(o -> refreshHeader());
        size.addListener(o -> refitIfShowing());
        direction.addListener(o -> refitIfShowing());
        resizable.addListener(o -> refreshResizeHandle());
        modalClass.addListener(o -> {
            if (overlay != null) applyClasses(overlay, getModalClass(), appliedModalClasses);
        });
        headerClass.addListener(o -> applyClasses(headerBox, getHeaderClass(), appliedHeaderClasses));
        bodyClass.addListener(o -> applyClasses(body, getBodyClass(), appliedBodyClasses));
        footerClass.addListener(o -> applyClasses(footerBox, getFooterClass(), appliedFooterClasses));
        refreshHeader();
        refreshFooter();
    }

    private void open() {
        if (stage != null && stage.isShowing()) return;
        Runnable action = () -> {
            createIfNeeded();
            configureStage();
            fitOwner();
            fire(getOnOpen(), OPEN);
            stage.show();
            play(true, () -> {
                closeButton.requestFocus();
                fire(getOnOpenAutoFocus(), OPEN_AUTO_FOCUS);
                fire(getOnOpened(), OPENED);
            });
        };
        delay(getOpenDelay(), action);
    }

    private void createIfNeeded() {
        if (stage != null) return;
        drawer = new VBox(headerBox, body, footerBox);
        drawer.getStyleClass().add("ele-drawer");
        drawer.setMaxSize(Region.USE_PREF_SIZE, Double.MAX_VALUE);
        resizeHandle = new Region();
        resizeHandle.getStyleClass().add("ele-drawer__resize-handle");
        resizeHandle.addEventFilter(MouseEvent.MOUSE_PRESSED, this::startResize);
        resizeHandle.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::resize);
        resizeHandle.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (isResizable()) fire(getOnResizeEnd(), RESIZE_END);
        });
        StackPane drawerLayer = new StackPane(drawer, resizeHandle);
        overlay = new StackPane(drawerLayer);
        overlay.getStyleClass().add("ele-drawer__wrapper");
        overlay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getTarget() == overlay && isModal() && isCloseOnClickModal()) requestClose();
        });
        Scene scene = new Scene(overlay, Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.DRAWER.toData());
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
        overlay.pseudoClassStateChanged(PseudoClass.getPseudoClass("modal"), isModal());
        overlay.pseudoClassStateChanged(PseudoClass.getPseudoClass("penetrable"), isModalPenetrable());
        applyClasses(overlay, getModalClass(), appliedModalClasses);
        drawer.getStyleClass().removeIf(c -> c.startsWith("ele-drawer--"));
        drawer.getStyleClass().add("ele-drawer--" + getDirection().name().toLowerCase());
        refreshResizeHandle();
        refreshHeader();
        refreshFooter();
    }

    private void fitOwner() {
        var bounds = owner == null
                ? Screen.getPrimary().getVisualBounds()
                : new javafx.geometry.Rectangle2D(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight());
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        boolean horizontal = getDirection() == EleFXDrawerDirection.RTL || getDirection() == EleFXDrawerDirection.LTR;
        double extent = Math.max(100, Math.min(horizontal ? bounds.getWidth() : bounds.getHeight(),
                parseSize(horizontal ? bounds.getWidth() : bounds.getHeight())));
        drawer.setPrefWidth(horizontal ? extent : bounds.getWidth());
        drawer.setPrefHeight(horizontal ? bounds.getHeight() : extent);
        drawer.setMaxWidth(horizontal ? extent : Double.MAX_VALUE);
        drawer.setMaxHeight(horizontal ? Double.MAX_VALUE : extent);
        StackPane.setAlignment(drawer, switch (getDirection()) {
            case RTL -> Pos.CENTER_RIGHT;
            case LTR -> Pos.CENTER_LEFT;
            case TTB -> Pos.TOP_CENTER;
            case BTT -> Pos.BOTTOM_CENTER;
        });
        drawer.setTranslateX(0);
        drawer.setTranslateY(0);
    }

    private void finishClose() {
        if (stage == null || !stage.isShowing() || closing) return;
        closing = true;
        fire(getOnClose(), CLOSE);
        delay(getCloseDelay(), () -> play(false, () -> {
            stage.hide();
            showing.set(false);
            closing = false;
            if (isDestroyOnClose()) body.getChildren().clear();
            fire(getOnClosed(), CLOSED);
            if (owner != null) owner.requestFocus();
            fire(getOnCloseAutoFocus(), CLOSE_AUTO_FOCUS);
        }));
    }

    private void play(boolean opening, Runnable done) {
        Duration duration = Duration.millis(getTransitionDuration());
        if (duration.lessThanOrEqualTo(Duration.ZERO)) {
            drawer.setTranslateX(opening ? 0 : offsetX());
            drawer.setTranslateY(opening ? 0 : offsetY());
            drawer.setOpacity(opening ? 1 : 0);
            done.run();
            return;
        }
        drawer.setOpacity(opening ? 0.9 : 1);
        drawer.setTranslateX(opening ? offsetX() : 0);
        drawer.setTranslateY(opening ? offsetY() : 0);
        TranslateTransition slide = new TranslateTransition(duration, drawer);
        slide.setToX(opening ? 0 : offsetX());
        slide.setToY(opening ? 0 : offsetY());
        FadeTransition fade = new FadeTransition(duration, drawer);
        fade.setToValue(opening ? 1 : 0);
        ParallelTransition all = new ParallelTransition(slide, fade);
        all.setOnFinished(e -> done.run());
        all.play();
    }

    private double offsetX() {
        return switch (getDirection()) {
            case RTL -> drawer.getWidth();
            case LTR -> -drawer.getWidth();
            default -> 0;
        };
    }

    private double offsetY() {
        return switch (getDirection()) {
            case BTT -> drawer.getHeight();
            case TTB -> -drawer.getHeight();
            default -> 0;
        };
    }

    private double parseSize(double available) {
        try {
            String value = getSize().toLowerCase();
            return value.endsWith("%")
                    ? available * Double.parseDouble(value.substring(0, value.length() - 1)) / 100
                    : Double.parseDouble(value.replace("px", "").trim());
        } catch (NumberFormatException ex) {
            return available * .3;
        }
    }

    private void refitIfShowing() {
        if (stage != null && stage.isShowing()) {
            configureStage();
            fitOwner();
        }
    }

    private void refreshResizeHandle() {
        if (resizeHandle == null) return;
        resizeHandle.setVisible(isResizable());
        resizeHandle.setManaged(isResizable());
        resizeHandle.getStyleClass().removeIf(c -> c.startsWith("ele-drawer__resize-handle--"));
        String edge = switch (getDirection()) {
            case RTL -> "left";
            case LTR -> "right";
            case TTB -> "bottom";
            case BTT -> "top";
        };
        resizeHandle.getStyleClass().add("ele-drawer__resize-handle--" + edge);
        StackPane.setAlignment(resizeHandle, switch (getDirection()) {
            case RTL -> Pos.CENTER_LEFT;
            case LTR -> Pos.CENTER_RIGHT;
            case TTB -> Pos.BOTTOM_CENTER;
            case BTT -> Pos.TOP_CENTER;
        });
    }

    private void startResize(MouseEvent event) {
        if (!isResizable()) return;
        resizeStartCoordinate = isHorizontal() ? event.getScreenX() : event.getScreenY();
        resizeStartSize = isHorizontal() ? drawer.getWidth() : drawer.getHeight();
        fire(getOnResizeStart(), RESIZE_START);
        event.consume();
    }

    private void resize(MouseEvent event) {
        if (!isResizable()) return;
        double coordinate = isHorizontal() ? event.getScreenX() : event.getScreenY();
        double delta = coordinate - resizeStartCoordinate;
        if (getDirection() == EleFXDrawerDirection.RTL || getDirection() == EleFXDrawerDirection.BTT) delta = -delta;
        setSize(Math.max(100, resizeStartSize + delta));
        fire(getOnResize(), RESIZE);
        event.consume();
    }

    private boolean isHorizontal() {
        return getDirection() == EleFXDrawerDirection.RTL || getDirection() == EleFXDrawerDirection.LTR;
    }

    private void refreshHeader() {
        boolean present = isWithHeader();
        if (present) {
            headerBox.getChildren().setAll(getHeader() == null ? titleLabel : getHeader());
            if (getHeader() == null) titleLabel.setText(getTitle());
            if (isShowClose()) headerBox.getChildren().add(closeButton);
        }
        headerBox.setVisible(present);
        headerBox.setManaged(present);
    }

    private void refreshFooter() {
        boolean present = getFooter() != null;
        if (present)
            footerBox.getChildren().setAll(getFooter());
        else
            footerBox.getChildren().clear();
        footerBox.setVisible(present);
        footerBox.setManaged(present);
    }

    private void refreshCloseIcon() {
        closeButton.setGraphic(getCloseIcon() == null ? defaultCloseIcon() : getCloseIcon());
    }

    private Node defaultCloseIcon() {
        EleFXIcon icon = new EleFXIcon(EleFXIconType.CLOSE, 18);
        icon.setMouseTransparent(true);
        return icon;
    }

    private static void delay(int millis, Runnable action) {
        if (millis == 0) {
            action.run();
            return;
        }
        PauseTransition pause = new PauseTransition(Duration.millis(millis));
        pause.setOnFinished(e -> action.run());
        pause.play();
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
