package com.sjydvlp.elefx.component.table;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.shape.Rectangle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

final class EleFXFixedColumnLayout {

    private static final Object CLIP_KEY = new Object();

    private final Map<TableColumnBase<?, ?>, Placement> placements = new LinkedHashMap<>();

    private double middleStart;

    private double middleEnd;

    void update(List<? extends TableColumn<?, ?>> leaves, double width, double scroll,
                DoubleUnaryOperator snapWidth) {
        placements.clear();
        Map<TableColumnBase<?, ?>, Double> groups = new LinkedHashMap<>();
        double leftWidth = 0;
        double middleWidth = 0;
        double rightWidth = 0;
        for (TableColumn<?, ?> leaf : leaves) {
            double columnWidth = snapWidth.applyAsDouble(leaf.getWidth());
            TableColumnBase<?, ?> group = topLevel(leaf);
            groups.merge(group, columnWidth, Double::sum);
            switch (fixed(group)) {
                case LEFT -> leftWidth += columnWidth;
                case RIGHT -> rightWidth += columnWidth;
                case NONE -> middleWidth += columnWidth;
            }
        }
        if (leftWidth + rightWidth == 0) {
            return;
        }

        double left = 0;
        double middle = leftWidth - scroll;
        double right = Math.max(0, Math.min(width - rightWidth, leftWidth + middleWidth));
        middleStart = Math.min(leftWidth, width);
        middleEnd = Math.max(middleStart, right);
        for (var group : groups.entrySet()) {
            double groupWidth = group.getValue();
            switch (fixed(group.getKey())) {
                case LEFT -> {
                    placements.put(group.getKey(), new Placement(left, 0, middleStart));
                    left += groupWidth;
                }
                case NONE -> {
                    placements.put(group.getKey(), new Placement(middle, middleStart, middleEnd));
                    middle += groupWidth;
                }
                case RIGHT -> {
                    placements.put(group.getKey(), new Placement(right, middleEnd, width));
                    right += groupWidth;
                }
            }
        }
        Map<TableColumnBase<?, ?>, Double> offsets = new LinkedHashMap<>();
        for (TableColumn<?, ?> leaf : leaves) {
            TableColumnBase<?, ?> group = topLevel(leaf);
            if (group != leaf) {
                Placement placement = placements.get(group);
                double offset = offsets.getOrDefault(group, 0.0);
                placements.put(leaf, new Placement(placement.x() + offset, placement.min(), placement.max()));
                offsets.put(group, offset + snapWidth.applyAsDouble(leaf.getWidth()));
            }
        }
    }

    Placement get(TableColumnBase<?, ?> column) {
        return placements.get(column);
    }

    boolean isActive() {
        return !placements.isEmpty();
    }

    double middleStart() {
        return middleStart;
    }

    double middleEnd() {
        return middleEnd;
    }

    static TableColumnBase<?, ?> topLevel(TableColumnBase<?, ?> column) {
        while (column.getParentColumn() != null) {
            column = column.getParentColumn();
        }
        return column;
    }

    static EleFXTableColumnFixed fixed(TableColumnBase<?, ?> column) {
        return column instanceof EleFXTableColumn<?, ?> eleColumn ? eleColumn.getFixed() : EleFXTableColumnFixed.NONE;
    }

    static void clip(Node node, double min, double max) {
        Bounds bounds = node.getLayoutBounds();
        if (min <= bounds.getMinX() && max >= bounds.getMaxX()) {
            clearClip(node);
            return;
        }
        ColumnClip state = (ColumnClip) node.getProperties().get(CLIP_KEY);
        if (state == null || node.getClip() != state.clip()) {
            Node original = node.getClip();
            Rectangle viewport = new Rectangle();
            node.setClip(null);
            viewport.setClip(original);
            state = new ColumnClip(viewport, original);
            node.getProperties().put(CLIP_KEY, state);
            node.setClip(state.clip());
        }
        Rectangle clip = state.clip();
        clip.setX(Math.max(bounds.getMinX(), min));
        clip.setWidth(Math.max(0, Math.min(bounds.getMaxX(), max) - clip.getX()));
        clip.setY(bounds.getMinY());
        clip.setHeight(bounds.getHeight());
    }

    static void clearClip(Node node) {
        if (node.hasProperties() && node.getProperties().remove(CLIP_KEY)instanceof ColumnClip state) {
            state.clip().setClip(null);
            if (node.getClip() == state.clip()) {
                node.setClip(state.original());
            }
        }
    }

    record Placement(double x, double min, double max) {
    }

    private record ColumnClip(Rectangle clip, Node original) {
    }
}
