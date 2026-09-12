package com.sjydvlp.elefx.component.form;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

/**
 * Element Plus inspired form layout. Form items are added through {@link #getItems()}.
 * It supports vertical and wrapping inline layouts, shared label alignment, and validation helpers.
 */
public class EleFXForm extends Region implements Themable {

    private static final String STYLE_CLASS = "ele-form";

    private final BooleanProperty inline = new SimpleBooleanProperty(this, "inline", false);

    private final ObjectProperty<EleFXFormLabelPosition> labelPosition = new SimpleObjectProperty<>(this,
            "labelPosition", EleFXFormLabelPosition.RIGHT);

    private final DoubleProperty labelWidth = new SimpleDoubleProperty(this, "labelWidth", -1);

    public EleFXForm() {
        initialize();
    }

    public EleFXForm(EleFXFormItem... items) {
        this();
        getItems().addAll(items);
    }

    public boolean isInline() {
        return inline.get();
    }

    public BooleanProperty inlineProperty() {
        return inline;
    }

    public void setInline(boolean value) {
        inline.set(value);
    }

    public EleFXFormLabelPosition getLabelPosition() {
        return labelPosition.get();
    }

    public ObjectProperty<EleFXFormLabelPosition> labelPositionProperty() {
        return labelPosition;
    }

    public void setLabelPosition(EleFXFormLabelPosition value) {
        labelPosition.set(value == null ? EleFXFormLabelPosition.RIGHT : value);
    }

    /** Negative values automatically match the widest inherited label. */
    public double getLabelWidth() {
        return labelWidth.get();
    }

    public DoubleProperty labelWidthProperty() {
        return labelWidth;
    }

    public void setLabelWidth(double value) {
        labelWidth.set(value < 0 ? -1 : value);
    }

    public ObservableList<EleFXFormItem> getItems() {
        @SuppressWarnings("unchecked")
        ObservableList<EleFXFormItem> items = (ObservableList<EleFXFormItem>) (ObservableList<?>) getChildren();
        return items;
    }

    /** Validates every item and returns true only when all fields are valid. */
    public boolean validate() {
        boolean valid = true;
        for (EleFXFormItem item : getItems())
            valid &= item.validate();
        return valid;
    }

    /** Validates the item with the supplied property name. */
    public boolean validateField(String prop) {
        for (EleFXFormItem item : getItems()) {
            if (item.getProp().equals(prop)) return item.validate();
        }
        return true;
    }

    public void clearValidate() {
        getItems().forEach(EleFXFormItem::clearValidate);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.FORM;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        inline.addListener(observable -> updateLayout());
        labelPosition.addListener(observable -> updateLayout());
        labelWidth.addListener(observable -> updateLayout());
        getChildren().addListener((ListChangeListener<Node>) observable -> updateLayout());
        updateLayout();
        sceneBuilderIntegration();
    }

    private void updateLayout() {
        double resolvedLabelWidth = getLabelWidth() >= 0
                ? getLabelWidth()
                : getItems().stream().filter(item -> item.getLabelWidth() < 0)
                        .mapToDouble(EleFXFormItem::labelPrefWidth).max().orElse(-1);
        for (EleFXFormItem item : getItems()) {
            item.applyFormDefaults(getLabelPosition(), resolvedLabelWidth);
        }
        getStyleClass().remove("ele-form--inline");
        if (isInline()) getStyleClass().add("ele-form--inline");
        requestLayout();
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + getManagedChildren().stream().mapToDouble(node -> node.minWidth(-1)).max().orElse(0)
                + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        double contentWidth;
        if (isInline()) {
            contentWidth = getManagedChildren().stream().mapToDouble(node -> node.prefWidth(-1)).sum();
            contentWidth += Math.max(0, getManagedChildren().size() - 1) * 32;
        } else
            contentWidth = getManagedChildren().stream().mapToDouble(node -> node.prefWidth(-1)).max().orElse(0);
        return snappedLeftInset() + contentWidth + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeHeight(width, true);
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeHeight(width, false);
    }

    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double contentWidth = Math.max(0, getWidth() - left - snappedRightInset());
        if (!isInline()) {
            double y = top;
            for (Node item : getManagedChildren()) {
                double height = item.prefHeight(contentWidth);
                item.resizeRelocate(left, y, contentWidth, height);
                y += height + 18;
            }
            return;
        }
        double x = left;
        double y = top;
        double rowHeight = 0;
        for (Node item : getManagedChildren()) {
            double width = Math.min(contentWidth, item.prefWidth(-1));
            double height = item.prefHeight(width);
            if (x > left && x + width > left + contentWidth) {
                x = left;
                y += rowHeight + 18;
                rowHeight = 0;
            }
            item.resizeRelocate(x, y, width, height);
            x += width + 32;
            rowHeight = Math.max(rowHeight, height);
        }
    }

    private double computeHeight(double width, boolean minimum) {
        double contentWidth = width < 0
                ? computePrefWidth(-1) - snappedLeftInset() - snappedRightInset()
                : Math.max(0, width - snappedLeftInset() - snappedRightInset());
        if (!isInline()) {
            double height = getManagedChildren().stream()
                    .mapToDouble(node -> minimum ? node.minHeight(contentWidth) : node.prefHeight(contentWidth)).sum();
            return snappedTopInset() + height + Math.max(0, getManagedChildren().size() - 1) * 18
                    + snappedBottomInset();
        }
        double x = 0;
        double height = 0;
        double rowHeight = 0;
        for (Node item : getManagedChildren()) {
            double itemWidth = Math.min(contentWidth, minimum ? item.minWidth(-1) : item.prefWidth(-1));
            double itemHeight = minimum ? item.minHeight(itemWidth) : item.prefHeight(itemWidth);
            if (x > 0 && x + itemWidth > contentWidth) {
                height += rowHeight + 18;
                x = 0;
                rowHeight = 0;
            }
            x += itemWidth + 32;
            rowHeight = Math.max(rowHeight, itemHeight);
        }
        return snappedTopInset() + height + rowHeight + snappedBottomInset();
    }
}
