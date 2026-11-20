package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntToDoubleFunction;

import static com.sjydvlp.elefx.component.table.EleFXFixedColumnLayout.fixed;
import static com.sjydvlp.elefx.component.table.EleFXFixedColumnLayout.topLevel;

/** Resolves cell coverage and draws only merged cells intersecting the virtual viewport. */
final class EleFXTableSpans<S> extends Pane {

    static final PseudoClass SPAN_HOVER = PseudoClass.getPseudoClass("span-hover");

    private record Span(int row, int column, int rowEnd, int columnEnd) {

        boolean hidden() {
            return rowEnd == row;
        }
    }

    private final EleFXTable<S> table;

    private final Rectangle viewport = new Rectangle();

    private final Map<Long, Span> coverage = new HashMap<>();

    private final Map<Span, TableCell<S, ?>> rendered = new HashMap<>();

    private final InvalidationListener widthChanged = observable -> requestTableLayout();

    private List<TableColumn<S, ?>> columns = List.of();

    private boolean dirty = true;

    private int hoveredRow = -1;

    private int hoveredRowEnd = -1;

    EleFXTableSpans(EleFXTable<S> table) {
        this.table = table;
        setManaged(false);
        setPickOnBounds(false);
        setViewOrder(-1);
        setClip(viewport);
    }

    void invalidateSpans() {
        dirty = true;
        table.requestLayout();
    }

    void rebuildIfNeeded() {
        if (!dirty) {
            return;
        }
        dirty = false;
        reset();
        var method = table.getSpanMethod();
        var items = table.getItems();
        if (method == null || items == null) {
            return;
        }
        columns = List.copyOf(table.getVisibleLeafColumns());
        for (var column : columns) {
            column.widthProperty().addListener(widthChanged);
        }
        for (int row = 0; row < items.size(); row++) {
            for (int column = 0; column < columns.size(); column++) {
                long key = key(row, column);
                if (coverage.containsKey(key)) {
                    continue;
                }
                var requested = method.apply(new EleFXTable.SpanContext<>(
                        items.get(row), columns.get(column), row, column));
                if (requested == null) {
                    continue;
                }
                if (requested.rowSpan() == 0) {
                    coverage.put(key, new Span(row, column, row, column));
                    continue;
                }
                int rowEnd = row + Math.min(requested.rowSpan(), items.size() - row);
                int colEnd = column + Math.min(requested.columnSpan(), columns.size() - column);
                var band = fixed(topLevel(columns.get(column)));
                for (int i = column + 1; i < colEnd; i++) {
                    if (fixed(topLevel(columns.get(i))) != band) {
                        colEnd = i;
                        break;
                    }
                }
                if (rowEnd - row == 1 && colEnd - column == 1 || overlaps(row, column, rowEnd, colEnd)) {
                    continue;
                }
                var span = new Span(row, column, rowEnd, colEnd);
                for (int r = row; r < rowEnd; r++) {
                    for (int c = column; c < colEnd; c++) {
                        coverage.put(key(r, c), span);
                    }
                }
            }
        }
    }

    boolean covers(int index, int column) {
        return coverage.containsKey(key(index, column));
    }

    int rowEnd(TableCell<?, ?> cell) {
        var span = coverage.get(key(cell.getIndex(), columns.indexOf(cell.getTableColumn())));
        return span == null ? cell.getIndex() + 1 : span.rowEnd();
    }

    void hoverRows(int start, int end) {
        if (hoveredRow != start || hoveredRowEnd != end) {
            hoveredRow = start;
            hoveredRowEnd = end;
            rendered.forEach(this::updateHover);
        }
    }

    private void updateHover(Span span, TableCell<S, ?> cell) {
        cell.pseudoClassStateChanged(SPAN_HOVER,
                hoveredRow >= 0 && hoveredRow < span.rowEnd() && hoveredRowEnd > span.row());
    }

