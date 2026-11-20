package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** A footer rendered outside the scrolling row viewport. */
final class EleFXTableSummary<S> extends Pane {

    private static final PseudoClass ADJACENT_ROW = PseudoClass.getPseudoClass("adjacent-row");

    private final EleFXTable<S> table;

    private final Rectangle viewport = new Rectangle();

    private final List<Label> cells = new ArrayList<>();

    private final Set<ObservableValue<?>> observedValues = Collections.newSetFromMap(new IdentityHashMap<>());

    private final InvalidationListener valueChanged = observable -> invalidateContent();

    private final WeakInvalidationListener weakValueChanged = new WeakInvalidationListener(valueChanged);

    private final InvalidationListener columnWidthChanged = observable -> requestTableLayout();

    private List<TableColumn<S, ?>> columns = List.of();

    private boolean dirty = true;

    EleFXTableSummary(EleFXTable<S> table) {
        this.table = table;
        getStyleClass().add("ele-table__summary");
        setManaged(false);
        setClip(viewport);
    }

    void invalidateContent() {
        if (!dirty && table.isShowSummary()) {
            dirty = true;
            table.requestLayout();
        }
    }

    void setAdjacentToRow(boolean value) {
        pseudoClassStateChanged(ADJACENT_ROW, value);
    }

    double measure() {
        if (!table.isShowSummary()) {
            return 0;
        }
        if (table.getVisibleLeafColumns().isEmpty()) {
            if (!columns.isEmpty()) {
                reset();
            }
            return 0;
        }
        if (dirty) {
            updateContent();
            applyCss();
        }
        double height = minHeight(-1);
        for (int i = 0; i < cells.size(); i++) {
            height = Math.max(height, cells.get(i).prefHeight(columns.get(i).getWidth()));
        }
        return snapSizeY(height);
    }

    void position(double x, double y, double width, double height, double offset,
                  EleFXFixedColumnLayout fixedColumns) {
        setVisible(height > 0);
        if (height <= 0) {
            return;
        }
        resizeRelocate(x, y, width, height);
        viewport.setWidth(width);
        viewport.setHeight(height);
        double columnX = -offset;
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            var cell = cells.get(i);
            double columnWidth = snapSizeX(column.getWidth());
            var placement = fixedColumns.get(column);
            cell.resizeRelocate(placement == null ? columnX : placement.x(), 0, columnWidth, height);
            if (placement == null) {
                EleFXFixedColumnLayout.clearClip(cell);
            } else {
                EleFXFixedColumnLayout.clip(cell, placement.min() - placement.x(), placement.max() - placement.x());
            }
            columnX += columnWidth;
        }
    }

    void reset() {
        dirty = true;
        clearObservedValues();
        cells.forEach(cell -> cell.setGraphic(null));
        cells.clear();
        observeColumns(List.of());
        getChildren().clear();
    }

    private void updateContent() {
        dirty = false;
        clearObservedValues();
        observeColumns(List.copyOf(table.getVisibleLeafColumns()));
        var source = table.getTreeItems();
        List<S> data = source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
        var method = table.getSummaryMethod();
        var values = new ArrayList<Object>();
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            BigDecimal sum = null;
            for (S row : data) {
                if (row == null || (method == null && i == 0)) {
                    continue;
                }
                var value = column.getCellObservableValue(row);
                if (value != null) {
                    Object cellValue = value.getValue();
                    if (observedValues.add(value)) {
                        value.addListener(weakValueChanged);
                    }
                    if (method == null) {
                        BigDecimal numeric = parseNumber(cellValue);
                        if (numeric != null) {
                            sum = sum == null ? numeric : sum.add(numeric);
                        }
                    }
                }
            }
            if (method == null) {
                values.add(i == 0 ? table.getSumText() : sum == null ? null : sum.stripTrailingZeros().toPlainString());
            }
        }
        List<?> summary = method == null ? values : method.apply(new EleFXTable.SummaryContext<>(columns, data));
        cells.forEach(cell -> cell.setGraphic(null));
        while (cells.size() > columns.size()) {
            getChildren().remove(cells.remove(cells.size() - 1));
        }
        for (int i = 0; i < columns.size(); i++) {
            if (i == cells.size()) {
                var cell = new Label();
                cell.getStyleClass().add("ele-table__summary-cell");
                cell.setManaged(false);
                cells.add(cell);
                getChildren().add(cell);
            }
            Object content = summary == null || i >= summary.size() ? null : summary.get(i);
            var cell = cells.get(i);
            cell.setGraphic(content instanceof Node node ? node : null);
            cell.setText(content == null || content instanceof Node ? null : content.toString());
        }
    }

    private void requestTableLayout() {
        table.requestLayout();
    }

    private void observeColumns(List<TableColumn<S, ?>> next) {
        if (columns.equals(next)) {
            return;
        }
        for (var column : columns) {
            column.widthProperty().removeListener(columnWidthChanged);
            column.cellValueFactoryProperty().removeListener(valueChanged);
        }
        columns = next;
        for (var column : columns) {
            column.widthProperty().addListener(columnWidthChanged);
            column.cellValueFactoryProperty().addListener(valueChanged);
        }
    }

    private void clearObservedValues() {
        observedValues.forEach(value -> value.removeListener(weakValueChanged));
        observedValues.clear();
    }

    private static BigDecimal parseNumber(Object value) {
        if (value instanceof BigDecimal number) {
            return number;
        }
        if (!(value instanceof Number || value instanceof CharSequence)) {
            return null;
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
