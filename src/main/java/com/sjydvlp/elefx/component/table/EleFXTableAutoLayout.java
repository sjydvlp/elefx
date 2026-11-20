package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Content measurement and temporary column widths for the automatic layout. */
final class EleFXTableAutoLayout<S> {

    private static final double DEFAULT_COLUMN_WIDTH = new TableColumn<>().getPrefWidth();

    private final EleFXTable<S> table;

    private final Label probe = new Label();

    private final Text measure = new Text();

    private final Map<TableColumn<S, ?>, ColumnWidth> widths = new IdentityHashMap<>();

    private final List<Observable> columnProperties = new ArrayList<>();

    private final Set<ObservableValue<?>> values = Collections.newSetFromMap(new IdentityHashMap<>());

    private final InvalidationListener contentChanged = observable -> invalidate();

    private final WeakInvalidationListener weakContentChanged = new WeakInvalidationListener(contentChanged);

    private List<TableColumn<S, ?>> columns = List.of();

    private double[] naturalWidths = new double[0];

    private boolean dirty = true;

    private boolean applying;

    private static final class ColumnWidth {

        double configured;

        double applied;

        ColumnWidth(double configured) {
            this.configured = configured;
            this.applied = configured;
        }
    }

    EleFXTableAutoLayout(EleFXTable<S> table) {
        this.table = table;
        probe.setManaged(false);
        probe.setVisible(false);
        probe.getStyleClass().add("table-cell");
        probe.fontProperty().addListener(contentChanged);
        probe.insetsProperty().addListener(contentChanged);
    }

    Node probe() {
        return probe;
    }

    void invalidate() {
        if (!applying && !dirty && table.getTableLayout() == EleFXTableLayout.AUTO) {
            dirty = true;
            table.requestLayout();
        }
    }

    void apply(double available) {
        if (table.getTableLayout() != EleFXTableLayout.AUTO || available <= 0) {
            return;
        }
        applying = true;
        try {
            if (dirty) {
                measureColumns();
                dirty = false;
            }
            double[] targets = naturalWidths.clone();
            double remaining = Math.floor(available);
            var growable = new ArrayList<Integer>();
            for (int i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                if (hasConfiguredWidth(column)) {
                    targets[i] = column.getWidth();
                } else if (canResize(column) && targets[i] < column.getMaxWidth()) {
                    growable.add(i);
                } else if (!canResize(column)) {
                    targets[i] = column.getWidth();
                }
                remaining -= targets[i];
            }
            distributeExtra(targets, remaining, growable);
            for (int i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                if (canResize(column) && !hasConfiguredWidth(column)) {
                    var width = widths.computeIfAbsent(column, key -> new ColumnWidth(key.getPrefWidth()));
                    if (column.getPrefWidth() != width.applied) {
                        width.configured = column.getPrefWidth();
                    }
                    width.applied = targets[i];
                    column.setPrefWidth(width.applied);
                }
            }
        } finally {
            applying = false;
        }
    }

    private void distributeExtra(double[] targets, double remaining, List<Integer> growable) {
        while (remaining >= 1 && !growable.isEmpty()) {
            double share = Math.max(1, Math.floor(remaining / growable.size()));
            var iterator = growable.iterator();
            while (iterator.hasNext() && remaining >= 1) {
                int i = iterator.next();
                double room = columns.get(i).getMaxWidth() - targets[i];
                double extra = Math.min(room, Math.min(remaining, share));
                targets[i] += extra;
                remaining -= extra;
                if (room <= extra) {
                    iterator.remove();
                }
            }
        }
    }

    boolean hasConfiguredWidth(TableColumn<S, ?> column) {
        var width = widths.get(column);
        double configured = width != null && column.getPrefWidth() == width.applied
                ? width.configured
                : column.getPrefWidth();
        return configured != DEFAULT_COLUMN_WIDTH || column.prefWidthProperty().isBound()
                || column.getMinWidth() == column.getMaxWidth();
    }

    private void measureColumns() {
        detachMeasurements();
        columns = List.copyOf(table.getVisibleLeafColumns());
        widths.entrySet().removeIf(entry -> {
            if (columns.contains(entry.getKey()) && canResize(entry.getKey())) {
                return false;
            }
            restore(entry.getKey(), entry.getValue());
            return true;
        });
        probe.applyCss();
        Font font = probe.getFont();
        Font headerFont = Font.font(font.getFamily(), FontWeight.BOLD, font.getSize());
        double padding = probe.snappedLeftInset() + probe.snappedRightInset();
        naturalWidths = new double[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            observe(column.textProperty(), column.graphicProperty(), column.cellValueFactoryProperty(),
                    column.cellFactoryProperty(),
                    column.minWidthProperty(), column.maxWidthProperty(), column.prefWidthProperty(),
                    column.resizableProperty(), column.sortableProperty());
            if (!canResize(column)) {
                continue;
            }
            measure.setFont(headerFont);
            double content = contentWidth(column.getText()) + contentWidth(column.getGraphic());
            if (column.isSortable()) {
                content += 24;
            }
            measure.setFont(font);
            var renderer = column instanceof EleFXTableColumn<S, ?> eleColumn ? eleColumn.graphicRenderer() : null;
            if (table.getItems() != null) {
                for (S item : table.getItems()) {
                    if (item == null) {
                        continue;
                    }
                    var observable = column.getCellObservableValue(item);
                    if (observable != null) {
                        Object value = observable.getValue();
                        if (values.add(observable)) {
                            observable.addListener(weakContentChanged);
                        }
                        if (renderer == null) {
                            content = Math.max(content, contentWidth(value));
                        }
                    }
                    if (renderer != null) {
                        Node graphic = renderer.apply(item);
                        if (graphic != null) {
                            probe.setGraphic(graphic);
                            try {
                                probe.applyCss();
                                content = Math.max(content, graphic.prefWidth(-1));
                            } finally {
                                probe.setGraphic(null);
                            }
                        }
                    }
                }
            }
            naturalWidths[i] = Math.max(column.getMinWidth(),
                    Math.min(column.getMaxWidth(), Math.ceil(content + padding)));
        }
    }

    private double contentWidth(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Node node) {
            return node.prefWidth(-1);
        }
        measure.setText(value.toString());
        return measure.getLayoutBounds().getWidth();
    }

    private boolean canResize(TableColumn<S, ?> column) {
        return column.isResizable() && !column.prefWidthProperty().isBound()
                && column.getMaxWidth() > column.getMinWidth();
    }

    void reset() {
        applying = true;
        try {
            detachMeasurements();
            widths.forEach(this::restore);
            widths.clear();
            columns = List.of();
            naturalWidths = new double[0];
            dirty = true;
        } finally {
            applying = false;
        }
    }

    private void restore(TableColumn<S, ?> column, ColumnWidth width) {
        if (!column.prefWidthProperty().isBound() && column.getPrefWidth() == width.applied) {
            column.setPrefWidth(width.configured);
        }
    }

    private void observe(Observable... properties) {
        for (var property : properties) {
            columnProperties.add(property);
            property.addListener(weakContentChanged);
        }
    }

    private void detachMeasurements() {
        columnProperties.forEach(property -> property.removeListener(weakContentChanged));
        columnProperties.clear();
        values.forEach(value -> value.removeListener(weakContentChanged));
        values.clear();
    }
}
