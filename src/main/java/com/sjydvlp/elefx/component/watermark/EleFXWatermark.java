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
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Collection;

/**
 * A non-interactive, repeating watermark container inspired by Element Plus.
 * Add application content through {@link #getContentChildren()}; the watermark is
 * always painted above the content and never intercepts mouse or keyboard input.
 */
public class EleFXWatermark extends StackPane implements Themable {

    public static final String STYLE_CLASS = "ele-watermark";

    private final StackPane contentPane = new StackPane();

    private final Canvas overlay = new Canvas();

    private final ObservableList<String> content = FXCollections.observableArrayList("Element Plus");

    private final DoubleProperty watermarkWidth = new SimpleDoubleProperty(this, "width", Double.NaN);

    private final DoubleProperty watermarkHeight = new SimpleDoubleProperty(this, "height", Double.NaN);

    private final DoubleProperty watermarkRotate = new SimpleDoubleProperty(this, "watermarkRotate", -22);

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

    /** NaN selects automatic text sizing, or 120px for an image. */
    public double getWatermarkWidth() {
        return watermarkWidth.get();
    }

    public DoubleProperty watermarkWidthProperty() {
        return watermarkWidth;
    }

    public void setWatermarkWidth(double value) {
        watermarkWidth.set(Math.max(1, value));
    }

    /** NaN selects automatic text sizing, or 64px for an image. */
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
        // Keep the watermark above the content without changing sibling order.
        // Mouse transparency lets all input pass through to the content below.
        overlay.setViewOrder(-1);
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

    /** Geometry is calculated in logical pixels, independently of display scaling. */
    private record MarkSize(double width, double height, double space,
            double rotatedWidth, double rotatedHeight, double baselineOffset) {
    }

    private MarkSize measureMark() {
        EleFXWatermarkFont style = getFont();
        double width = 120, height = 64, space = 0, baselineOffset = 0;
        if (getImage().isBlank()) {
            Text metrics = new Text();
            metrics.setFont(Font.font(style.getFontFamily(), style.getFontSize()));
            double maxWidth = 0, maxHeight = 0;
            for (String line : content) {
                metrics.setText(line == null ? "" : line);
                maxWidth = Math.max(maxWidth, Math.ceil(metrics.getLayoutBounds().getWidth()));
                maxHeight = Math.max(maxHeight, Math.ceil(metrics.getLayoutBounds().getHeight()));
            }
            height = Math.max(0, maxHeight * content.size()
                    + Math.max(0, content.size() - 1) * style.getFontGap());
            space = Math.ceil(Math.abs(Math.sin(Math.toRadians(getWatermarkRotate())) * height) / 2);
            width = maxWidth + space;
        }
        if (!Double.isNaN(getWatermarkWidth())) width = getWatermarkWidth();
        if (!Double.isNaN(getWatermarkHeight())) height = getWatermarkHeight();
        if (watermarkImage == null && !content.isEmpty() && content.get(0) != null && !content.get(0).isEmpty()) {
            Text metrics = new Text(content.get(0));
            metrics.setFont(Font.font(style.getFontFamily(), style.getFontWeight(),
                    style.getFontStyle(), style.getFontSize()));
            double ascent = -metrics.getLayoutBounds().getMinY();
            double fontHeight = metrics.getLayoutBounds().getHeight();
            // JavaFX has no hanging baseline; use the conventional 80% ascent.
            String baseline = style.getTextBaseline();
            if ("hanging".equalsIgnoreCase(baseline))
                baselineOffset = -ascent * .2;
            else if ("alphabetic".equalsIgnoreCase(baseline))
                baselineOffset = -ascent;
            else if ("center".equalsIgnoreCase(baseline) || "middle".equalsIgnoreCase(baseline))
                baselineOffset = -fontHeight / 2;
            else if ("bottom".equalsIgnoreCase(baseline) || "ideographic".equalsIgnoreCase(baseline))
                baselineOffset = -fontHeight;
        }
        double angle = Math.toRadians(getWatermarkRotate());
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        return new MarkSize(width, height, space, width * cos + height * sin,
                width * sin + height * cos, baselineOffset);
    }

