package com.sjydvlp.elefx.component.image;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.util.Collection;

/** Standalone preview overlay. It owns all preview state and its window lifecycle. */
public final class EleFXImageViewer {

    public static final EventType<Event> SHOW = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_SHOW");

    public static final EventType<Event> CLOSE = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_CLOSE");

    public static final EventType<Event> SWITCH = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_SWITCH");

    private final ObservableList<String> urlList = FXCollections.observableArrayList();

    private final IntegerProperty initialIndex = new SimpleIntegerProperty(this, "initialIndex", 0);

    private final BooleanProperty showProgress = new SimpleBooleanProperty(this, "showProgress", false);

    private final BooleanProperty infinite = new SimpleBooleanProperty(this, "infinite", true);

    private final BooleanProperty closeOnPressEscape = new SimpleBooleanProperty(this, "closeOnPressEscape", true);

    private final ObjectProperty<Node> errorNode = new SimpleObjectProperty<>(this, "errorNode");

    private final ObjectProperty<EventHandler<Event>> onShow = new SimpleObjectProperty<>(this, "onShow");

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final ObjectProperty<EventHandler<Event>> onSwitch = new SimpleObjectProperty<>(this, "onSwitch");

    private Stage stage;

    private ImageView image;

    private StackPane content;

    private Label progress;

    private final Label defaultError = new Label("Failed to load image");

    private EleFXIcon sizeModeIcon;

    private int index;

    private boolean error;

    private boolean originalSize;

    public EleFXImageViewer() {
    }

    public EleFXImageViewer(Collection<String> values) {
        setUrlList(values);
    }

    public ObservableList<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(Collection<String> values) {
        urlList.setAll(values == null ? FXCollections.emptyObservableList() : values);
        if (isShowing()) refresh();
    }

    public void setPreviewSrcList(Collection<String> values) {
        setUrlList(values);
    }

    public int getInitialIndex() {
        return initialIndex.get();
    }

    public IntegerProperty initialIndexProperty() {
        return initialIndex;
    }

    public void setInitialIndex(int value) {
        initialIndex.set(Math.max(0, value));
    }

    public boolean isShowProgress() {
        return showProgress.get();
    }

    public BooleanProperty showProgressProperty() {
        return showProgress;
    }

    public void setShowProgress(boolean value) {
        showProgress.set(value);
        updateProgress();
    }

    public boolean isInfinite() {
        return infinite.get();
    }

    public BooleanProperty infiniteProperty() {
        return infinite;
    }

    public void setInfinite(boolean value) {
        infinite.set(value);
    }

    public boolean isCloseOnPressEscape() {
        return closeOnPressEscape.get();
    }

    public BooleanProperty closeOnPressEscapeProperty() {
        return closeOnPressEscape;
    }

    public void setCloseOnPressEscape(boolean value) {
        closeOnPressEscape.set(value);
    }

    /** Content displayed in the preview area when the current image cannot be loaded. */
    public Node getErrorNode() {
        return errorNode.get();
    }

    public ObjectProperty<Node> errorNodeProperty() {
        return errorNode;
    }

    public void setErrorNode(Node value) {
        errorNode.set(value);
        if (error) showError();
    }

    public EventHandler<Event> getOnShow() {
        return onShow.get();
    }

    public ObjectProperty<EventHandler<Event>> onShowProperty() {
        return onShow;
    }

    public void setOnShow(EventHandler<Event> value) {
        onShow.set(value);
    }

    public EventHandler<Event> getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<EventHandler<Event>> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(EventHandler<Event> value) {
        onClose.set(value);
    }

    public EventHandler<Event> getOnSwitch() {
        return onSwitch.get();
    }

    public ObjectProperty<EventHandler<Event>> onSwitchProperty() {
        return onSwitch;
    }

    public void setOnSwitch(EventHandler<Event> value) {
        onSwitch.set(value);
    }

    public void show() {
        show(null, null);
    }

    /** Displays this viewer over the supplied owner window. */
    public void show(Window owner) {
        show(owner, null);
    }

