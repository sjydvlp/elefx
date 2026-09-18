package com.sjydvlp.elefx.component.skeleton;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * A single Element Plus-inspired placeholder shape.
 *
 * <p>
 * Its preferred dimensions remain fully CSS-customizable; the variant only supplies
 * useful Element Plus defaults such as a circular avatar or a paragraph line.
 * </p>
 */
public class EleFXSkeletonItem extends Region implements Themable {

    private final ObjectProperty<EleFXSkeletonItemVariant> variant = new SimpleObjectProperty<>(this, "variant",
            EleFXSkeletonItemVariant.TEXT);

    private final DoubleProperty widthPercent = new SimpleDoubleProperty(this, "widthPercent", -1);

    private String variantClass;

    private boolean circleSizeBound;

    private boolean percentageWidthBound;

    private boolean imageWidthConstraintManaged;

    private boolean imageHeightConstraintManaged;

    private final EleFXIcon imagePlaceholderIcon = new EleFXIcon(EleFXIconType.PICTURE_FILLED, 30);

    public EleFXSkeletonItem() {
        initialize();
    }

    public EleFXSkeletonItem(EleFXSkeletonItemVariant variant) {
        setVariant(variant);
        initialize();
    }

    public EleFXSkeletonItemVariant getVariant() {
        return variant.get();
    }

    public ObjectProperty<EleFXSkeletonItemVariant> variantProperty() {
        return variant;
    }

    public void setVariant(EleFXSkeletonItemVariant value) {
        variant.set(value == null ? EleFXSkeletonItemVariant.TEXT : value);
    }

    /**
     * Width as a percentage of the immediate layout container, equivalent to Element
     * Plus CSS such as {@code width: 50%}. Set {@code -1} to resume normal sizing.
     */
    public double getWidthPercent() {
        return widthPercent.get();
    }

    public DoubleProperty widthPercentProperty() {
        return widthPercent;
    }

    public void setWidthPercent(double value) {
        if (!Double.isFinite(value) || value < -1 || value > 100) {
            throw new IllegalArgumentException("widthPercent must be between 0 and 100, or -1");
        }
        widthPercent.set(value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SKELETON;
    }

    private void initialize() {
        getStyleClass().add("ele-skeleton-item");
        imagePlaceholderIcon.getStyleClass().add("ele-skeleton-item__image-icon");
        // Use a direct fill instead of relying on stylesheet order. #DCDFE6 is the
        // Element Plus image-placeholder glyph colour over the #F0F2F5 skeleton fill.
        imagePlaceholderIcon.setFill(Color.web("#DCDFE6"));
        imagePlaceholderIcon.setManaged(false);
        imagePlaceholderIcon.setMouseTransparent(true);
        getChildren().add(imagePlaceholderIcon);
        variant.addListener(o -> refreshVariant());
        widthPercent.addListener(o -> refreshPercentageWidth());
        parentProperty().addListener((o, oldParent, newParent) -> refreshPercentageWidth());
        prefWidthProperty()
                .addListener((o, oldWidth, newWidth) -> synchronizeImageWidthConstraint(newWidth.doubleValue()));
        prefHeightProperty()
                .addListener((o, oldHeight, newHeight) -> synchronizeImageHeightConstraint(newHeight.doubleValue()));
        refreshVariant();
        refreshPercentageWidth();
        sceneBuilderIntegration();
    }

    private void refreshVariant() {
        releaseCircleSizeConstraint();
        if (variantClass != null) getStyleClass().remove(variantClass);
        variantClass = "ele-skeleton-item--" + getVariant().name().toLowerCase();
        getStyleClass().add(variantClass);
        if (getVariant() == EleFXSkeletonItemVariant.CIRCLE) {
            // VBox fills resizable children horizontally. Bind the maximum size to the
            // requested preferred size so setPrefSize(diameter, diameter) is respected.
            maxWidthProperty().bind(prefWidthProperty());
            maxHeightProperty().bind(prefHeightProperty());
            circleSizeBound = true;
        }
        imagePlaceholderIcon.setVisible(getVariant() == EleFXSkeletonItemVariant.IMAGE);
        synchronizeImageWidthConstraint(getPrefWidth());
        synchronizeImageHeightConstraint(getPrefHeight());
        refreshPercentageWidth();
        requestLayout();
    }

    private void releaseCircleSizeConstraint() {
        if (!circleSizeBound) return;
        maxWidthProperty().unbind();
        maxHeightProperty().unbind();
        circleSizeBound = false;
    }

    private void refreshPercentageWidth() {
        if (percentageWidthBound) {
            prefWidthProperty().unbind();
            maxWidthProperty().unbind();
            percentageWidthBound = false;
        }
        if (circleSizeBound && !maxWidthProperty().isBound()) {
            maxWidthProperty().bind(prefWidthProperty());
        }
        if (getWidthPercent() < 0 || !(getParent()instanceof Region parent)) return;

        // VBox fills resizable children horizontally; constrain the maximum width too
        // so the requested percentage behaves like Element Plus CSS.
        if (circleSizeBound) maxWidthProperty().unbind();
        prefWidthProperty().bind(parent.widthProperty().multiply(getWidthPercent() / 100.0));
        maxWidthProperty().bind(prefWidthProperty());
        percentageWidthBound = true;
    }

    private void synchronizeImageWidthConstraint(double width) {
        if (getVariant() != EleFXSkeletonItemVariant.IMAGE || maxWidthProperty().isBound()) return;
        if (Double.isFinite(width) && width >= 0) {
            setMaxWidth(width);
            imageWidthConstraintManaged = true;
        } else if (imageWidthConstraintManaged) {
            setMaxWidth(Region.USE_COMPUTED_SIZE);
            imageWidthConstraintManaged = false;
        }
    }

    private void synchronizeImageHeightConstraint(double height) {
        if (getVariant() != EleFXSkeletonItemVariant.IMAGE || maxHeightProperty().isBound()) return;
        if (Double.isFinite(height) && height >= 0) {
            setMaxHeight(height);
            imageHeightConstraintManaged = true;
        } else if (imageHeightConstraintManaged) {
            setMaxHeight(Region.USE_COMPUTED_SIZE);
            imageHeightConstraintManaged = false;
        }
    }

    @Override
    protected void layoutChildren() {
        double iconWidth = imagePlaceholderIcon.prefWidth(-1);
        double iconHeight = imagePlaceholderIcon.prefHeight(-1);
        imagePlaceholderIcon.resizeRelocate(
                (getWidth() - iconWidth) / 2,
                (getHeight() - iconHeight) / 2,
                iconWidth,
                iconHeight);
    }
}
