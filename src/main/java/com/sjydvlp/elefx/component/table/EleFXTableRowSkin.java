package com.sjydvlp.elefx.component.table;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EleFXTableRowSkin<S> extends TableRowSkin<S> {

    private static final PseudoClass HOVERING_EXPANSION = PseudoClass.getPseudoClass("hovering-expansion");

    private Map<TableColumnBase<?, ?>, TableCell<S, ?>> cells;

    private final Set<TableCell<S, ?>> hiddenBySpan = new HashSet<>();

    private final EleFXTableSkin<S> tableSkin;

    private final ExpandedContent expansion = new ExpandedContent();

    private boolean positioned;

    EleFXTableRowSkin(TableRow<S> row, EleFXTableSkin<S> tableSkin) {
        super(row);
        this.tableSkin = tableSkin;
        expansion.getStyleClass().add("ele-table__expanded-content");
        expansion.setPadding(new Insets(20, 0, 20, 0));
        expansion.setManaged(false);
        expansion.setViewOrder(1);
        expansion.hoverProperty().addListener(
                (observable, previous, hovering) -> row.pseudoClassStateChanged(HOVERING_EXPANSION, hovering));
        registerInvalidationListener(row.itemProperty(), observable -> expansion.getChildren().clear());
    }

    @Override
    protected TableCell<S, ?> createCell(TableColumnBase column) {
        TableCell<S, ?> cell = super.createCell(column);
        // The superclass creates cells during construction and when its column cache is rebuilt.
        if (cells == null) {
            cells = new HashMap<>();
        }
        cells.put(column, cell);
        return cell;
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        Node content = expandedContent();
        double rowHeight = content == null ? height : Math.min(height, basePrefHeight(width));
        super.layoutChildren(x, y, width, rowHeight);
        if (cells != null) {
            cells.keySet().retainAll(getVisibleLeafColumns());
            if (content != null && !tableSkin.fixedColumns().isActive()) {
                // Keep the data cells in the top band even when the virtual row
                // itself is tall enough to contain the expanded renderer.
                for (TableCell<S, ?> cell : cells.values()) {
                    if (cell.getParent() == getSkinnable()) {
                        cell.resizeRelocate(cell.getLayoutX(), y, cell.getWidth(), rowHeight);
                    }
                }
            }
            positionCells(x, y, rowHeight);
            updateSpanVisibility();
        }
        layoutExpansion(content, x, y + rowHeight, width, Math.max(0, height - rowHeight));
    }

    void updateSpanVisibility() {
        if (cells == null) {
            return;
        }
        var row = getSkinnable();
        var columns = getVisibleLeafColumns();
        hiddenBySpan.removeIf(cell -> {
            int column = columns.indexOf(cell.getTableColumn());
            if (!row.isEmpty() && column >= 0 && tableSkin.isSpanCovered(row.getIndex(), column)) {
                return false;
            }
            cell.setVisible(true);
            return true;
        });
        if (!row.isEmpty()) {
            for (int i = 0; i < columns.size(); i++) {
                var cell = cells.get(columns.get(i));
                if (cell != null && cell.isVisible() && !cell.visibleProperty().isBound()
                        && tableSkin.isSpanCovered(row.getIndex(), i)) {
                    cell.setVisible(false);
                    hiddenBySpan.add(cell);
                }
            }
        }
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        double height = super.computePrefHeight(width, top, right, bottom, left);
        Node content = expandedContent();
        if (content == null) {
            return height;
        }
        double viewport = expansionWidth(width);
        double contentWidth = viewport < 0
                ? -1
                : Math.max(0, viewport - expansion.snappedLeftInset() - expansion.snappedRightInset());
        return height + expansion.snappedTopInset()
                + snapSizeY(content.prefHeight(contentWidth)) + expansion.snappedBottomInset();
    }

    private Node expandedContent() {
        var row = getSkinnable();
        return tableSkin == null || row.isEmpty() || row.getItem() == null
                || row.getTableView() == null || row.getTableView().getFixedCellSize() > 0
                        ? null
                        : tableSkin.expandedContent(row.getItem());
    }

    private double basePrefHeight(double width) {
        return super.computePrefHeight(width, 0, 0, 0, 0);
    }

    private double expansionWidth(double width) {
        double viewport = tableSkin.expansionWidth();
        return viewport > 0 ? viewport : width;
    }

    private void layoutExpansion(Node content, double x, double y, double width, double height) {
        if (content == null) {
            getSkinnable().pseudoClassStateChanged(HOVERING_EXPANSION, false);
            expansion.getChildren().clear();
            getChildren().remove(expansion);
            return;
        }
        if (content.getParent() != expansion) {
            if (content.getParent()instanceof ExpandedContent previous) {
                previous.getChildren().remove(content);
            }
            expansion.getChildren().setAll(content);
        }
        if (!getChildren().contains(expansion)) {
            getChildren().add(expansion);
        }
        expansion.resizeRelocate(x + tableSkin.horizontalOffset(), y, expansionWidth(width), height);
    }

    void positionCells() {
        TableRow<S> row = getSkinnable();
        double height = row.getHeight() - row.snappedTopInset() - row.snappedBottomInset();
        positionCells(row.snappedLeftInset(), row.snappedTopInset(),
                expandedContent() == null ? height : Math.min(height, basePrefHeight(row.getWidth())));
    }

    private void positionCells(double x, double y, double height) {
        EleFXFixedColumnLayout layout = tableSkin.fixedColumns();
        if (!layout.isActive()) {
            if (positioned) {
                cells.values().forEach(EleFXFixedColumnLayout::clearClip);
                positioned = false;
                getSkinnable().requestLayout();
            }
            return;
        }
        if (cells == null || getSkinnable().getIndex() < 0) {
            return;
        }
        positioned = true;
        double scroll = tableSkin.horizontalOffset();
        double fixedHeight = getSkinnable().getTableView().getFixedCellSize();
        for (TableColumn<S, ?> column : getVisibleLeafColumns()) {
            TableCell<S, ?> cell = cells.get(column);
            var placement = layout.get(column);
            if (cell == null || placement == null) {
                continue;
            }
            double cellWidth = snapSizeX(column.getWidth());
            if (cell.getParent() == null) {
                // Fixed-height native rows detach cells outside their original horizontal viewport.
                if (placement.x() + cellWidth <= placement.min() || placement.x() >= placement.max()) {
                    continue;
                }
                getChildren().add(cell);
            }
            cell.resizeRelocate(x + scroll + placement.x(), fixedHeight > 0 ? 0 : y,
                    cellWidth, fixedHeight > 0 ? fixedHeight : Math.max(0, height));
            EleFXFixedColumnLayout.clip(cell, placement.min() - placement.x(), placement.max() - placement.x());
        }
    }

    private static final class ExpandedContent extends StackPane {
    }
}