    private void draw() {
        double canvasWidth = overlay.getWidth(), canvasHeight = overlay.getHeight();
        GraphicsContext graphics = overlay.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvasWidth, canvasHeight);
        if (canvasWidth <= 0 || canvasHeight <= 0) return;
        MarkSize size = measureMark();
        double stepX = size.rotatedWidth() + getGapX();
        double stepY = size.rotatedHeight() + getGapY();
        if (!Double.isFinite(stepX) || !Double.isFinite(stepY) || stepX <= 0 || stepY <= 0
                || size.width() <= 0 || size.height() <= 0)
            return;

        // Element Plus positions its repeated texture at offset minus half the gap.
        double originX = Double.isNaN(getOffsetX()) ? 0 : getOffsetX() - getGapX() / 2;
        double originY = Double.isNaN(getOffsetY()) ? 0 : getOffsetY() - getGapY() / 2;
        if (!Double.isFinite(originX) || !Double.isFinite(originY)) return;
        double clipX = Math.max(0, originX), clipY = Math.max(0, originY);
        graphics.save();
        graphics.beginPath();
        graphics.rect(clipX, clipY, Math.max(0, canvasWidth - clipX), Math.max(0, canvasHeight - clipY));
        graphics.clip();

        // Alternate columns are shifted vertically by half a repeat period.
        int firstColumn = (int) Math.floor((clipX - originX) / stepX);
        for (int column = firstColumn; originX + column * stepX < canvasWidth; column++) {
            double x = originX + column * stepX;
            double shift = (column & 1) == 0 ? 0 : -stepY / 2;
            int firstRow = (int) Math.floor((clipY - originY - shift) / stepY);
            for (int row = firstRow; originY + shift + row * stepY < canvasHeight; row++) {
                double y = originY + shift + row * stepY;
                // Clip each repeated tile, including its baseline displacement.
                graphics.save();
                graphics.beginPath();
                graphics.rect(x, y, stepX, stepY);
                graphics.clip();
                drawMark(graphics, x, y + size.baselineOffset(), size);
                graphics.restore();
            }
        }
        graphics.restore();
    }

    private void drawMark(GraphicsContext graphics, double x, double y, MarkSize size) {
        graphics.save();
        graphics.translate(x + size.rotatedWidth() / 2, y + size.rotatedHeight() / 2);
        graphics.rotate(getWatermarkRotate());
        graphics.translate(-size.width() / 2, -size.height() / 2);
        // Match the source canvas: explicit dimensions crop content before rotation.
        graphics.beginPath();
        graphics.rect(0, 0, size.width(), size.height());
        graphics.clip();
        if (watermarkImage != null && !watermarkImage.isError() && watermarkImage.getProgress() >= 1)
            graphics.drawImage(watermarkImage, 0, 0, size.width(), size.height());
        else
            drawText(graphics, size);
        graphics.restore();
    }

    private void drawText(GraphicsContext graphics, MarkSize size) {
        EleFXWatermarkFont style = getFont();
        try {
            graphics.setFill(Color.web(style.getColor()));
        } catch (IllegalArgumentException ignored) {
            graphics.setFill(Color.rgb(0, 0, 0, .15));
        }
        graphics.setFont(
                Font.font(style.getFontFamily(), style.getFontWeight(), style.getFontStyle(), style.getFontSize()));
        graphics.setTextAlign(style.getTextAlign());
        graphics.setTextBaseline(VPos.TOP);
        double lineHeight = style.getFontSize() + style.getFontGap();
        double textX = switch (style.getTextAlign()) {
            case LEFT, JUSTIFY -> size.space() / 2;
            case RIGHT -> size.width() - size.space() / 2;
            case CENTER -> size.width() / 2;
        };
        for (int index = 0; index < content.size(); index++)
            graphics.fillText(content.get(index) == null ? "" : content.get(index), textX, index * lineHeight);
    }
}
