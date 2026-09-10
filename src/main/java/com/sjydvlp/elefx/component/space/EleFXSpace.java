package com.sjydvlp.elefx.component.space;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * An Element Plus inspired layout pane which applies a uniform gap between its items.
 *
 * <p>
 * Items are added through {@link #getChildren()}, as with other EleFX layout
 * components. The default direction is horizontal with the Element Plus {@code small}
 * (8px) gap. In horizontal mode {@link #setWrap(boolean)} enables line wrapping.
 * </p>
 */
public class EleFXSpace extends Pane implements Themable {

    private static final String STYLE_CLASS = "ele-space";

    private static final String SPACER_STYLE_CLASS = "ele-space__spacer";

    private final ObjectProperty<Orientation> direction = new SimpleObjectProperty<>(this, "direction",
            Orientation.HORIZONTAL);

    private final ObjectProperty<Pos> alignment = new SimpleObjectProperty<>(this, "alignment", Pos.CENTER);

    private final ObjectProperty<EleFXSpaceSize> size = new SimpleObjectProperty<>(this, "size", EleFXSpaceSize.SMALL);

    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing", Double.NaN);

    private final BooleanProperty wrap = new SimpleBooleanProperty(this, "wrap", false);

    private final BooleanProperty fill = new SimpleBooleanProperty(this, "fill", false);

    private final DoubleProperty fillRatio = new SimpleDoubleProperty(this, "fillRatio", 100);

    private final ObjectProperty<Object> spacer = new SimpleObjectProperty<>(this, "spacer");

    private Node spacerNode;

    public EleFXSpace() {
        initialize();
    }

    public EleFXSpace(Node... children) {
        this();
        getChildren().addAll(children);
    }

    public Orientation getDirection() {
        return direction.get();
    }

    public ObjectProperty<Orientation> directionProperty() {
        return direction;
    }

    public void setDirection(Orientation direction) {
        this.direction.set(direction == null ? Orientation.HORIZONTAL : direction);
    }

    public Pos getAlignment() {
        return alignment.get();
    }

    public ObjectProperty<Pos> alignmentProperty() {
        return alignment;
    }

    public void setAlignment(Pos alignment) {
        this.alignment.set(alignment == null ? Pos.CENTER : alignment);
    }

    public EleFXSpaceSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXSpaceSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXSpaceSize size) {
        this.size.set(size == null ? EleFXSpaceSize.SMALL : size);
    }

    /** Sets an exact gap in pixels. Set {@link Double#NaN} to use the selected preset size. */
    public double getSpacing() {
        return spacing.get();
    }

    public DoubleProperty spacingProperty() {
        return spacing;
    }

    public void setSpacing(double spacing) {
        if (!Double.isNaN(spacing) && (!Double.isFinite(spacing) || spacing < 0)) {
            throw new IllegalArgumentException("spacing must be a non-negative finite number or NaN");
        }
        this.spacing.set(spacing);
    }

    public boolean isWrap() {
        return wrap.get();
    }

    public BooleanProperty wrapProperty() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap.set(wrap);
    }

    public boolean isFill() {
        return fill.get();
    }

    public BooleanProperty fillProperty() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill.set(fill);
    }

    public double getFillRatio() {
        return fillRatio.get();
    }

    public DoubleProperty fillRatioProperty() {
        return fillRatio;
    }

    public void setFillRatio(double fillRatio) {
        if (!Double.isFinite(fillRatio) || fillRatio <= 0)
            throw new IllegalArgumentException("fillRatio must be positive");
        this.fillRatio.set(fillRatio);
    }

    /**
     * An optional separator inserted between items. Accepted values are a {@link String},
     * {@link Number}, or an unparented {@link Node}. Text spacers repeat between every
     * pair of items; a node spacer is placed once (a JavaFX node cannot have multiple
     * positions in one scene graph).
     */
    public Object getSpacer() {
        return spacer.get();
    }

    public ObjectProperty<Object> spacerProperty() {
        return spacer;
    }

    public void setSpacer(Object spacer) {
        if (spacer != null && !(spacer instanceof String) && !(spacer instanceof Number) && !(spacer instanceof Node)) {
            throw new IllegalArgumentException("spacer must be a String, Number, Node, or null");
        }
        if (spacer instanceof Node node && node.getParent() != null && node != spacerNode) {
            throw new IllegalArgumentException("a Node spacer must not already have a parent");
        }
        this.spacer.set(spacer);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SPACE;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    @Override
    protected double computeMinWidth(double height) {
        return computeSize(true, true);
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeSize(false, true);
    }

    @Override
    protected double computePrefWidth(double height) {
        return computeSize(true, false);
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeSize(false, false);
    }

    @Override
    protected void layoutChildren() {
        List<Node> items = items();
        if (items.isEmpty()) return;
        boolean horizontal = getDirection() != Orientation.VERTICAL;
        double x0 = snappedLeftInset(), y0 = snappedTopInset();
        double availableW = Math.max(0, getWidth() - x0 - snappedRightInset());
        double availableH = Math.max(0, getHeight() - y0 - snappedBottomInset());
        double gap = resolvedGap();
        List<Line> lines = lines(items, horizontal, horizontal ? availableW : availableH, false);
        double totalCross = lines.stream().mapToDouble(line -> line.cross).sum() + gap * Math.max(0, lines.size() - 1);
        double crossCursor = (horizontal ? y0 : x0)
                + crossOffset(horizontal, horizontal ? availableH : availableW, totalCross);
        for (Line line : lines) {
            double primaryCursor = horizontal ? x0 : y0;
            double availablePrimary = horizontal ? availableW : availableH;
            double primaryOffset = primaryOffset(horizontal, availablePrimary, line.primary);
            primaryCursor += primaryOffset;
            for (Item item : line.items) {
                double childCross = item.cross;
                double childCrossOffset = crossItemOffset(horizontal, line.cross, childCross);
                if (horizontal)
                    item.node.resizeRelocate(primaryCursor, crossCursor + childCrossOffset, item.primary, childCross);
                else
                    item.node.resizeRelocate(crossCursor + childCrossOffset, primaryCursor, childCross, item.primary);
                primaryCursor += item.primary + gap;
            }
            crossCursor += line.cross + gap;
        }
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        direction.addListener(observable -> requestLayout());
        alignment.addListener(observable -> requestLayout());
        size.addListener(observable -> requestLayout());
        spacing.addListener(observable -> requestLayout());
        wrap.addListener(observable -> requestLayout());
        fill.addListener(observable -> requestLayout());
        fillRatio.addListener(observable -> requestLayout());
        spacer.addListener((observable, oldValue, newValue) -> updateSpacer(oldValue, newValue));
        getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> requestLayout());
        sceneBuilderIntegration();
    }

    private void updateSpacer(Object oldValue, Object newValue) {
        if (spacerNode != null) getChildren().remove(spacerNode);
        spacerNode = newValue instanceof Node node
                ? node
                : newValue == null ? null : new Label(String.valueOf(newValue));
        if (spacerNode != null) {
            spacerNode.getStyleClass().add(SPACER_STYLE_CLASS);
            getChildren().add(spacerNode);
        }
        requestLayout();
    }

    private List<Node> items() {
        List<Node> result = new ArrayList<>();
        for (Node child : getManagedChildren())
            if (child != spacerNode) result.add(child);
        if (spacerNode == null || result.size() < 2) return result;
        List<Node> separated = new ArrayList<>();
        for (Node node : result) {
            if (!separated.isEmpty() && (!(getSpacer() instanceof Node) || separated.size() == 1))
                separated.add(spacerNode);
            separated.add(node);
        }
        return separated;
    }

    private List<Line> lines(List<Node> items, boolean horizontal, double availablePrimary, boolean minimum) {
        List<Line> result = new ArrayList<>();
        Line line = new Line();
        double gap = resolvedGap();
        for (Node node : items) {
            boolean isSpacer = node == spacerNode;
            double primary = preferred(node, horizontal, minimum);
            if (!isSpacer && isFill() && availablePrimary > 0) primary = availablePrimary * getFillRatio() / 100.0;
            double cross = preferred(node, !horizontal, minimum);
            if (isWrap() && horizontal && !line.items.isEmpty() && line.primary + gap + primary > availablePrimary) {
                result.add(line);
                line = new Line();
            }
            if (!line.items.isEmpty()) line.primary += gap;
            line.items.add(new Item(node, primary, cross));
            line.primary += primary;
            line.cross = Math.max(line.cross, cross);
        }
        if (!line.items.isEmpty()) result.add(line);
        return result;
    }

    private double computeSize(boolean width, boolean minimum) {
        boolean horizontal = getDirection() != Orientation.VERTICAL;
        List<Line> lines = lines(items(), horizontal, -1, minimum);
        double gap = resolvedGap();
        double value;
        if (horizontal == width)
            value = lines.stream().mapToDouble(line -> line.primary).max().orElse(0);
        else
            value = lines.stream().mapToDouble(line -> line.cross).sum() + gap * Math.max(0, lines.size() - 1);
        return value + (width ? snappedLeftInset() + snappedRightInset() : snappedTopInset() + snappedBottomInset());
    }

    private double preferred(Node node, boolean width, boolean minimum) {
        return Math.max(0,
                width
                        ? (minimum ? node.minWidth(-1) : node.prefWidth(-1))
                        : (minimum ? node.minHeight(-1) : node.prefHeight(-1)));
    }

    private double resolvedGap() {
        return Double.isNaN(getSpacing()) ? getSize().pixels() : getSpacing();
    }

    private double primaryOffset(boolean horizontal, double available, double used) {
        return horizontal ? switch (getAlignment().getHpos()) {
            case CENTER -> (available - used) / 2;
            case RIGHT -> available - used;
            default -> 0;
        } : switch (getAlignment().getVpos()) {
            case CENTER -> (available - used) / 2;
            case BOTTOM -> available - used;
            default -> 0;
        };
    }

    private double crossOffset(boolean horizontal, double available, double used) {
        return horizontal ? switch (getAlignment().getVpos()) {
            case CENTER -> (available - used) / 2;
            case BOTTOM -> available - used;
            default -> 0;
        } : switch (getAlignment().getHpos()) {
            case CENTER -> (available - used) / 2;
            case RIGHT -> available - used;
            default -> 0;
        };
    }

    private double crossItemOffset(boolean horizontal, double lineCross, double itemCross) {
        return crossOffset(horizontal, lineCross, itemCross);
    }

    private static class Line {

        final List<Item> items = new ArrayList<>();

        double primary;

        double cross;
    }

    private record Item(Node node, double primary, double cross) {
    }
}