    void position(List<TableRow<S>> visibleRows, double width, double height, double offset,
                  EleFXFixedColumnLayout fixedColumns, IntToDoubleFunction measureRow) {
        resizeRelocate(0, 0, width, height);
        viewport.setWidth(width);
        viewport.setHeight(height);
        var rows = new HashMap<Integer, TableRow<S>>();
        var spans = new HashSet<Span>();
        for (var row : visibleRows) {
            if (row.isEmpty()) {
                continue;
            }
            rows.put(row.getIndex(), row);
            for (int column = 0; column < columns.size(); column++) {
                var span = coverage.get(key(row.getIndex(), column));
                if (span != null && !span.hidden()) {
                    spans.add(span);
                }
            }
        }
        rendered.entrySet().removeIf(entry -> {
            if (spans.contains(entry.getKey())) {
                return false;
            }
            removeCell(entry.getValue());
            return true;
        });
        if (rows.isEmpty() || spans.isEmpty()) {
            return;
        }
        int first = java.util.Collections.min(rows.keySet());
        double firstY = sceneToLocal(rows.get(first).localToScene(0, 0)).getY();
        var heights = new HashMap<Integer, Double>();
        double[] edges = new double[columns.size() + 1];
        for (int i = 0; i < columns.size(); i++) {
            edges[i + 1] = edges[i] + snapSizeX(columns.get(i).getWidth());
        }
        for (var span : spans) {
            int visibleStart = Math.max(span.row(), first);
            var start = rows.get(visibleStart);
            double y = start == null ? firstY : sceneToLocal(start.localToScene(0, 0)).getY();
            double spanHeight = 0;
            for (int index = span.row(); index < span.rowEnd(); index++) {
                double rowHeight = heights.computeIfAbsent(index,
                        key -> rows.containsKey(key) ? rows.get(key).getHeight() : measureRow.applyAsDouble(key));
                spanHeight += rowHeight;
                if (index < visibleStart) {
                    y -= rowHeight;
                }
            }
            var column = columns.get(span.column());
            var placement = fixedColumns.get(column);
            var cell = rendered.computeIfAbsent(span, key -> createCell(key, column));
            double x = placement == null ? edges[span.column()] - offset : placement.x();
            cell.resizeRelocate(x, y, edges[span.columnEnd()] - edges[span.column()], spanHeight);
            if (placement != null) {
                EleFXFixedColumnLayout.clip(cell, placement.min() - x, placement.max() - x);
            } else {
                EleFXFixedColumnLayout.clearClip(cell);
            }
        }
    }

    private <T> TableCell<S, T> createCell(Span span, TableColumn<S, T> column) {
        var cell = column.getCellFactory().call(column);
        var row = new TableRow<S>();
        row.updateTableView(table);
        row.updateIndex(span.row());
        cell.setManaged(false);
        cell.getStyleClass().add("ele-table__span-cell");
        cell.updateTableView(table);
        cell.updateTableColumn(column);
        cell.updateTableRow(row);
        cell.updateIndex(span.row());
        updateHover(span, cell);
        getChildren().add(cell);
        return cell;
    }

    private void removeCell(TableCell<S, ?> cell) {
        getChildren().remove(cell);
        cell.updateIndex(-1);
        cell.getTableRow().updateIndex(-1);
        cell.getTableRow().updateTableView(null);
        cell.updateTableRow(null);
        cell.updateTableColumn(null);
        cell.updateTableView(null);
    }

    void reset() {
        for (var column : columns) {
            column.widthProperty().removeListener(widthChanged);
        }
        columns = List.of();
        coverage.clear();
        rendered.values().forEach(this::removeCell);
        rendered.clear();
    }

    private boolean overlaps(int row, int column, int rowEnd, int colEnd) {
        for (int r = row; r < rowEnd; r++) {
            for (int c = column; c < colEnd; c++) {
                if (coverage.containsKey(key(r, c))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long key(int row, int column) {
        return ((long) row << 32) | column;
    }

    private void requestTableLayout() {
        table.requestLayout();
    }
}
