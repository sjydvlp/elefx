package com.sjydvlp.elefx.component.image;

import java.util.Collection;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
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
import javafx.geometry.Insets;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    private final BooleanProperty showProgress = new SimpleBooleanProperty(this, "showProgress", false);

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

    private StackPane previewImageContent;

    private Label previewProgress;

    private int previewIndex;

    private boolean previewImageError;

    private boolean previewOriginalSize;

    private EleFXIcon previewSizeModeIcon;

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

    /** Replaces the image sources available to the preview viewer. */
    public void setPreviewSrcList(Collection<String> values) {
        previewSrcList.setAll(values == null ? FXCollections.emptyObservableList() : values);
        if (!isPreviewShowing()) return;
        ObservableList<String> sources = previewSources();
        if (sources.isEmpty()) {
            closePreview();
            return;
        }
        previewIndex = Math.min(previewIndex, sources.size() - 1);
        updatePreview();
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

    /** Whether the preview viewer displays the current image index and total count. */
    public boolean isShowProgress() {
        return showProgress.get();
    }

    public BooleanProperty showProgressProperty() {
        return showProgress;
    }

    public void setShowProgress(boolean value) {
        showProgress.set(value);
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
        fitPreviewToOwner();
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
        // Its fitted size follows this control during layout, so it must not in turn
        // be used by StackPane to calculate this control's preferred size.
        imageView.setManaged(false);
        fallbackBox.getStyleClass().add("ele-image__fallback");
        fallbackBox.setManaged(false);
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
        showProgress.addListener(o -> updatePreviewProgress());
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
        Insets insets = getInsets();
        double x = insets.getLeft();
        double y = insets.getTop();
        double boxW = Math.max(0, getWidth() - x - insets.getRight());
        double boxH = Math.max(0, getHeight() - y - insets.getBottom());
        fallbackBox.resizeRelocate(x, y, boxW, boxH);
        Image image = imageView.getImage();
        if (image == null || boxW <= 0 || boxH <= 0) return;
        double w = image.getWidth(), h = image.getHeight();
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
        imageView.relocate(x + (boxW - imageView.getBoundsInLocal().getWidth()) / 2,
                y + (boxH - imageView.getBoundsInLocal().getHeight()) / 2);
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return snappedTopInset() + snappedBottomInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        Image image = imageView.getImage();
        double contentWidth = image == null ? fallbackBox.prefWidth(-1) : image.getWidth();
        return snappedLeftInset() + snappedRightInset() + contentWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        Image image = imageView.getImage();
        double contentHeight = image == null ? fallbackBox.prefHeight(-1) : image.getHeight();
        return snappedTopInset() + snappedBottomInset() + contentHeight;
    }

    @Override
    protected void layoutChildren() {
        updateGeometry();
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
        previewStage.initStyle(StageStyle.TRANSPARENT);
        previewImage = new ImageView();
        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);
        previewImage.setFitWidth(900);
        previewImage.setFitHeight(650);
        Button previous = previewButton(EleFXIconType.ARROW_LEFT, "Previous image", "ele-image-viewer__nav");
        Button next = previewButton(EleFXIconType.ARROW_RIGHT, "Next image", "ele-image-viewer__nav");
        Button zoomOut = previewButton(EleFXIconType.ZOOM_OUT, "Zoom out", "ele-image-viewer__action");
        Button zoomIn = previewButton(EleFXIconType.ZOOM_IN, "Zoom in", "ele-image-viewer__action");
        Button sizeMode = previewButton(EleFXIconType.FULL_SCREEN, "Show original size", "ele-image-viewer__action");
        previewSizeModeIcon = (EleFXIcon) sizeMode.getGraphic();
        Button rotateLeft = previewButton(EleFXIconType.REFRESH_LEFT, "Rotate left", "ele-image-viewer__action");
        Button rotateRight = previewButton(EleFXIconType.REFRESH_RIGHT, "Rotate right", "ele-image-viewer__action");
        Button close = previewButton(EleFXIconType.CLOSE, "Close preview", "ele-image-viewer__close");
        previous.setOnAction(e -> movePreview(-1));
        next.setOnAction(e -> movePreview(1));
        zoomOut.setOnAction(e -> zoomPreview(1 / 1.2));
        zoomIn.setOnAction(e -> zoomPreview(1.2));
        sizeMode.setOnAction(e -> togglePreviewSizeMode(sizeMode));
        rotateLeft.setOnAction(e -> previewImage.setRotate(previewImage.getRotate() - 90));
        rotateRight.setOnAction(e -> previewImage.setRotate(previewImage.getRotate() + 90));
        close.setOnAction(e -> closePreview());
        HBox controls = new HBox(4, zoomOut, zoomIn, sizeMode, rotateLeft, rotateRight);
        controls.getStyleClass().add("ele-image-viewer__toolbar");
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(Region.USE_PREF_SIZE);
        previewProgress = new Label();
        previewProgress.getStyleClass().add("ele-image-viewer__progress");
        previewProgress.setMinHeight(20);
        previewProgress.setPrefHeight(20);
        updatePreviewProgress();
        previewImageContent = new StackPane(previewImage);
        previewImageContent.getStyleClass().add("ele-image-viewer");
        previewImageContent.setMinSize(0, 0);
        previewImageContent.widthProperty().addListener(o -> updatePreviewImageSize());
        previewImageContent.heightProperty().addListener(o -> updatePreviewImageSize());
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        VBox footer = new VBox(8, previewProgress, controls);
        footer.setAlignment(Pos.CENTER);
        footer.setFillWidth(false);
        footer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane root = new StackPane(previewImageContent, footer, previous, next);
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);
        root.getStyleClass().add("ele-image-viewer__root");
        StackPane overlay = new StackPane(root, close);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        Scene scene = new Scene(overlay, Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && isCloseOnPressEscape())
                closePreview();
            else if (e.getCode() == KeyCode.LEFT)
                movePreview(-1);
            else if (e.getCode() == KeyCode.RIGHT) movePreview(1);
        });
        scene.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() == 0) return;
            zoomPreview(e.getDeltaY() > 0 ? 1.1 : 1 / 1.1);
            e.consume();
        });
        previewStage.setScene(scene);
        previewStage.setOnHidden(e -> fireEvent(new Event(this, this, PREVIEW_CLOSE)));
    }

    private Button previewButton(EleFXIconType iconType, String accessibleText, String styleClass) {
        EleFXIcon icon = new EleFXIcon(iconType, 22);
        icon.setMouseTransparent(true);
        Button button = new Button();
        button.setGraphic(icon);
        button.setAccessibleText(accessibleText);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private void fitPreviewToOwner() {
        if (previewStage.isFullScreen() || getScene() == null) return;
        Window owner = getScene().getWindow();
        if (owner == null) return;
        previewStage.setX(owner.getX());
        previewStage.setY(owner.getY());
        previewStage.setWidth(owner.getWidth());
        previewStage.setHeight(owner.getHeight());
    }

    private void zoomPreview(double factor) {
        double scale = Math.max(previewImage.getScaleX(), previewImage.getScaleY()) * factor;
        scale = Math.max(.2, Math.min(7, scale));
        previewImage.setScaleX(scale);
        previewImage.setScaleY(scale);
    }

    private void togglePreviewSizeMode(Button sizeMode) {
        previewOriginalSize = !previewOriginalSize;
        updatePreviewImageSize();
        previewImage.setScaleX(1);
        previewImage.setScaleY(1);
        previewSizeModeIcon.setType(previewOriginalSize ? EleFXIconType.SCALE_TO_ORIGINAL : EleFXIconType.FULL_SCREEN);
        sizeMode.setAccessibleText(previewOriginalSize ? "Fit image to preview" : "Show original size");
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
        previewImageError = false;
        previewOriginalSize = false;
        if (previewSizeModeIcon != null) previewSizeModeIcon.setType(EleFXIconType.FULL_SCREEN);
        updatePreviewImageSize();
        previewImage.setScaleX(1);
        previewImage.setScaleY(1);
        previewImage.setRotate(0);
        try {
            Image candidate = new Image(source, true);
            candidate.errorProperty().addListener((o, old, failed) -> {
                if (failed && previewImage.getImage() == candidate) {
                    previewImageError = true;
                    updatePreviewProgress();
                }
            });
            candidate.progressProperty().addListener((o, old, progress) -> {
                if (progress.doubleValue() >= 1 && previewImage.getImage() == candidate) updatePreviewImageSize();
            });
            previewImage.setImage(candidate);
            previewImageError = candidate.isError();
            updatePreviewImageSize();
            updatePreviewProgress();
        } catch (IllegalArgumentException exception) {
            previewImage.setImage(null);
            previewImageError = true;
            updatePreviewProgress();
        }
    }

    private void updatePreviewProgress() {
        if (previewProgress == null) return;
        boolean visible = isShowProgress() || previewImageError;
        previewProgress.setVisible(visible);
        previewProgress.setManaged(true);
        if (!visible) return;
        if (previewImageError) {
            previewProgress.setText("Failed to load image");
            return;
        }
        ObservableList<String> sources = previewSources();
        previewProgress.setText((previewIndex + 1) + " / " + sources.size());
    }

    private void updatePreviewImageSize() {
        if (previewImage == null || previewImageContent == null) return;
        Image image = previewImage.getImage();
        if (previewOriginalSize || image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            previewImage.setPreserveRatio(true);
            previewImage.setFitWidth(0);
            previewImage.setFitHeight(0);
            return;
        }
        double availableWidth = previewImageContent.getWidth();
        double availableHeight = previewImageContent.getHeight();
        if (availableWidth <= 0 || availableHeight <= 0) return;
        if (image.getWidth() <= availableWidth && image.getHeight() <= availableHeight) {
            previewImage.setPreserveRatio(true);
            previewImage.setFitWidth(0);
            previewImage.setFitHeight(0);
        } else {
            double scale = Math.max(availableWidth / image.getWidth(), availableHeight / image.getHeight());
            previewImage.setPreserveRatio(false);
            previewImage.setFitWidth(image.getWidth() * scale);
            previewImage.setFitHeight(image.getHeight() * scale);
        }
    }
}
