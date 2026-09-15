package com.sjydvlp.elefx.component.avatar;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * An Element Plus-inspired avatar which can display an image, an icon, text, or custom content.
 * Image content has precedence; after an image load failure, configured fallback content is shown,
 * or a built-in picture icon when no fallback is configured.
 */
public class EleFXAvatar extends StackPane implements Themable {

    public static final double DEFAULT_SIZE = 40;

    /** Event fired when the current image cannot be loaded. */
    public static final EventType<Event> IMAGE_ERROR = new EventType<>(Event.ANY, "ELEFX_AVATAR_IMAGE_ERROR");

    private static final String STYLE_CLASS = "ele-avatar";

    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<EleFXAvatarSize> avatarSize = new SimpleObjectProperty<>(this, "avatarSize",
            EleFXAvatarSize.DEFAULT);

    private final ObjectProperty<EleFXAvatarShape> avatarShape = new SimpleObjectProperty<>(this, "avatarShape",
            EleFXAvatarShape.CIRCLE);

    private final StringProperty src = new SimpleStringProperty(this, "src");

    private final StringProperty alt = new SimpleStringProperty(this, "alt");

    private final ObjectProperty<EleFXAvatarFit> fit = new SimpleObjectProperty<>(this, "fit", EleFXAvatarFit.COVER);

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final StringProperty text = new SimpleStringProperty(this, "text");

    private final BooleanProperty imageError = new SimpleBooleanProperty(this, "imageError", false);

    private final ObjectProperty<EventHandler<Event>> onError = new SimpleObjectProperty<>(this, "onError");

    private final ImageView imageView = new ImageView();

    private final EleFXIcon defaultErrorIcon = new EleFXIcon(EleFXIconType.PICTURE_FILLED);

    private final ObjectProperty<Node> errorIcon = new SimpleObjectProperty<>(this, "errorIcon", defaultErrorIcon);

    private final Rectangle squareClip = new Rectangle();

    private final Circle circleClip = new Circle();

    private Image currentImage;

    private boolean applyingPresetSize;

    public EleFXAvatar() {
        initialize();
    }

    public EleFXAvatar(String text) {
        setText(text);
        initialize();
    }

    public EleFXAvatar(double size) {
        setSize(size);
        initialize();
    }

    public EleFXAvatar(EleFXAvatarSize size) {
        setSize(size);
        initialize();
    }

    public EleFXAvatar(String text, double size) {
        setText(text);
        setSize(size);
        initialize();
    }

    public double getSize() {
        return size.get();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public void setSize(double size) {
        if (!Double.isFinite(size) || size <= 0)
            throw new IllegalArgumentException("size must be a positive finite number");
        if (!applyingPresetSize) avatarSize.set(null);
        this.size.set(size);
    }

    /**
     * Applies an Element Plus preset size. Calling {@link #setSize(double)} subsequently
     * clears this property and uses the supplied custom pixel size instead.
     */
    public EleFXAvatarSize getAvatarSize() {
        return avatarSize.get();
    }

    public ObjectProperty<EleFXAvatarSize> avatarSizeProperty() {
        return avatarSize;
    }

    public void setAvatarSize(EleFXAvatarSize size) {
        avatarSize.set(size);
    }

    /** Convenience overload matching Element Plus's numeric-or-preset {@code size} API. */
    public void setSize(EleFXAvatarSize size) {
        setAvatarSize(size);
    }

    /**
     * The Avatar outline. This is named {@code avatarShape} because JavaFX
     * {@link javafx.scene.layout.Region} reserves {@code shape} for its clipping shape.
     */
    public EleFXAvatarShape getAvatarShape() {
        return avatarShape.get();
    }

    public ObjectProperty<EleFXAvatarShape> avatarShapeProperty() {
        return avatarShape;
    }

    public void setAvatarShape(EleFXAvatarShape shape) {
        this.avatarShape.set(shape == null ? EleFXAvatarShape.CIRCLE : shape);
    }

    public String getSrc() {
        return src.get();
    }

    public StringProperty srcProperty() {
        return src;
    }

    public void setSrc(String src) {
        this.src.set(src == null || src.isBlank() ? null : src);
    }

    public String getAlt() {
        return alt.get();
    }

    public StringProperty altProperty() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt.set(alt);
    }