    public void showPreview() {
        show();
    }

    void show(Window owner, String fallback) {
        if (urlList.isEmpty() && fallback != null && !fallback.isBlank()) urlList.setAll(fallback);
        if (urlList.isEmpty()) return;
        index = Math.min(getInitialIndex(), urlList.size() - 1);
        if (stage == null) create(owner);
        refresh();
        fitOwner(owner);
        stage.show();
        stage.toFront();
        fire(onShow.get(), SHOW);
    }

    public void hide() {
        close();
    }

    public void close() {
        if (stage != null) stage.hide();
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }

    private void create(Window owner) {
        stage = new Stage();
        if (owner != null) stage.initOwner(owner);
        stage.initStyle(StageStyle.TRANSPARENT);
        image = new ImageView();
        image.setPreserveRatio(true);
        image.setSmooth(true);
        defaultError.getStyleClass().add("ele-image-viewer__error");
        content = new StackPane(image);
        content.getStyleClass().add("ele-image-viewer");
        content.setMinSize(0, 0);
        content.widthProperty().addListener(o -> sizeImage());
        content.heightProperty().addListener(o -> sizeImage());
        Button previous = button(EleFXIconType.ARROW_LEFT, "Previous image", "ele-image-viewer__nav");
        Button next = button(EleFXIconType.ARROW_RIGHT, "Next image", "ele-image-viewer__nav");
        previous.getStyleClass().add("ele-image-viewer__nav--previous");
        next.getStyleClass().add("ele-image-viewer__nav--next");
        Button zoomOut = button(EleFXIconType.ZOOM_OUT, "Zoom out", "ele-image-viewer__action");
        Button zoomIn = button(EleFXIconType.ZOOM_IN, "Zoom in", "ele-image-viewer__action");
        Button sizeMode = button(EleFXIconType.FULL_SCREEN, "Show original size", "ele-image-viewer__action");
        sizeModeIcon = (EleFXIcon) sizeMode.getGraphic();
        Button rotateLeft = button(EleFXIconType.REFRESH_LEFT, "Rotate left", "ele-image-viewer__action");
        Button rotateRight = button(EleFXIconType.REFRESH_RIGHT, "Rotate right", "ele-image-viewer__action");
        Button close = button(EleFXIconType.CLOSE, "Close preview", "ele-image-viewer__close");
        previous.setOnAction(e -> move(-1));
        next.setOnAction(e -> move(1));
        zoomOut.setOnAction(e -> zoom(1 / 1.2));
        zoomIn.setOnAction(e -> zoom(1.2));
        sizeMode.setOnAction(e -> toggleSizeMode(sizeMode));
        rotateLeft.setOnAction(e -> image.setRotate(image.getRotate() - 90));
        rotateRight.setOnAction(e -> image.setRotate(image.getRotate() + 90));
        close.setOnAction(e -> close());
        progress = new Label();
        progress.getStyleClass().add("ele-image-viewer__progress");
        HBox toolbar = new HBox(4, zoomOut, zoomIn, sizeMode, rotateLeft, rotateRight);
        toolbar.getStyleClass().add("ele-image-viewer__toolbar");
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setMaxWidth(Region.USE_PREF_SIZE);
        VBox footer = new VBox(14, progress, toolbar);
        footer.setAlignment(Pos.CENTER);
        footer.setFillWidth(false);
        footer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane root = new StackPane(content, footer, previous, next);
        root.getStyleClass().add("ele-image-viewer__root");
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        StackPane overlay = new StackPane(root, close);
        overlay.getStyleClass().add("ele-image-viewer__overlay");
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        Scene scene = new Scene(overlay, Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.IMAGE.toData());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && isCloseOnPressEscape())
                close();
            else if (e.getCode() == KeyCode.LEFT)
                move(-1);
            else if (e.getCode() == KeyCode.RIGHT) move(1);
        });
        scene.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() != 0) {
                zoom(e.getDeltaY() > 0 ? 1.1 : 1 / 1.1);
                e.consume();
            }
        });
        stage.setScene(scene);
        stage.setOnHidden(e -> fire(onClose.get(), CLOSE));
    }

    private Button button(EleFXIconType type, String accessibleText, String style) {
        EleFXIcon icon = new EleFXIcon(type, 22);
        icon.setFill(Color.WHITE);
        icon.setMouseTransparent(true);
        Button result = new Button();
        result.setGraphic(icon);
        result.setAccessibleText(accessibleText);
        result.getStyleClass().add(style);
        return result;
    }

    private void fitOwner(Window owner) {
        if (owner != null) {
            stage.setX(owner.getX());
            stage.setY(owner.getY());
            stage.setWidth(owner.getWidth());
            stage.setHeight(owner.getHeight());
            return;
        }
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }

    private void zoom(double factor) {
        double scale = Math.max(image.getScaleX(), image.getScaleY()) * factor;
        scale = Math.max(.2, Math.min(7, scale));
        image.setScaleX(scale);
        image.setScaleY(scale);
    }

    private void toggleSizeMode(Button button) {
        originalSize = !originalSize;
        sizeImage();
        image.setScaleX(1);
        image.setScaleY(1);
        sizeModeIcon.setType(originalSize ? EleFXIconType.SCALE_TO_ORIGINAL : EleFXIconType.FULL_SCREEN);
        button.setAccessibleText(originalSize ? "Fit image to preview" : "Show original size");
    }

    private void move(int amount) {
        if (urlList.isEmpty()) return;
        int target = index + amount;
        target = isInfinite()
                ? Math.floorMod(target, urlList.size())
                : Math.max(0, Math.min(target, urlList.size() - 1));
        if (target != index) {
            index = target;
            refresh();
            fire(onSwitch.get(), SWITCH);
        }
    }

    private void refresh() {
        if (urlList.isEmpty()) {
            close();
            return;
        }
        index = Math.min(index, urlList.size() - 1);
        error = false;
        showImage();
        originalSize = false;
        if (sizeModeIcon != null) sizeModeIcon.setType(EleFXIconType.FULL_SCREEN);
        image.setScaleX(1);
        image.setScaleY(1);
        image.setRotate(0);
        try {
            Image candidate = new Image(urlList.get(index), true);
            candidate.errorProperty().addListener((o, old, failed) -> {
                if (failed && image.getImage() == candidate) {
                    error = true;
                    showError();
                    updateProgress();
                }
            });
            candidate.progressProperty().addListener((o, old, value) -> sizeImage());
            image.setImage(candidate);
            error = candidate.isError();
            if (error) showError();
            sizeImage();
            updateProgress();
        } catch (IllegalArgumentException exception) {
            image.setImage(null);
            error = true;
            showError();
            updateProgress();
        }
    }

    private void updateProgress() {
        if (progress == null) return;
        boolean visible = isShowProgress();
        progress.setVisible(visible);
        progress.setManaged(visible);
        if (visible) progress.setText((index + 1) + " / " + urlList.size());
    }

    private void showImage() {
        if (content != null && (content.getChildren().size() != 1 || content.getChildren().get(0) != image))
            content.getChildren().setAll(image);
    }

    private void showError() {
        if (content == null) return;
        Node fallback = getErrorNode() == null ? defaultError : getErrorNode();
        content.getChildren().setAll(fallback);
    }

    private void sizeImage() {
        if (image == null || content == null || image.getImage() == null) return;
        double width = content.getWidth(), height = content.getHeight();
        Image value = image.getImage();
        if (originalSize) {
            image.setFitWidth(0);
            image.setFitHeight(0);
            return;
        }
        if (width <= 0 || height <= 0) return;
        double scale = Math.min(width / value.getWidth(), height / value.getHeight());
        image.setFitWidth(value.getWidth() * Math.min(1, scale));
        image.setFitHeight(value.getHeight() * Math.min(1, scale));
    }

    private void fire(EventHandler<Event> handler, EventType<Event> type) {
        if (handler != null) handler.handle(new Event(this, null, type));
    }
}
