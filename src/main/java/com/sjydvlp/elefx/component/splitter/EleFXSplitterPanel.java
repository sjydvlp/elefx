package com.sjydvlp.elefx.component.splitter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/** A content pane managed by an {@link EleFXSplitter}. */
public class EleFXSplitterPanel extends StackPane {

    private static final String STYLE_CLASS = "ele-splitter-panel";

    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", Double.NaN);

    private final DoubleProperty min = new SimpleDoubleProperty(this, "min", 0);

    private final DoubleProperty max = new SimpleDoubleProperty(this, "max", Double.MAX_VALUE);

    private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", true);

    private final BooleanProperty collapsible = new SimpleBooleanProperty(this, "collapsible", false);

    private final BooleanProperty collapsed = new SimpleBooleanProperty(this, "collapsed", false);

    private double expandedSize = Double.NaN;

    public EleFXSplitterPanel() {
        initialize();
    }

    public EleFXSplitterPanel(Node... children) {
        super(children);
        initialize();
    }

    /** Current primary-axis size in pixels. {@code NaN} lets the splitter size the pane equally. */
    public double getSize() {
        return size.get();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public void setSize(double size) {
        this.size.set(validateSize("size", size, true));
    }

    public double getMin() {
        return min.get();
    }

    public DoubleProperty minProperty() {
        return min;
    }

    public void setMin(double min) {
        this.min.set(validateSize("min", min, false));
    }

    public double getMax() {
        return max.get();
    }

    public DoubleProperty maxProperty() {
        return max;
    }

    public void setMax(double max) {
        this.max.set(validateSize("max", max, false));
    }

    public boolean isResizable() {
        return resizable.get();
    }

    public BooleanProperty resizableProperty() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable.set(resizable);
    }

    public boolean isCollapsible() {
        return collapsible.get();
    }

    public BooleanProperty collapsibleProperty() {
        return collapsible;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible.set(collapsible);
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed && !isCollapsible()) return;
        this.collapsed.set(collapsed);
    }

    /** Toggles this panel if {@link #isCollapsible()} is enabled. */
    public void toggleCollapsed() {
        setCollapsed(!isCollapsed());
    }

    double expandedSize() {
        return expandedSize;
    }

    void setExpandedSize(double expandedSize) {
        this.expandedSize = expandedSize;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        min.addListener((observable, oldValue, value) -> validateRange());
        max.addListener((observable, oldValue, value) -> validateRange());
    }

    private void validateRange() {
        if (getMin() > getMax()) throw new IllegalArgumentException("min must not exceed max");
    }

    private static double validateSize(String name, double value, boolean allowNaN) {
        if ((allowNaN && Double.isNaN(value)) || (Double.isFinite(value) && value >= 0)) return value;
        throw new IllegalArgumentException(
                name + " must be a non-negative finite number" + (allowNaN ? " or NaN" : ""));
    }
}
