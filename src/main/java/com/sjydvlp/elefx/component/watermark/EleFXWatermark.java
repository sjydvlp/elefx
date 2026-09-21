package com.sjydvlp.elefx.component.watermark;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.Collection;

/**
 * A non-interactive, repeating watermark container inspired by Element Plus.
 * Add application content through {@link #getContentChildren()}; the watermark is
 * painted above it and never intercepts mouse or keyboard input.
 */
public class EleFXWatermark extends StackPane implements Themable {

    public static final String STYLE_CLASS = "ele-watermark";

    private final StackPane contentPane = new StackPane();

    private final Canvas overlay = new Canvas();

    private final ObservableList<String> content = FXCollections.observableArrayList("Element Plus");

    private final DoubleProperty watermarkWidth = new SimpleDoubleProperty(this, "width", 120);

    private final DoubleProperty watermarkHeight = new SimpleDoubleProperty(this, "height", 64);

    private final DoubleProperty watermarkRotate = new SimpleDoubleProperty(this, "watermarkRotate", -22);

    private final DoubleProperty zIndex = new SimpleDoubleProperty(this, "zIndex", 9);

    private final StringProperty image = new SimpleStringProperty(this, "image", "");

    private final ObjectProperty<EleFXWatermarkFont> font = new SimpleObjectProperty<>(this, "font",
            new EleFXWatermarkFont());

    private final DoubleProperty gapX = new SimpleDoubleProperty(this, "gapX", 100);

    private final DoubleProperty gapY = new SimpleDoubleProperty(this, "gapY", 100);

    private final DoubleProperty offsetX = new SimpleDoubleProperty(this, "offsetX", Double.NaN);

    private final DoubleProperty offsetY = new SimpleDoubleProperty(this, "offsetY", Double.NaN);

    private Image watermarkImage;

    private final InvalidationListener redrawListener = observable -> draw();

    public EleFXWatermark() {
        initialize();
    }

    public EleFXWatermark(Node... children) {
        this();
        getContentChildren().addAll(children);
    }

    public EleFXWatermark(String text, Node... children) {
        this(children);
        setContent(text);
    }

    public ObservableList<Node> getContentChildren() {
        return contentPane.getChildren();
    }

    public StackPane getContentPane() {
        return contentPane;
    }

    public Canvas getOverlay() {
        return overlay;
    }

    public ObservableList<String> getContent() {
        return content;
    }

    public void setContent(String value) {
        content.setAll(value == null ? "" : value);
    }

    public void setContent(String... values) {
        content.setAll(values == null ? Arrays.<String>asList() : Arrays.asList(values));
    }

    public void setContent(Collection<String> values) {
        content.setAll(values == null ? FXCollections.<String>emptyObservableList() : values);
    }

    public double getWatermarkWidth() {
        return watermarkWidth.get();
    }

    public DoubleProperty watermarkWidthProperty() {
        return watermarkWidth;
    }

    public void setWatermarkWidth(double value) {
        watermarkWidth.set(Math.max(1, value));
    }

    public double getWatermarkHeight() {
        return watermarkHeight.get();
    }

    public DoubleProperty watermarkHeightProperty() {
        return watermarkHeight;
    }

    public void setWatermarkHeight(double value) {
        watermarkHeight.set(Math.max(1, value));
    }

    /** Rotation of each watermark tile in degrees, distinct from JavaFX Node rotation. */
    public double getWatermarkRotate() {
        return watermarkRotate.get();
    }

    public DoubleProperty watermarkRotateProperty() {
        return watermarkRotate;
    }

    public void setWatermarkRotate(double value) {
        watermarkRotate.set(value);
    }

    /** Element Plus-compatible layering value; higher values are painted above lower sibling views. */
    public double getZIndex() {
        return zIndex.get();
    }

    public DoubleProperty zIndexProperty() {
        return zIndex;
    }

    public void setZIndex(double value) {
        zIndex.set(value);
    }

    public String getImage() {
        return image.get();
    }