    public EleFXAvatarFit getFit() {
        return fit.get();
    }

    public ObjectProperty<EleFXAvatarFit> fitProperty() {
        return fit;
    }

    public void setFit(EleFXAvatarFit fit) {
        this.fit.set(fit == null ? EleFXAvatarFit.COVER : fit);
    }

    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node icon) {
        this.icon.set(icon);
    }

    /**
     * Icon displayed after an image load failure when no custom fallback content is present.
     * The default is Element Plus's filled-picture icon; set {@code null} to show no error icon.
     */
    public Node getErrorIcon() {
        return errorIcon.get();
    }

    public ObjectProperty<Node> errorIconProperty() {
        return errorIcon;
    }

    public void setErrorIcon(Node errorIcon) {
        this.errorIcon.set(errorIcon);
    }

    /** Custom fallback content. It takes precedence over {@link #getIcon()} and {@link #getText()}. */
    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public boolean isImageError() {
        return imageError.get();
    }

    public BooleanProperty imageErrorProperty() {
        return imageError;
    }

    public EventHandler<Event> getOnError() {
        return onError.get();
    }

    public ObjectProperty<EventHandler<Event>> onErrorProperty() {
        return onError;
    }

    public void setOnError(EventHandler<Event> handler) {
        onError.set(handler);
    }

    /** Returns the ImageView used while a source image is available. */
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.AVATAR;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        imageView.getStyleClass().add("ele-avatar__image");
        defaultErrorIcon.getStyleClass().add("ele-avatar__error-icon");
        imageView.setSmooth(true);
        setAlignment(Pos.CENTER);
        size.addListener((o, oldValue, newValue) -> updateSize());
        avatarSize.addListener((o, oldValue, newValue) -> updatePresetSize(oldValue, newValue));
        avatarShape.addListener((o, oldValue, newValue) -> updateShape(oldValue, newValue));
        src.addListener((o, oldValue, newValue) -> loadImage());
        alt.addListener((o, oldValue, newValue) -> setAccessibleText(newValue));
        fit.addListener(o -> updateImageGeometry());
        icon.addListener(o -> updateContent());
        errorIcon.addListener(o -> updateContent());
        content.addListener(o -> updateContent());
        text.addListener(o -> updateContent());
        imageError.addListener((o, oldValue, newValue) -> updateImageError(newValue));
        onError.addListener((o, oldValue, newValue) -> setEventHandler(IMAGE_ERROR, newValue));
        updateSize();
        updatePresetSize(null, getAvatarSize());
        updateShape(null, getAvatarShape());
        setAccessibleText(getAlt());
        loadImage();
        sceneBuilderIntegration();
    }

    private void updateSize() {
        double value = getSize();
        setMinSize(value, value);
        setPrefSize(value, value);
        setMaxSize(value, value);
        squareClip.setWidth(value);
        squareClip.setHeight(value);
        circleClip.setCenterX(value / 2);
        circleClip.setCenterY(value / 2);
        circleClip.setRadius(value / 2);
        defaultErrorIcon.setSize(Math.max(12, value / 2));
        updateImageGeometry();
    }

    private void updateImageError(boolean hasError) {
        updateBooleanStyleClass(hasError, "ele-avatar--error");
        updateContent();
    }

    private void updatePresetSize(EleFXAvatarSize oldSize, EleFXAvatarSize newSize) {
        if (oldSize != null) getStyleClass().remove(oldSize.styleClass());
        if (newSize == null) return;
        if (!getStyleClass().contains(newSize.styleClass())) getStyleClass().add(newSize.styleClass());
        applyingPresetSize = true;
        try {
            setSize(newSize.pixels());
        } finally {
            applyingPresetSize = false;
        }
    }

    private void updateShape(EleFXAvatarShape oldShape, EleFXAvatarShape newShape) {
        if (oldShape != null) getStyleClass().remove(oldShape.styleClass());
        EleFXAvatarShape resolved = newShape == null ? EleFXAvatarShape.CIRCLE : newShape;
        if (!getStyleClass().contains(resolved.styleClass())) getStyleClass().add(resolved.styleClass());
        setClip(resolved == EleFXAvatarShape.CIRCLE ? circleClip : squareClip);
    }

    private void loadImage() {
        imageError.set(false);
        String source = getSrc();
        if (source == null) {
            currentImage = null;
            imageView.setImage(null);
            updateContent();
            return;
        }
        Image image;
        try {
            image = new Image(source, true);
        } catch (IllegalArgumentException exception) {
            currentImage = null;
            imageView.setImage(null);
            imageError.set(true);
            updateContent();
            fireEvent(new Event(this, this, IMAGE_ERROR));
            return;
        }
        currentImage = image;
        image.errorProperty().addListener((o, oldValue, hasError) -> {
            if (hasError && currentImage == image) {
                imageError.set(true);
                imageView.setImage(null);
                updateContent();
                fireEvent(new Event(this, this, IMAGE_ERROR));
            }
        });
        image.progressProperty().addListener((o, oldValue, progress) -> {
            if (currentImage == image && progress.doubleValue() >= 1 && !image.isError()) {
                imageView.setImage(image);
                updateContent();
            }
        });
        if (!image.isBackgroundLoading() && !image.isError()) imageView.setImage(image);
        if (image.isError()) {
            imageError.set(true);
            fireEvent(new Event(this, this, IMAGE_ERROR));
        }
        updateContent();
    }

    private void updateContent() {
        getChildren().clear();
        boolean showImage = imageView.getImage() != null && !isImageError();
        if (showImage) {
            updateImageGeometry();
            getChildren().add(imageView);
            requestLayout();
            return;
        }
        Node fallback = getContent() != null
                ? getContent()
                : isImageError()
                        ? errorFallback()
                        : getIcon() != null ? getIcon() : getText() == null ? null : createTextNode();
        if (fallback != null) getChildren().add(fallback);
        requestLayout();
    }

    private Node errorFallback() {
        Node errorFallback = getErrorIcon();
        if (errorFallback != null && !errorFallback.getStyleClass().contains("ele-avatar__error-icon")) {
            errorFallback.getStyleClass().add("ele-avatar__error-icon");
        }
        return errorFallback;
    }

    private void updateBooleanStyleClass(boolean active, String styleClass) {
        if (active && !getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
        if (!active) getStyleClass().remove(styleClass);
    }

    private Label createTextNode() {
        Label label = new Label(getText());
        label.getStyleClass().add("ele-avatar__text");
        return label;
    }

    private void updateImageGeometry() {
        Image image = imageView.getImage();
        if (image == null) return;
        double box = getSize();
        double width = image.getWidth();
        double height = image.getHeight();
        if (width <= 0 || height <= 0) return;
        switch (getFit()) {
            case FILL -> {
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(box);
                imageView.setFitHeight(box);
            }
            case CONTAIN -> {
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(box);
                imageView.setFitHeight(box);
            }
            case COVER -> {
                imageView.setPreserveRatio(false);
                if (width / height > 1) {
                    imageView.setFitHeight(box);
                    imageView.setFitWidth(box * width / height);
                } else {
                    imageView.setFitWidth(box);
                    imageView.setFitHeight(box * height / width);
                }
            }
            case NONE -> {
                imageView.setPreserveRatio(false);
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
            }
            case SCALE_DOWN -> {
                imageView.setPreserveRatio(false);
                if (width <= box && height <= box) {
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(height);
                } else {
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(box);
                    imageView.setFitHeight(box);
                }
            }
        }
    }
}
