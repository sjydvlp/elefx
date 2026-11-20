package com.sjydvlp.elefx.component.table;

import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

final class EleFXNestedTableHeader extends NestedTableColumnHeader {

    private static final Cursor COLUMN_RESIZE = createColumnResizeCursor();

    private static Cursor createColumnResizeCursor() {
        Canvas canvas = new Canvas(32, 32);
        var graphics = canvas.getGraphicsContext2D();
        graphics.scale(32.0 / 1024, 32.0 / 1024);
        graphics.appendSVGPath(EleFXIconType.COLUMN_RESIZE.svgPathData());
        graphics.fill();
        var snapshot = new SnapshotParameters();
        snapshot.setFill(Color.TRANSPARENT);
        return new ImageCursor(canvas.snapshot(snapshot, null), 10, 8);
    }

    EleFXNestedTableHeader(TableColumnBase<?, ?> column) {
        super(column);
        useColumnResizeCursor(this);
    }

    private static void useColumnResizeCursor(Node node) {
        node.cursorProperty().addListener((observable, previous, current) -> {
            if (current == Cursor.H_RESIZE) {
                node.setCursor(COLUMN_RESIZE);
            }
        });
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        for (var child : getChildren()) {
            if (child instanceof Rectangle handle && !handle.mouseTransparentProperty().isBound()
                    && handle.getProperties().get("TableColumn")instanceof TableColumn<?, ?> column
                    && column.getTableView()instanceof EleFXTable<?> table) {
                handle.mouseTransparentProperty().bind(table.borderedProperty().not());
                useColumnResizeCursor(handle);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected TableColumnHeader createTableColumnHeader(TableColumnBase column) {
        if (column instanceof TableColumn<?, ?> tableColumn) {
            if (column != getTableColumn() && !column.getColumns().isEmpty()) {
                return new EleFXNestedTableHeader(column);
            }
            return new SortableHeader(tableColumn);
        }
        return super.createTableColumnHeader(column);
    }

    private static final class SortableHeader extends TableColumnHeader {

        private static final PseudoClass SORT_CARETS = PseudoClass.getPseudoClass("sort-carets");

        private static final PseudoClass SORTABLE = PseudoClass.getPseudoClass("sortable");

        private static final PseudoClass FILTER_TRIGGER = PseudoClass.getPseudoClass("filter-trigger");

        private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

        private static final double CARET_GAP = 7;

        private final TableColumn<?, ?> column;

        private final Region ascending = new Region();

        private final Region descending = new Region();

        private final VBox carets = new VBox(3, ascending, descending);

        private final InvalidationListener appearanceChanged = observable -> updateAppearance();

        SortableHeader(TableColumn<?, ?> column) {
            super(column);
            this.column = column;
            ascending.getStyleClass().add("ele-table__sort-ascending");
            descending.getStyleClass().add("ele-table__sort-descending");
            carets.setManaged(false);
            carets.setPickOnBounds(false);
            configureCaret(ascending, TableColumn.SortType.ASCENDING);
            configureCaret(descending, TableColumn.SortType.DESCENDING);
            // Reuse the native header's sorting and mouse handlers.
            column.sortableProperty().addListener(new WeakInvalidationListener(appearanceChanged));
            column.sortNodeProperty().addListener(new WeakInvalidationListener(appearanceChanged));
            column.graphicProperty().addListener(new WeakInvalidationListener(appearanceChanged));
            updateAppearance();
        }

        private void configureCaret(Region caret, TableColumn.SortType direction) {
            caret.addEventHandler(MouseEvent.MOUSE_PRESSED, SortableHeader::consumePrimaryButton);
            caret.addEventHandler(MouseEvent.MOUSE_RELEASED, SortableHeader::consumePrimaryButton);
            caret.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                if (event.isPrimaryButtonDown()) {
                    event.consume();
                }
            });
            caret.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                    event.consume();
                    sortByCaret(column, direction);
                }
            });
        }

        private static void consumePrimaryButton(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY) {
                event.consume();
            }
        }

        private static <S> void sortByCaret(TableColumn<S, ?> column, TableColumn.SortType direction) {
            var table = column.getTableView();
            if (table == null || !column.isSortable() || column.getComparator() == null) {
                return;
            }
            var order = table.getSortOrder();
            if (order.contains(column) && column.getSortType() == direction) {
                order.remove(column);
                return;
            }
            column.setSortType(direction);
            if (order.size() != 1 || order.get(0) != column) {
                order.setAll(java.util.List.of(column));
            }
        }

        private void updateAppearance() {
            boolean sortable = column.isSortable() && column.getColumns().isEmpty();
            boolean visible = sortable && column.getSortNode() == null;
            carets.setVisible(visible);
            pseudoClassStateChanged(SORT_CARETS, visible);
            pseudoClassStateChanged(SORTABLE, sortable);
            pseudoClassStateChanged(FILTER_TRIGGER, hasFilterTrigger());
            requestLayout();
        }

        private boolean hasFilterTrigger() {
            return column instanceof EleFXTableFilterColumn<?, ?> filterColumn
                    && filterColumn.hasFilterTrigger();
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            if (!carets.isVisible()) {
                return;
            }
            // JavaFX rebuilds the header's children when the sort order changes.
            if (carets.getParent() != this) {
                getChildren().add(carets);
                carets.applyCss();
            }
            boolean sorted = column.getTableView() != null && column.getTableView().getSortOrder().contains(column);
            ascending.pseudoClassStateChanged(ACTIVE, sorted && column.getSortType() == TableColumn.SortType.ASCENDING);
            descending.pseudoClassStateChanged(ACTIVE,
                    sorted && column.getSortType() == TableColumn.SortType.DESCENDING);

            for (var child : getChildren()) {
                if (child instanceof Label label) {
                    double width = snapSizeX(carets.prefWidth(-1));
                    double height = snapSizeY(carets.prefHeight(width));
                    boolean hasFilter = hasFilterTrigger();
                    double trailingSpace = hasFilter
                            ? snapSizeX(label.getGraphic().prefWidth(-1)) + label.getGraphicTextGap()
                            : 0;
                    double labelWidth = Math.max(0,
                            getWidth() - snappedLeftInset() - snappedRightInset()
                                    - (hasFilter ? 0 : width + CARET_GAP));
                    label.resize(labelWidth, label.getHeight());
                    double textEnd = Math.min(labelWidth, label.prefWidth(-1))
                            - label.getInsets().getRight() - trailingSpace;
                    carets.resizeRelocate(
                            snapPositionX(label.getLayoutX() + Math.max(0, textEnd) + CARET_GAP),
                            snapPositionY((getHeight() - height) / 2), width, height);
                    break;
                }
            }
        }
    }
}