    public StringProperty imageProperty() {
        return image;
    }

    public void setImage(String value) {
        image.set(value == null ? "" : value.trim());
    }

    public EleFXWatermarkFont getFont() {
        return font.get();
    }

    public ObjectProperty<EleFXWatermarkFont> fontProperty() {
        return font;
    }

    public void setFont(EleFXWatermarkFont value) {
        font.set(value == null ? new EleFXWatermarkFont() : value);
    }

    public double getGapX() {
        return gapX.get();
    }

    public DoubleProperty gapXProperty() {
        return gapX;
    }

    public void setGapX(double value) {
        gapX.set(Math.max(0, value));
    }

    public double getGapY() {
        return gapY.get();
    }

    public DoubleProperty gapYProperty() {
        return gapY;
    }

    public void setGapY(double value) {
        gapY.set(Math.max(0, value));
    }

    public void setGap(double x, double y) {
        setGapX(x);
        setGapY(y);
    }

    public double getOffsetX() {
        return offsetX.get();
    }

    public DoubleProperty offsetXProperty() {
        return offsetX;
    }

    /** Use {@link Double#NaN} to restore Element Plus's default of half the horizontal gap. */
    public void setOffsetX(double value) {
        offsetX.set(value);
    }

    public double getOffsetY() {
        return offsetY.get();
    }

    public DoubleProperty offsetYProperty() {
        return offsetY;
    }

    /** Use {@link Double#NaN} to restore Element Plus's default of half the vertical gap. */
    public void setOffsetY(double value) {
        offsetY.set(value);
    }

    public void setOffset(double x, double y) {
        setOffsetX(x);
        setOffsetY(y);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.WATERMARK;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        contentPane.getStyleClass().add("ele-watermark__content");
        overlay.getStyleClass().add("ele-watermark__overlay");
        overlay.setMouseTransparent(true);
        overlay.setManaged(false);
        getChildren().addAll(contentPane, overlay);
        content.addListener((ListChangeListener<String>) change -> draw());
        watermarkWidth.addListener(redrawListener);
        watermarkHeight.addListener(redrawListener);
        watermarkRotate.addListener(redrawListener);
        gapX.addListener(redrawListener);
        gapY.addListener(redrawListener);
        offsetX.addListener(redrawListener);
        offsetY.addListener(redrawListener);
        image.addListener((o, oldValue, value) -> loadImage(value));
        font.addListener((o, oldValue, value) -> {
            observeFont(oldValue, false);
            observeFont(value, true);
            draw();
        });
        observeFont(getFont(), true);
        zIndex.addListener((o, oldValue, value) -> setViewOrder(-value.doubleValue()));
        setViewOrder(-getZIndex());
        sceneBuilderIntegration();
    }

    private void observeFont(EleFXWatermarkFont value, boolean add) {
        if (value == null) return;
        if (add) {
            value.colorProperty().addListener(redrawListener);
            value.fontSizeProperty().addListener(redrawListener);
            value.fontWeightProperty().addListener(redrawListener);
            value.fontFamilyProperty().addListener(redrawListener);
            value.fontGapProperty().addListener(redrawListener);
            value.fontStyleProperty().addListener(redrawListener);
            value.textAlignProperty().addListener(redrawListener);
            value.textBaselineProperty().addListener(redrawListener);
        } else {
            value.colorProperty().removeListener(redrawListener);
            value.fontSizeProperty().removeListener(redrawListener);
            value.fontWeightProperty().removeListener(redrawListener);
            value.fontFamilyProperty().removeListener(redrawListener);
            value.fontGapProperty().removeListener(redrawListener);
            value.fontStyleProperty().removeListener(redrawListener);
            value.textAlignProperty().removeListener(redrawListener);
            value.textBaselineProperty().removeListener(redrawListener);
        }
    }

