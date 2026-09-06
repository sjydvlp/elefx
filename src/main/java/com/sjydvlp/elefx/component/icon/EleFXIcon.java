package com.sjydvlp.elefx.component.icon;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 * 基于 SVGPath 的 EleFX 图标。
 *
 * <p>
 * 图标的 {@link #sizeProperty() size} 以像素计，默认值为 16。通过
 * {@link #setFill(Paint)} 设置图标颜色，或使用 CSS 选择器
 * {@code .ele-icon .ele-icon__svg} 进行全局定制。
 * </p>
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public class EleFXIcon extends StackPane implements Themable {

    /** Default icon size in pixels. */
    public static final double DEFAULT_SIZE = 16.0;

    private static final double VIEW_BOX_SIZE = 24.0;

    private static final String STYLE_CLASS = "ele-icon";

    private static final String SVG_STYLE_CLASS = "ele-icon__svg";

    private final SVGPath svgPath = new SVGPath();

    private final ObjectProperty<EleFXIconType> type = new SimpleObjectProperty<>(this, "type",
            EleFXIconType.SEARCH);

    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", DEFAULT_SIZE);

    /** Creates a 16px search icon. */
    public EleFXIcon() {
        initialize();
    }

    /**
     * Creates a 16px icon of the given type.
     *
     * @param type the icon type; {@code null} selects {@link EleFXIconType#SEARCH}
     */
    public EleFXIcon(EleFXIconType type) {
        setType(type);
        initialize();
    }

    /**
     * Creates an icon of the given type and size.
     *
     * @param type the icon type; {@code null} selects {@link EleFXIconType#SEARCH}
     * @param size icon size in pixels
     */
    public EleFXIcon(EleFXIconType type, double size) {
        setType(type);
        setSize(size);
        initialize();
    }

    public EleFXIconType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXIconType> typeProperty() {
        return type;
    }

    public void setType(EleFXIconType type) {
        this.type.set(type == null ? EleFXIconType.SEARCH : type);
    }

    public double getSize() {
        return size.get();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    /**
     * Sets the icon's square size in pixels.
     *
     * @param size a positive size in pixels
     * @throws IllegalArgumentException if {@code size} is not positive and finite
     */
    public void setSize(double size) {
        if (!Double.isFinite(size) || size <= 0) {
            throw new IllegalArgumentException("Icon size must be a positive finite number");
        }
        this.size.set(size);
    }

    public Paint getFill() {
        return svgPath.getFill();
    }

    public ObjectProperty<Paint> fillProperty() {
        return svgPath.fillProperty();
    }

    public void setFill(Paint fill) {
        svgPath.setFill(fill);
    }

    /**
     * Returns the SVGPath used to render this icon.
     *
     * @return the rendered SVG path
     */
    public SVGPath getSvgPath() {
        return svgPath;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.ICON;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        svgPath.getStyleClass().add(SVG_STYLE_CLASS);
        getChildren().add(svgPath);
        setAlignment(Pos.CENTER);
        updateType(null, getType());
        updateSize(getSize());
        type.addListener((observable, oldType, newType) -> updateType(oldType, newType));
        size.addListener((observable, oldSize, newSize) -> updateSize(newSize.doubleValue()));
        sceneBuilderIntegration();
    }

    private void updateType(EleFXIconType oldType, EleFXIconType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }

        EleFXIconType resolvedType = newType == null ? EleFXIconType.SEARCH : newType;
        if (!getStyleClass().contains(resolvedType.styleClass())) {
            getStyleClass().add(resolvedType.styleClass());
        }
        svgPath.setContent(resolvedType.svgPathData());
    }

    private void updateSize(double size) {
        if (!Double.isFinite(size) || size <= 0) {
            throw new IllegalArgumentException("Icon size must be a positive finite number");
        }

        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        double scale = size / VIEW_BOX_SIZE;
        svgPath.setScaleX(scale);
        svgPath.setScaleY(scale);
    }
}
