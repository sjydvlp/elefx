package com.sjydvlp.elefx.component.image;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * An Element Plus-inspired image view with loading/error fallbacks and an optional preview.
 * Custom placeholder and error nodes are JavaFX equivalents of Element Plus's named slots.
 */
public class EleFXImage extends StackPane implements Themable {

    public static final EventType<Event> IMAGE_LOAD = new EventType<>(Event.ANY, "ELEFX_IMAGE_LOAD");

    public static final EventType<Event> IMAGE_ERROR = new EventType<>(Event.ANY, "ELEFX_IMAGE_ERROR");

    private final StringProperty src = new SimpleStringProperty(this, "src", "");

    private final StringProperty alt = new SimpleStringProperty(this, "alt", "");

    private final ObjectProperty<EleFXImageFit> fit = new SimpleObjectProperty<>(this, "fit", EleFXImageFit.SCALE_DOWN);

    private final BooleanProperty lazy = new SimpleBooleanProperty(this, "lazy", false);

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);

    private final BooleanProperty imageError = new SimpleBooleanProperty(this, "imageError", false);

    private final BooleanProperty previewEnabled = new SimpleBooleanProperty(this, "previewEnabled", false);

    private final ObjectProperty<Node> placeholderNode = new SimpleObjectProperty<>(this, "placeholderNode");

    private final ObjectProperty<Node> errorNode = new SimpleObjectProperty<>(this, "errorNode");

    private final ObjectProperty<EventHandler<Event>> onLoad = new SimpleObjectProperty<>(this, "onLoad");

    private final ObjectProperty<EventHandler<Event>> onError = new SimpleObjectProperty<>(this, "onError");

    private final ImageView imageView = new ImageView();

    private final StackPane fallbackBox = new StackPane();

    private final ProgressIndicator defaultPlaceholder = new ProgressIndicator();

    private final Label defaultError = new Label("Failed to load image");

    private final Rectangle clip = new Rectangle();

    private Image currentImage;

    private final EleFXImageViewer imageViewer = new EleFXImageViewer();

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
        fit.set(value == null ? EleFXImageFit.SCALE_DOWN : value);
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

    /** Whether this image can open its viewer when clicked or through {@link #showPreview()}. */
    public boolean isPreviewEnabled() {
        return previewEnabled.get();
    }

    public BooleanProperty previewEnabledProperty() {
        return previewEnabled;
    }

    public void setPreviewEnabled(boolean value) {
        previewEnabled.set(value);
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

    public ImageView getImageView() {
        return imageView;
    }

    /** Returns the viewer used when this image is clicked for preview. */
    public EleFXImageViewer getImageViewer() {
        return imageViewer;
    }

    /** Starts deferred loading. It is safe to call even when lazy loading is disabled. */
    public void requestLoad() {
        loadImage();
    }

    /** Opens the preview window when a preview source list or source is available. */
    public void showPreview() {
        if (!isPreviewEnabled()) return;
        imageViewer.show(getScene() == null ? null : getScene().getWindow(), getSrc());
    }

    public void closePreview() {
        imageViewer.close();
    }

    public boolean isPreviewShowing() {
        return imageViewer.isShowing();
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
        placeholderNode.addListener(o -> showFallback());
        errorNode.addListener(o -> showFallback());
        previewEnabled.addListener((o, oldValue, enabled) -> {
            if (!enabled) closePreview();
        });
        onLoad.addListener((o, a, b) -> setEventHandler(IMAGE_LOAD, b));
        onError.addListener((o, a, b) -> setEventHandler(IMAGE_ERROR, b));
        setOnMouseClicked(e -> {
            if (!e.isConsumed() && isPreviewEnabled()
                    && (!imageViewer.getUrlList().isEmpty() || !getSrc().isBlank()))
                showPreview();
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

}