    private void loadImage(String source) {
        watermarkImage = null;
        if (source == null || source.isBlank()) {
            draw();
            return;
        }
        try {
            Image candidate = new Image(source, true);
            watermarkImage = candidate;
            candidate.progressProperty().addListener((o, oldValue, progress) -> {
                if (watermarkImage == candidate && progress.doubleValue() >= 1) draw();
            });
            candidate.errorProperty().addListener((o, oldValue, error) -> {
                if (watermarkImage == candidate && error) {
                    watermarkImage = null;
                    draw();
                }
            });
        } catch (IllegalArgumentException ignored) {
        }
        draw();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double canvasWidth = getWidth() - snappedLeftInset() - snappedRightInset();
        double canvasHeight = getHeight() - snappedTopInset() - snappedBottomInset();
        overlay.setWidth(Math.max(0, canvasWidth));
        overlay.setHeight(Math.max(0, canvasHeight));
        overlay.relocate(snappedLeftInset(), snappedTopInset());
        draw();
    }

    private void draw() {
        double canvasWidth = overlay.getWidth(), canvasHeight = overlay.getHeight();
        GraphicsContext graphics = overlay.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvasWidth, canvasHeight);
        if (canvasWidth <= 0 || canvasHeight <= 0) return;
        double cellWidth = getWatermarkWidth() + getGapX(), cellHeight = getWatermarkHeight() + getGapY();
        if (cellWidth <= 0 || cellHeight <= 0) return;
        double startX = Double.isNaN(getOffsetX()) ? getGapX() / 2 : getOffsetX();
        double startY = Double.isNaN(getOffsetY()) ? getGapY() / 2 : getOffsetY();
        for (double y = startY - cellHeight; y < canvasHeight + cellHeight; y += cellHeight)
            for (double x = startX - cellWidth; x < canvasWidth + cellWidth; x += cellWidth)
                drawMark(graphics, x, y);
    }

    private void drawMark(GraphicsContext graphics, double x, double y) {
        graphics.save();
        graphics.translate(x + getWatermarkWidth() / 2, y + getWatermarkHeight() / 2);
        graphics.rotate(getWatermarkRotate());
        if (watermarkImage != null && !watermarkImage.isError() && watermarkImage.getProgress() >= 1)
            graphics.drawImage(watermarkImage, -getWatermarkWidth() / 2, -getWatermarkHeight() / 2, getWatermarkWidth(),
                    getWatermarkHeight());
        else
            drawText(graphics);
        graphics.restore();
    }

    private void drawText(GraphicsContext graphics) {
        EleFXWatermarkFont style = getFont();
        try {
            graphics.setFill(Color.web(style.getColor()));
        } catch (IllegalArgumentException ignored) {
            graphics.setFill(Color.rgb(0, 0, 0, .15));
        }
        graphics.setFont(
                Font.font(style.getFontFamily(), style.getFontWeight(), style.getFontStyle(), style.getFontSize()));
        graphics.setTextAlign(style.getTextAlign());
        graphics.setTextBaseline(baseline(style.getTextBaseline()));
        double lineHeight = style.getFontSize() + style.getFontGap();
        double totalHeight = Math.max(0, content.size() - 1) * lineHeight + style.getFontSize();
        double firstY = -totalHeight / 2;
        double textX = textX(style.getTextAlign());
        for (int index = 0; index < content.size(); index++)
            graphics.fillText(content.get(index) == null ? "" : content.get(index), textX, firstY + index * lineHeight);
    }

    private static VPos baseline(String value) {
        if ("center".equalsIgnoreCase(value)) return VPos.CENTER;
        if ("alphabetic".equalsIgnoreCase(value)) return VPos.BASELINE;
        if ("bottom".equalsIgnoreCase(value)) return VPos.BOTTOM;
        return VPos.TOP;
    }

    private double textX(TextAlignment alignment) {
        if (alignment == TextAlignment.LEFT || alignment == TextAlignment.JUSTIFY) return -getWatermarkWidth() / 2;
        if (alignment == TextAlignment.RIGHT) return getWatermarkWidth() / 2;
        return 0;
    }
}
