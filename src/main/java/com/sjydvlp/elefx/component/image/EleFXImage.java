package com.sjydvlp.elefx.component.image;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * An Element Plus-inspired image view with loading/error fallbacks and an optional preview.
 * Custom placeholder and error nodes are JavaFX equivalents of Element Plus's named slots.
 */
public class EleFXImage extends StackPane implements Themable {

    public static final EventType<Event> IMAGE_LOAD = new EventType<>(Event.ANY, "ELEFX_IMAGE_LOAD");

    public static final EventType<Event> IMAGE_ERROR = new EventType<>(Event.ANY, "ELEFX_IMAGE_ERROR");

    public static final EventType<Event> PREVIEW_SHOW = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_SHOW");

    public static final EventType<Event> PREVIEW_CLOSE = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_CLOSE");

    public static final EventType<Event> PREVIEW_SWITCH = new EventType<>(Event.ANY, "ELEFX_IMAGE_PREVIEW_SWITCH");

    private final StringProperty src = new SimpleStringProperty(this, "src", "");

    private final StringProperty alt = new SimpleStringProperty(this, "alt", "");

    private final ObjectProperty<EleFXImageFit> fit = new SimpleObjectProperty<>(this, "fit", EleFXImageFit.FILL);

    private final BooleanProperty lazy = new SimpleBooleanProperty(this, "lazy", false);

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);

    private final BooleanProperty imageError = new SimpleBooleanProperty(this, "imageError", false);

    private final ObjectProperty<Node> placeholderNode = new SimpleObjectProperty<>(this, "placeholderNode");

    private final ObjectProperty<Node> errorNode = new SimpleObjectProperty<>(this, "errorNode");

    private final ObservableList<String> previewSrcList = FXCollections.observableArrayList();

    private final IntegerProperty initialIndex = new SimpleIntegerProperty(this, "initialIndex", 0);

    private final BooleanProperty infinite = new SimpleBooleanProperty(this, "infinite", true);

    private final BooleanProperty closeOnPressEscape = new SimpleBooleanProperty(this, "closeOnPressEscape", true);

    private final ObjectProperty<EventHandler<Event>> onLoad = new SimpleObjectProperty<>(this, "onLoad");

    private final ObjectProperty<EventHandler<Event>> onError = new SimpleObjectProperty<>(this, "onError");

    private final ObjectProperty<EventHandler<Event>> onShow = new SimpleObjectProperty<>(this, "onShow");

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final ObjectProperty<EventHandler<Event>> onSwitch = new SimpleObjectProperty<>(this, "onSwitch");

    private final ImageView imageView = new ImageView();

    private final StackPane fallbackBox = new StackPane();

    private final ProgressIndicator defaultPlaceholder = new ProgressIndicator();

    private final Label defaultError = new Label("Failed to load image");

    private final Rectangle clip = new Rectangle();

    private Image currentImage;

    private Stage previewStage;

    private ImageView previewImage;

    private Label previewProgress;

    private int previewIndex;

    public EleFXImage() {
        initialize();
    }

    public EleFXImage(String src) {
        setSrc(src);
        initialize();
    }

    public String getSrc() {
        return src.get();
    }

    public StringProperty srcProperty() {
        return src;
    }

    public void setSrc(String value) {
        src.set(value == null ? "" : value.trim());
    }

    public String getAlt() {
        return alt.get();
    }

    public StringProperty altProperty() {
        return alt;
    }

    public void setAlt(String value) {
        alt.set(value == null ? "" : value);
    }

    public EleFXImageFit getFit() {
        return fit.get();
    }

    public ObjectProperty<EleFXImageFit> fitProperty() {
        return fit;
    }

    public void setFit(EleFXImageFit value) {
        fit.set(value == null ? EleFXImageFit.FILL : value);
    }

    public boolean isLazy() {
        return lazy.get();
    }

    public BooleanProperty lazyProperty() {
        return lazy;
    }

    /** When true, source loading is deferred until {@link #requestLoad()} is called. */
    public void setLazy(boolean value) {
        lazy.set(value);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isImageError() {
        return imageError.get();
    }

    public BooleanProperty imageErrorProperty() {
        return imageError;
    }

    public Node getPlaceholderNode() {
        return placeholderNode.get();
    }

    public ObjectProperty<Node> placeholderNodeProperty() {
        return placeholderNode;
    }

    public void setPlaceholderNode(Node value) {
        placeholderNode.set(value);
    }

    public Node getErrorNode() {
        return errorNode.get();
    }

    public ObjectProperty<Node> errorNodeProperty() {
        return errorNode;
    }

    public void setErrorNode(Node value) {
        errorNode.set(value);
    }

    public ObservableList<String> getPreviewSrcList() {
        return previewSrcList;
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

    public EventHandler<Event> getOnLoad() {
        return onLoad.get();
    }

    public ObjectProperty<EventHandler<Event>> onLoadProperty() {
        return onLoad;
    }

    public void setOnLoad(EventHandler<Event> value) {
        onLoad.set(value);
    }

    public EventHandler<Event> getOnError() {
        return onError.get();
    }

    public ObjectProperty<EventHandler<Event>> onErrorProperty() {
        return onError;
    }

    public void setOnError(EventHandler<Event> value) {
        onError.set(value);
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

    public ImageView getImageView() {
        return imageView;
    }

    /** Starts deferred loading. It is safe to call even when lazy loading is disabled. */
    public void requestLoad() {
        loadImage();
    }

    /** Opens the preview window when a preview source list or source is available. */
    public void showPreview() {
        ObservableList<String> sources = previewSources();
        if (sources.isEmpty()) return;
        previewIndex = Math.min(getInitialIndex(), sources.size() - 1);
        if (previewStage == null) createPreview();
        updatePreview();
        previewStage.show();
        previewStage.toFront();
        fireEvent(new Event(this, this, PREVIEW_SHOW));
    }

    public void closePreview() {
        if (previewStage != null) previewStage.hide();
    }

    public boolean isPreviewShowing() {
        return previewStage != null && previewStage.isShowing();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.IMAGE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-image");
        setAlignment(Pos.CENTER);
        setClip(clip);
        setFocusTraversable(true);
        imageView.getStyleClass().add("ele-image__inner");
        imageView.setSmooth(true);
        fallbackBox.getStyleClass().add("ele-image__fallback");
        defaultPlaceholder.getStyleClass().add("ele-image__placeholder");
        defaultError.getStyleClass().add("ele-image__error");
        widthProperty().addListener(o -> updateGeometry());
        heightProperty().addListener(o -> updateGeometry());
        src.addListener(o -> {
            if (!isLazy())
                loadImage();
            else
                showFallback();
        });
        alt.addListener(o -> setAccessibleText(getAlt()));
        fit.addListener(o -> updateGeometry());
        placeholderNode.addListener(o -> showFallback());
        errorNode.addListener(o -> showFallback());
        onLoad.addListener((o, a, b) -> setEventHandler(IMAGE_LOAD, b));
        onError.addListener((o, a, b) -> setEventHandler(IMAGE_ERROR, b));
        onShow.addListener((o, a, b) -> setEventHandler(PREVIEW_SHOW, b));
        onClose.addListener((o, a, b) -> setEventHandler(PREVIEW_CLOSE, b));
        onSwitch.addListener((o, a, b) -> setEventHandler(PREVIEW_SWITCH, b));
        setOnMouseClicked(e -> {
            if (!e.isConsumed() && !previewSources().isEmpty()) showPreview();
        });
        setAccessibleText(getAlt());
        if (!isLazy())
            loadImage();
        else
            showFallback();
        sceneBuilderIntegration();
    }

    private void loadImage() {
        imageError.set(false);
        imageView.setImage(null);
        String source = getSrc();
        if (source.isBlank()) {
            loading.set(false);
            showFallback();
            return;
        }
        loading.set(true);
        showFallback();
        try {
            currentImage = new Image(source, true);
        } catch (IllegalArgumentException exception) {
            fail(null);
            return;
        }
        Image candidate = currentImage;
        candidate.errorProperty().addListener((o, old, failed) -> {
            if (failed && currentImage == candidate) fail(candidate);
        });
        candidate.progressProperty().addListener((o, old, progress) -> {
            if (currentImage == candidate && progress.doubleValue() >= 1 && !candidate.isError()) succeed(candidate);
        });
        if (!candidate.isBackgroundLoading() && !candidate.isError())
            succeed(candidate);
        else if (candidate.isError()) fail(candidate);
    }

    private void succeed(Image image) {
        if (currentImage != image) return;
        loading.set(false);
        imageError.set(false);
        imageView.setImage(image);
        getChildren().setAll(imageView);
        updateGeometry();
        fireEvent(new Event(this, this, IMAGE_LOAD));
    }

    private void fail(Image image) {
        if (image != null && currentImage != image) return;
        loading.set(false);
        imageError.set(true);
        imageView.setImage(null);
        showFallback();
        fireEvent(new Event(this, this, IMAGE_ERROR));
    }

    private void showFallback() {
        Node node = isImageError() ? getErrorNode() : getPlaceholderNode();
        if (node == null) node = isImageError() ? defaultError : defaultPlaceholder;
        fallbackBox.getChildren().setAll(node);
        getChildren().setAll(fallbackBox);
    }

    private void updateGeometry() {
        clip.setWidth(getWidth());
        clip.setHeight(getHeight());
        Image image = imageView.getImage();
        if (image == null || getWidth() <= 0 || getHeight() <= 0) return;
        double w = image.getWidth(), h = image.getHeight(), boxW = getWidth(), boxH = getHeight();
        if (w <= 0 || h <= 0) return;
        switch (getFit()) {
            case FILL -> {
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(boxW);
                imageView.setFitHeight(boxH);
            }
            case CONTAIN -> {
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(boxW);
                imageView.setFitHeight(boxH);
            }
            case COVER -> {
                imageView.setPreserveRatio(false);
                if (w / h > boxW / boxH) {
                    imageView.setFitHeight(boxH);
                    imageView.setFitWidth(boxH * w / h);
                } else {
                    imageView.setFitWidth(boxW);
                    imageView.setFitHeight(boxW * h / w);
                }
            }
            case NONE -> {
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(w);
                imageView.setFitHeight(h);
            }
            case SCALE_DOWN -> {
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(w <= boxW && h <= boxH ? w : boxW);
                imageView.setFitHeight(h <= boxH && w <= boxW ? h : boxH);
            }
        }
    }

    private ObservableList<String> previewSources() {
        if (!previewSrcList.isEmpty()) return previewSrcList;
        return getSrc().isBlank() ? FXCollections.observableArrayList() : FXCollections.observableArrayList(getSrc());
    }

    private void createPreview() {
        previewStage = new Stage();
        Window owner = getScene() == null ? null : getScene().getWindow();
        if (owner != null) previewStage.initOwner(owner);
        previewStage.initModality(Modality.NONE);
        previewStage.setTitle(getAlt().isBlank() ? "Image preview" : getAlt());
        previewImage = new ImageView();
        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);
        previewImage.setFitWidth(900);
        previewImage.setFitHeight(650);
        Button previous = new Button("‹"), next = new Button("›"), zoomOut = new Button("−"), zoomIn = new Button("+");
        Button close = new Button("×");
        previous.setOnAction(e -> movePreview(-1));
        next.setOnAction(e -> movePreview(1));
        zoomOut.setOnAction(e -> previewImage.setScaleX(previewImage.getScaleX() / 1.2));
        zoomOut.setOnAction(e -> previewImage.setScaleY(previewImage.getScaleY() / 1.2));
        zoomIn.setOnAction(e -> previewImage.setScaleX(previewImage.getScaleX() * 1.2));
        zoomIn.setOnAction(e -> previewImage.setScaleY(previewImage.getScaleY() * 1.2));
        close.setOnAction(e -> closePreview());
        HBox controls = new HBox(8, previous, next, zoomOut, zoomIn, close);
        controls.getStyleClass().add("ele-image-viewer__toolbar");
        controls.setAlignment(Pos.CENTER);
        previewProgress = new Label();
        previewProgress.getStyleClass().add("ele-image-viewer__progress");
        VBox content = new VBox(12, previewImage, previewProgress, controls);
        content.getStyleClass().add("ele-image-viewer");
        content.setAlignment(Pos.CENTER);
        BorderPane root = new BorderPane(content);
        root.getStyleClass().add("ele-image-viewer__root");
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && isCloseOnPressEscape())
                closePreview();
            else if (e.getCode() == KeyCode.LEFT)
                movePreview(-1);
            else if (e.getCode() == KeyCode.RIGHT) movePreview(1);
        });
        previewStage.setScene(scene);
        previewStage.setOnHidden(e -> fireEvent(new Event(this, this, PREVIEW_CLOSE)));
    }

    private void movePreview(int change) {
        ObservableList<String> sources = previewSources();
        if (sources.isEmpty()) return;
        int target = previewIndex + change;
        if (isInfinite())
            target = Math.floorMod(target, sources.size());
        else
            target = Math.max(0, Math.min(target, sources.size() - 1));
        if (target != previewIndex) {
            previewIndex = target;
            updatePreview();
            fireEvent(new Event(this, this, PREVIEW_SWITCH));
        }
    }

    private void updatePreview() {
        ObservableList<String> sources = previewSources();
        if (sources.isEmpty()) return;
        String source = sources.get(previewIndex);
        try {
            previewImage.setImage(new Image(source, true));
            previewImage.setScaleX(1);
            previewImage.setScaleY(1);
            previewProgress.setText((previewIndex + 1) + " / " + sources.size());
        } catch (IllegalArgumentException exception) {
            previewImage.setImage(null);
            previewProgress.setText("Failed to load image");
        }
    }
}
