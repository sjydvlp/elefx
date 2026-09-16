package com.sjydvlp.elefx.component.empty;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * An Element Plus-inspired placeholder for an empty state.
 *
 * <p>
 * The {@linkplain #getImageNode() custom image node} and
 * {@linkplain #getDescriptionNode() custom description node} mirror Element Plus's named
 * slots. Add actions or other default-slot content through {@link #getBottomChildren()}.
 * </p>
 */
public class EleFXEmpty extends VBox implements Themable {

    public static final double DEFAULT_IMAGE_SIZE = 160;

    private static final String STYLE_CLASS = "ele-empty";

    private final StringProperty image = new SimpleStringProperty(this, "image", "");

    private final DoubleProperty imageSize = new SimpleDoubleProperty(this, "imageSize", DEFAULT_IMAGE_SIZE);

    private final StringProperty description = new SimpleStringProperty(this, "description", "No Data");

    private final ObjectProperty<Node> imageNode = new SimpleObjectProperty<>(this, "imageNode");

    private final ObjectProperty<Node> descriptionNode = new SimpleObjectProperty<>(this, "descriptionNode");

    private final StackPane imageBox = new StackPane();

    private final StackPane descriptionBox = new StackPane();

    private final VBox bottom = new VBox();

    private final ImageView imageView = new ImageView();

    private final Label descriptionLabel = new Label();

    private final Node defaultImage = createDefaultImage();

    private Image loadedImage;

    public EleFXEmpty() {
        initialize();
    }

    public EleFXEmpty(String description) {
        setDescription(description);
        initialize();
    }

    public EleFXEmpty(Node... bottomContent) {
        this();
        getBottomChildren().addAll(bottomContent);
    }

    /** URL of the optional image. A custom {@link #getImageNode() image node} takes precedence. */
    public String getImage() {
        return image.get();
    }

    public StringProperty imageProperty() {
        return image;
    }

    public void setImage(String value) {
        image.set(value == null ? "" : value.trim());
    }

    /** Width, in pixels, used for the image area and the default illustration. */
    public double getImageSize() {
        return imageSize.get();
    }

    public DoubleProperty imageSizeProperty() {
        return imageSize;
    }

    public void setImageSize(double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("imageSize must be a positive finite number");
        }
        imageSize.set(value);
    }

    /** Text shown beneath the image when no custom description node is set. */
    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String value) {
        description.set(value == null ? "" : value);
    }

    /** Custom image content, corresponding to Element Plus's {@code image} slot. */
    public Node getImageNode() {
        return imageNode.get();
    }

    public ObjectProperty<Node> imageNodeProperty() {
        return imageNode;
    }

    public void setImageNode(Node value) {
        imageNode.set(value);
    }

    /** Custom description content, corresponding to Element Plus's {@code description} slot. */
    public Node getDescriptionNode() {
        return descriptionNode.get();
    }

    public ObjectProperty<Node> descriptionNodeProperty() {
        return descriptionNode;
    }

    public void setDescriptionNode(Node value) {
        descriptionNode.set(value);
    }

    /** Container for bottom content, corresponding to Element Plus's default slot. */
    public VBox getBottom() {
        return bottom;
    }

    public ObservableList<Node> getBottomChildren() {
        return bottom.getChildren();
    }

    /** The image area, exposed for layout customization. */
    public StackPane getImageBox() {
        return imageBox;
    }

    /** The description area, exposed for layout customization. */
    public StackPane getDescriptionBox() {
        return descriptionBox;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.EMPTY;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.TOP_CENTER);
        setFillWidth(false);
        imageBox.getStyleClass().add("ele-empty__image");
        descriptionBox.getStyleClass().add("ele-empty__description");
        bottom.getStyleClass().add("ele-empty__bottom");
        bottom.setAlignment(Pos.TOP_CENTER);
        imageView.getStyleClass().add("ele-empty__image-view");
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        descriptionLabel.getStyleClass().add("ele-empty__description-text");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setAlignment(Pos.CENTER);
        getChildren().addAll(imageBox, descriptionBox, bottom);

        image.addListener((o, oldValue, value) -> loadImage());
        imageSize.addListener((o, oldValue, value) -> updateImageSize());
        imageNode.addListener(o -> refreshImage());
        description.addListener(o -> refreshDescription());
        descriptionNode.addListener(o -> refreshDescription());
        bottom.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> refreshBottom());
        updateImageSize();
        loadImage();
        refreshDescription();
        refreshBottom();
        sceneBuilderIntegration();
    }

    private void updateImageSize() {
        double size = getImageSize();
        imageBox.setMinSize(size, size);
        imageBox.setPrefSize(size, size);
        imageBox.setMaxSize(size, size);
        imageView.setFitWidth(size);
        defaultImage.setScaleX(size / DEFAULT_IMAGE_SIZE);
        defaultImage.setScaleY(size / DEFAULT_IMAGE_SIZE);
        refreshImage();
    }

    private void loadImage() {
        String source = getImage();
        if (source.isBlank()) {
            loadedImage = null;
            imageView.setImage(null);
            refreshImage();
            return;
        }
        try {
            Image candidate = new Image(source, true);
            loadedImage = candidate;
            candidate.errorProperty().addListener((o, oldValue, error) -> {
                if (error && loadedImage == candidate) {
                    imageView.setImage(null);
                    refreshImage();
                }
            });
            candidate.progressProperty().addListener((o, oldValue, progress) -> {
                if (loadedImage == candidate && progress.doubleValue() >= 1 && !candidate.isError()) {
                    imageView.setImage(candidate);
                    refreshImage();
                }
            });
            imageView.setImage(candidate.isError() ? null : candidate);
        } catch (IllegalArgumentException exception) {
            loadedImage = null;
            imageView.setImage(null);
        }
        refreshImage();
    }

    private void refreshImage() {
        Node content = getImageNode() != null
                ? getImageNode()
                : imageView.getImage() != null ? imageView : defaultImage;
        imageBox.getChildren().setAll(content);
    }

    private void refreshDescription() {
        Node content = getDescriptionNode();
        if (content == null) {
            descriptionLabel.setText(getDescription());
            content = descriptionLabel;
        }
        boolean present = content != descriptionLabel || !getDescription().isBlank();
        descriptionBox.getChildren().setAll(content);
        descriptionBox.setManaged(present);
        descriptionBox.setVisible(present);
    }

    private void refreshBottom() {
        boolean present = !bottom.getChildren().isEmpty();
        bottom.setManaged(present);
        bottom.setVisible(present);
    }

    private static Node createDefaultImage() {
        Group drawing = new Group();
        Rectangle shadow = styled(new Rectangle(24, 133, 112, 8), "ele-empty__fill-8");
        shadow.setArcWidth(8);
        shadow.setArcHeight(8);
        Rectangle document = styled(new Rectangle(45, 28, 70, 94), "ele-empty__fill-3");
        document.setArcWidth(6);
        document.setArcHeight(6);
        Polygon foldedCorner = styled(new Polygon(94, 28, 115, 28, 115, 49), "ele-empty__fill-6");
        Rectangle page = styled(new Rectangle(53, 38, 54, 76), "ele-empty__fill-1");
        page.setArcWidth(4);
        page.setArcHeight(4);
        Rectangle lineOne = styled(new Rectangle(64, 57, 32, 5), "ele-empty__fill-7");
        Rectangle lineTwo = styled(new Rectangle(64, 70, 24, 5), "ele-empty__fill-7");
        Circle marker = styled(new Circle(80, 91, 11), "ele-empty__fill-5");
        Rectangle smallLine = styled(new Rectangle(76, 86, 8, 3), "ele-empty__fill-0");
        smallLine.setArcWidth(3);
        smallLine.setArcHeight(3);
        drawing.getChildren().addAll(shadow, document, foldedCorner, page, lineOne, lineTwo, marker, smallLine);
        drawing.getStyleClass().add("ele-empty__default-image");
        return drawing;
    }

    private static <T extends Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }
}
