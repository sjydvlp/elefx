package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sjydvlp.elefx.component.table.EleFXFixedColumnLayout.fixed;
import static com.sjydvlp.elefx.component.table.EleFXFixedColumnLayout.topLevel;

final class EleFXTableSkin<S> extends TableViewSkin<S> {

    private static final double FIXED_SHADOW_WIDTH = 10;

    private static final double SCROLL_EDGE_TOLERANCE = 0.5;

    private final Region frame = new Region();

    private final ScrollBar emptyHorizontalBar = new ScrollBar();

    private final EventHandler<ScrollEvent> emptyScrollHandler = this::scrollEmptyTable;

    private final Region leftShadow = createFixedShadow("ele-table__fixed-shadow--left");

    private final Region rightShadow = createFixedShadow("ele-table__fixed-shadow--right");

    private final EleFXFixedColumnLayout fixedColumns = new EleFXFixedColumnLayout();

    private final EleFXTableSummary<S> summary;

    private final EleFXTableAutoLayout<S> autoLayout;

    private final EleFXTableSpans<S> spans;

    private ObservableList<S> summaryItems;

    private final InvalidationListener summaryChanged = observable -> refreshSummary();

    private final InvalidationListener itemsChanged = observable -> {
        refreshSummary();
        refreshSpans();
        refreshAutoLayout();
    };

    private final List<Observable> columnProperties = new ArrayList<>();

    private final InvalidationListener columnChanged = observable -> updateColumnLayout();

    private boolean hasFixedColumns;

    private boolean hasSpans;

    private final Set<TableRow<S>> spanHoverRows = new HashSet<>();

    private int spanHoverIndex = -1;

    private int spanHoverEnd = -1;

    private final EventHandler<MouseEvent> spanMouseMoved = this::updateSpanHover;

    private final EventHandler<MouseEvent> spanMouseExited = event -> {
        if (event.getTarget() == flow()) {
            clearSpanHover();
        }
    };

    private EleFXTableExpandColumn<S> expandColumn;

    private boolean positionedHeaders;

    private boolean showingRows;

    EleFXTableSkin(EleFXTable<S> table) {
        super(table);
        summary = new EleFXTableSummary<>(table);
        autoLayout = new EleFXTableAutoLayout<>(table);
        getChildren().add(autoLayout.probe());
        registerChangeListener(table.tableLayoutProperty(), observable -> {
            if (table.getTableLayout() == EleFXTableLayout.FIXED) {
                autoLayout.reset();
            }
            table.requestLayout();
        });
        registerChangeListener(table.sizeProperty(), observable -> refreshAutoLayout());
        spans = new EleFXTableSpans<>(table);
        flow().addSpanLayer(spans);
        flow().addEventFilter(MouseEvent.MOUSE_MOVED, spanMouseMoved);
        flow().addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, spanMouseMoved);
        flow().addEventHandler(MouseEvent.MOUSE_EXITED, spanMouseExited);
        registerChangeListener(table.spanMethodProperty(), observable -> updateColumnLayout());
        summary.setVisible(false);
        getChildren().add(summary);
        registerChangeListener(table.showSummaryProperty(), observable -> {
            if (!table.isShowSummary()) {
                summary.reset();
            }
            table.requestLayout();
        });
        registerChangeListener(table.summaryMethodProperty(), observable -> refreshSummary());
        registerChangeListener(table.sumTextProperty(), observable -> refreshSummary());
        registerChangeListener(table.itemsProperty(), observable -> observeSummaryItems());
        observeSummaryItems();
        table.tree().addInvalidationListener(summaryChanged);
        registerChangeListener(table.tableMenuButtonVisibleProperty(),
                observable -> getTableHeaderRow().requestLayout());
        var nativeFactory = flow().getCellFactory();
        flow().setCellFactory(flow -> {
            TableRow<S> row = nativeFactory.call(flow);
            if (usesCustomRows() && row.getSkin() == null) {
                installRowSkinOnAttach(row);
            }
            return row;
        });
        registerListChangeListener(table.getColumns(), change -> observeFixedColumns());
        registerListChangeListener(table.getVisibleLeafColumns(), change -> {
            updateColumnLayout();
            refreshSummary();
            refreshAutoLayout();
        });
        registerInvalidationListener(flow().horizontalBar().valueProperty(), observable -> {
            if (hasFixedColumns || hasSpans || table.isShowSummary()) {
                table.requestLayout();
            }
        });
        registerChangeListener(flow().positionProperty(), observable -> {
            if (hasSpans || table.isShowSummary()) {
                table.requestLayout();
            }
        });
        observeFixedColumns();

        frame.getStyleClass().add("ele-table__frame");
        frame.setManaged(false);
        frame.setMouseTransparent(true);
        frame.setViewOrder(-3);
        // The shadows overlay the header and extend into the body when it has rows.
        getTableHeaderRow().setViewOrder(1);
        getChildren().addAll(leftShadow, rightShadow, frame);
        emptyHorizontalBar.getStyleClass().add("ele-table__empty-scrollbar");
        emptyHorizontalBar.setManaged(false);
        emptyHorizontalBar.setVisible(false);
        emptyHorizontalBar.setFocusTraversable(false);
        emptyHorizontalBar.setViewOrder(-2);
        getChildren().add(emptyHorizontalBar);
        registerChangeListener(emptyHorizontalBar.valueProperty(), observable -> {
            if (emptyHorizontalBar.isVisible()) {
                layoutFixedColumns();
                layoutFixedShadows();
                if (table.isShowSummary()) {
                    table.requestLayout();
                }
            }
        });
        table.addEventFilter(ScrollEvent.SCROLL, emptyScrollHandler);
        frame.visibleProperty().bind(table.effectiveBorderedProperty());
        registerChangeListener(table.effectiveBorderedProperty(), observable -> table.requestLayout());
    }

    private void observeSummaryItems() {
        if (summaryItems != null) {
            summaryItems.removeListener(itemsChanged);
        }
        summaryItems = getSkinnable().getItems();
        if (summaryItems != null) {
            summaryItems.addListener(itemsChanged);
        }
        refreshSummary();
        refreshSpans();
        refreshAutoLayout();
    }

    void refreshSummary() {
        summary.invalidateContent();
    }

    void refreshAutoLayout() {
        autoLayout.invalidate();
    }

    void refreshSpans() {
        if (hasSpans) {
            spans.invalidateSpans();
        }
    }

    private boolean usesCustomRows() {
        return hasFixedColumns || expandColumn != null || hasSpans;
    }

    boolean isSpanCovered(int row, int column) {
        return hasSpans && spans.covers(row, column);
    }

    private void updateSpanHover(MouseEvent event) {
        if (!hasSpans) {
            return;
        }
        for (Node node = event.getTarget()instanceof Node target ? target : null; node != null
                && node != flow(); node = node.getParent()) {
            if (node instanceof TableCell<?, ?> cell && cell.getParent() == spans) {
                spanHoverIndex = cell.getIndex();
                spanHoverEnd = spans.rowEnd(cell);
                updateSpanHoverRow();
                spans.hoverRows(spanHoverIndex, spanHoverEnd);
                return;
            }
            if (node instanceof TableRow<?> row && row.getTableView() == getSkinnable()) {
                spanHoverIndex = -1;
                spanHoverEnd = -1;
                updateSpanHoverRow();
                spans.hoverRows(row.isEmpty() ? -1 : row.getIndex(), row.getIndex() + 1);
                return;
            }
        }
        clearSpanHover();
    }

    private void updateSpanHoverRow() {
        var visibleRows = flow().visibleRows();
        spanHoverRows.removeIf(row -> {
            if (isSpanHoverRow(row) && visibleRows.contains(row)) {
                return false;
            }
            row.pseudoClassStateChanged(EleFXTableSpans.SPAN_HOVER, false);
            return true;
        });
        if (spanHoverIndex >= 0) {
            for (var row : visibleRows) {
                if (isSpanHoverRow(row) && spanHoverRows.add(row)) {
                    row.pseudoClassStateChanged(EleFXTableSpans.SPAN_HOVER, true);
                }
            }
        }
    }

    private boolean isSpanHoverRow(TableRow<S> row) {
        return !row.isEmpty() && row.getIndex() >= spanHoverIndex && row.getIndex() < spanHoverEnd;
    }

    private void clearSpanHover() {
        spanHoverIndex = -1;
        spanHoverEnd = -1;
        updateSpanHoverRow();
        spans.hoverRows(-1, -1);
    }

    private static Region createFixedShadow(String styleClass) {
        Region shadow = new Region();
        shadow.getStyleClass().add(styleClass);
        shadow.setManaged(false);
        shadow.setMouseTransparent(true);
        shadow.setVisible(false);
        shadow.setViewOrder(-1);
        return shadow;
    }

    private void installRowSkinOnAttach(TableRow<S> row) {
        row.parentProperty().addListener(new ChangeListener<Parent>() {

            @Override
            public void changed(ObservableValue<? extends Parent> observable,
                                Parent oldParent, Parent parent) {
                for (Parent ancestor = parent; ancestor != null; ancestor = ancestor.getParent()) {
                    if (ancestor == flow()) {
                        row.parentProperty().removeListener(this);
                        if (row.getSkin() == null && usesCustomRows()) {
                            // JavaFX 17's TableRowSkin constructor looks up its enclosing VirtualFlow.
                            row.setSkin(new EleFXTableRowSkin<>(row, EleFXTableSkin.this));
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected VirtualFlow<TableRow<S>> createVirtualFlow() {
        return new TableFlow<>();
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        return new TableHeaderRow(this) {

            @Override
            protected NestedTableColumnHeader createRootHeader() {
                return new EleFXNestedTableHeader(null);
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                // JavaFX sizes the filler and clip from the control, even when the header is narrower.
                if (getClip()instanceof Rectangle clip) {
                    clip.setWidth(getWidth());
                }
                if (getSkinnable().isTableMenuButtonVisible()) {
                    Node corner = lookup(".show-hide-columns-button");
                    if (corner != null) {
                        corner.relocate(getWidth() - corner.getLayoutBounds().getWidth(), corner.getLayoutY());
                    }
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    private TableFlow<S> flow() {
        return (TableFlow<S>) getVirtualFlow();
    }

    private void observeFixedColumns() {
        columnProperties.forEach(property -> property.removeListener(columnChanged));
        columnProperties.clear();
        for (TableColumn<S, ?> column : getSkinnable().getColumns()) {
            if (column instanceof EleFXTableColumn<?, ?> eleColumn) {
                columnProperties.add(eleColumn.fixedProperty());
                eleColumn.fixedProperty().addListener(columnChanged);
            }
        }
        updateColumnLayout();
    }

    private void updateColumnLayout() {
        boolean active = getSkinnable().getVisibleLeafColumns().stream()
                .anyMatch(column -> fixed(topLevel(column)) != EleFXTableColumnFixed.NONE);
        EleFXTableExpandColumn<S> nextExpandColumn = null;
        for (TableColumn<S, ?> column : getSkinnable().getVisibleLeafColumns()) {
            if (column instanceof EleFXTableExpandColumn<?>) {
                @SuppressWarnings("unchecked")
                EleFXTableExpandColumn<S> expand = (EleFXTableExpandColumn<S>) column;
                nextExpandColumn = expand;
                break;
            }
        }
        boolean nextHasSpans = table().getSpanMethod() != null;
        if (active != hasFixedColumns || expandColumn != nextExpandColumn || nextHasSpans != hasSpans) {
            hasFixedColumns = active;
            expandColumn = nextExpandColumn;
            hasSpans = nextHasSpans;
            table().updateExpandColumn(expandColumn);
            flow().rebuildRows();
        }
        if (hasSpans) {
            refreshSpans();
        } else {
            clearSpanHover();
            spans.reset();
        }
        getSkinnable().requestLayout();
    }

    private EleFXTable<S> table() {
        return (EleFXTable<S>) getSkinnable();
    }

    Node expandedContent(S item) {
        return table().expandedContent(item);
    }

    void expansionChanged() {
        flow().refreshRowSizes();
        getSkinnable().requestLayout();
    }

    double expansionWidth() {
        return flow().getWidth();
    }

    EleFXFixedColumnLayout fixedColumns() {
        return fixedColumns;
    }

    double horizontalOffset() {
        ScrollBar horizontal = getItemCount() == 0 ? emptyHorizontalBar : flow().horizontalBar();
        return horizontal.isVisible() ? horizontal.getValue() : 0;
    }

    @Override
    protected void scrollHorizontally(TableColumn<S, ?> column) {
        if (emptyHorizontalBar.isVisible()) {
            if (column != null && fixed(topLevel(column)) == EleFXTableColumnFixed.NONE) {
                double x = 0;
                for (TableColumn<S, ?> leaf : getSkinnable().getVisibleLeafColumns()) {
                    if (leaf == column) {
                        emptyHorizontalBar.setValue(x);
                        break;
                    }
                    if (fixed(topLevel(leaf)) == EleFXTableColumnFixed.NONE) {
                        x += snapSizeX(leaf.getWidth());
                    }
                }
            }
            return;
        }
        if (!hasFixedColumns) {
            super.scrollHorizontally(column);
            return;
        }
        if (column == null || fixed(topLevel(column)) != EleFXTableColumnFixed.NONE) {
            return;
        }
        ScrollBar bar = flow().horizontalBar();
        var placement = fixedColumns.get(column);
        if (!bar.isVisible() || placement == null || placement.max() <= placement.min()) {
            return;
        }
        double width = snapSizeX(column.getWidth());
        double scroll = horizontalOffset();
        if (placement.x() < placement.min() || width >= placement.max() - placement.min()) {
            scroll += placement.x() - placement.min();
        } else if (placement.x() + width > placement.max()) {
            scroll += placement.x() + width - placement.max();
        }
        bar.setValue(Math.max(bar.getMin(), Math.min(bar.getMax(), scroll)));
    }

    private void layoutFixedColumns() {
        var headerRow = getTableHeaderRow();
        NestedTableColumnHeader root = headerRow.getRootHeader();
        boolean empty = getItemCount() == 0;
        if (empty) {
            headerRow.layout();
        }
        double headerOffset = hasFixedColumns ? 0 : horizontalOffset();
        root.setTranslateX(empty ? -root.getLayoutX() - headerOffset : 0);
        if (!hasFixedColumns && !positionedHeaders) {
            return;
        }
        headerRow.layout();
        double viewportWidth = empty ? headerRow.getWidth() : flow().viewportWidth();
        double rootX = root.getLayoutX() + root.getTranslateX();
        fixedColumns.update(getSkinnable().getVisibleLeafColumns(), viewportWidth,
                horizontalOffset(), this::snapSizeX);
        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof TableColumnHeader header) {
                var placement = fixedColumns.get(header.getTableColumn());
                positionHeaderNode(header, placement, header.getLayoutX(), rootX);
            } else if (child instanceof Rectangle handle) {
                // JavaFX puts each top-level column's resize handle alongside its header.
                var column = handle.getProperties().get("TableColumn");
                var placement = column instanceof TableColumn<?, ?> tableColumn ? fixedColumns.get(tableColumn) : null;
                if (placement == null) {
                    positionHeaderNode(handle, null, 0, 0);
                    continue;
                }
                for (TableColumnHeader header : root.getColumnHeaders()) {
                    if (header.getTableColumn() == column) {
                        positionHeaderNode(handle, placement, header.getLayoutX(), rootX);
                        break;
                    }
                }
            }
        }
        positionedHeaders = fixedColumns.isActive();
        for (TableRow<S> row : flow().visibleRows()) {
            if (row.getSkin()instanceof EleFXTableRowSkin<?> skin) {
                skin.positionCells();
            }
        }
    }

    private void positionHeaderNode(Node node, EleFXFixedColumnLayout.Placement placement,
                                    double columnX, double rootX) {
        if (placement == null) {
            node.setTranslateX(0);
            EleFXFixedColumnLayout.clearClip(node);
            return;
        }
        double translation = placement.x() - rootX - columnX;
        node.setTranslateX(translation);
        double nodeX = rootX + node.getLayoutX() + translation;
        EleFXFixedColumnLayout.clip(node, placement.min() - nodeX, placement.max() - nodeX);
    }

    private void layoutFixedShadows() {
        boolean empty = getItemCount() == 0;
        ScrollBar bar = empty ? emptyHorizontalBar : flow().horizontalBar();
        var header = getTableHeaderRow();
        double viewportWidth = empty ? header.getWidth() : flow().viewportWidth();
        double middleStart = fixedColumns.middleStart();
        double middleEnd = fixedColumns.middleEnd();
        boolean show = fixedColumns.isActive() && bar.isVisible() && middleEnd > middleStart;
        leftShadow.setVisible(show && middleStart > 0 && bar.getValue() - bar.getMin() > SCROLL_EDGE_TOLERANCE);
        rightShadow.setVisible(show && middleEnd < viewportWidth
                && bar.getMax() - bar.getValue() > SCROLL_EDGE_TOLERANCE);
        if (leftShadow.isVisible() || rightShadow.isVisible()) {
            double x = header.getLayoutX();
            double y = header.getLayoutY();
            double bottom = y + header.getHeight();
            if (!empty) {
                bottom = flow().getLayoutY() + flow().getHeight();
                TableRow<S> lastRow = flow().getVisibleCell(getItemCount() - 1);
                if (lastRow != null) {
                    double rowBottom = getSkinnable().sceneToLocal(lastRow.localToScene(0, lastRow.getHeight())).getY();
                    bottom = Math.min(bottom, snapPositionY(rowBottom));
                }
            }
            double height = Math.max(0, bottom - y);
            double width = Math.min(snapSizeX(FIXED_SHADOW_WIDTH), middleEnd - middleStart);
            leftShadow.resizeRelocate(x + middleStart, y, width, height);
            rightShadow.resizeRelocate(x + middleEnd - width, y, width, height);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        autoLayout.apply(width);
        var table = getSkinnable();
        var columns = table.getVisibleLeafColumns();
        if (table.getColumnResizePolicy() == TableView.UNCONSTRAINED_RESIZE_POLICY
                && !columns.isEmpty() && columns.stream().allMatch(autoLayout::hasConfiguredWidth)) {
            width = Math.min(width, Math.ceil(columns.stream().mapToDouble(TableColumn::getWidth).sum()));
        }
        if (hasSpans) {
            spans.rebuildIfNeeded();
        }
        double summaryHeight = Math.min(height, summary.measure());
        double bodyHeight = height - summaryHeight;
        boolean hasRows = getItemCount() > 0;
        boolean leavingEmptyState = emptyHorizontalBar.isVisible() && hasRows;
        ScrollBar rowBar = flow().horizontalBar();
        double previousOffset = showingRows ? rowBar.getValue() : emptyHorizontalBar.getValue();
        super.layoutChildren(x, y, width, bodyHeight);
        layoutEmptyHorizontalBar(x, y, width, bodyHeight);
        if ((hasFixedColumns || hasSpans || leavingEmptyState || summaryHeight > 0) && flow().isVisible()) {
            flow().layout();
        }
        if (leavingEmptyState && rowBar.isVisible()) {
            rowBar.setValue(Math.max(rowBar.getMin(), Math.min(rowBar.getMax(), previousOffset)));
        } else if (showingRows && !hasRows && emptyHorizontalBar.isVisible()) {
            emptyHorizontalBar.setValue(Math.min(emptyHorizontalBar.getMax(), previousOffset));
        }
        showingRows = hasRows;
        layoutFixedColumns();
        if (hasSpans && flow().isVisible()) {
            updateSpanHoverRow();
            var visible = flow().visibleRows();
            for (var row : visible) {
                if (row.getSkin()instanceof EleFXTableRowSkin<?> skin) {
                    skin.updateSpanVisibility();
                }
            }
            spans.position(visible, flow().viewportWidth(), flow().getHeight(),
                    horizontalOffset(), fixedColumns, this::measureSpanRow);
        }
        double bodyBottom = y + bodyHeight;
        double summaryTop = bodyBottom;
        boolean adjacentToRow = false;
        if (summaryHeight > 0 && hasRows && flow().isVisible()) {
            boolean overflow = flow().hasVerticalOverflow();
            TableRow<S> lastRow = overflow
                    ? flow().getLastVisibleCell()
                    : flow().getVisibleCell(getItemCount() - 1);
            if (lastRow != null && !lastRow.isEmpty()) {
                double lastRowBottom = rowBottom(lastRow);
                if (!overflow) {
                    summaryTop = Math.min(bodyBottom, Math.max(y, lastRowBottom));
                }
                adjacentToRow = lastRowBottom == summaryTop;
            }
        }
        // Keep the overlay scrollbar above the footer when the rows fit with room to spare.
        rowBar.setTranslateY(rowBar.isVisible() ? summaryTop - bodyBottom : 0);
        summary.setAdjacentToRow(adjacentToRow);
        summary.position(x, summaryTop, width, summaryHeight, horizontalOffset(), fixedColumns);
        layoutFixedShadows();
        if (!frame.isVisible()) {
            return;
        }

        double frameHeight = getSkinnable().getHeight();
        if (summaryHeight > 0) {
            frameHeight = Math.min(frameHeight, summaryTop + summaryHeight + snappedBottomInset());
        } else if (hasRows && flow().isVisible()) {
            TableFlow<S> flow = flow();
            flow.layout();
            // Draw the bottom edge at the clipped viewport instead of below its last divider.
            frameHeight = Math.min(frameHeight, snapPositionY(flow.getLayoutY() + flow.getHeight()));
            if (getSkinnable().getPrefHeight() == Region.USE_COMPUTED_SIZE && !flow.hasVerticalOverflow()) {
                TableRow<S> lastRow = flow.getVisibleCell(getItemCount() - 1);
                if (lastRow != null) {
                    frameHeight = Math.min(frameHeight, rowBottom(lastRow));
                }
            }
        }
        frame.resizeRelocate(0, 0, width + snappedLeftInset() + snappedRightInset(), frameHeight);
    }

    private double rowBottom(TableRow<S> row) {
        return snapPositionY(getSkinnable().sceneToLocal(row.localToScene(0, row.getHeight())).getY());
    }

    private double measureSpanRow(int index) {
        double fixedHeight = getSkinnable().getFixedCellSize();
        if (fixedHeight > 0) {
            return fixedHeight;
        }
        TableRow<S> row = flow().getCell(index);
        if (row.getItem() != getSkinnable().getItems().get(index)) {
            row.updateIndex(index);
            row.applyCss();
        }
        return snapSizeY(row.prefHeight(-1));
    }

    private void layoutEmptyHorizontalBar(double x, double y, double width, double height) {
        double columnsWidth = 0;
        if (getItemCount() == 0) {
            boolean constrained = getSkinnable().getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY;
            double requiredWidth = 0;
            for (TableColumn<S, ?> column : getSkinnable().getVisibleLeafColumns()) {
                columnsWidth += column.getWidth();
                requiredWidth += column.isResizable() ? column.getMinWidth() : column.getWidth();
            }
            // Constrained columns can retain rounded or stale widths as the flow switches to its placeholder.
            if (constrained && requiredWidth <= width) {
                columnsWidth = width;
            }
        }
        double overflow = Math.max(0, snapPositionX(columnsWidth) - snapPositionX(width));
        emptyHorizontalBar.setVisible(overflow > 0 && width > 0);
        if (!emptyHorizontalBar.isVisible()) {
            emptyHorizontalBar.setValue(0);
            return;
        }
        emptyHorizontalBar.setMax(overflow);
        emptyHorizontalBar.setValue(Math.min(emptyHorizontalBar.getValue(), overflow));
        emptyHorizontalBar.setVisibleAmount(overflow * width / columnsWidth);
        emptyHorizontalBar.setBlockIncrement(width);
        double insetX = Math.min(snapSizeX(TableFlow.SCROLLBAR_INSET), width / 2);
        double insetY = snapSizeY(TableFlow.SCROLLBAR_INSET);
        double barHeight = Math.min(snapSizeY(TableFlow.SCROLLBAR_THICKNESS),
                Math.max(0, height - getTableHeaderRow().getHeight() - insetY));
        emptyHorizontalBar.resizeRelocate(x + insetX, y + height - insetY - barHeight,
                Math.max(0, width - 2 * insetX - TableFlow.trailingScrollBarSpace(emptyHorizontalBar)), barHeight);
    }

    private void scrollEmptyTable(ScrollEvent event) {
        if (!emptyHorizontalBar.isVisible()) {
            return;
        }
        double delta = event.getDeltaX();
        if (delta == 0 && event.isShiftDown()) {
            delta = event.getDeltaY();
        }
        if (delta != 0) {
            double value = emptyHorizontalBar.getValue() - delta;
            emptyHorizontalBar.setValue(Math.max(0, Math.min(emptyHorizontalBar.getMax(), value)));
            event.consume();
        }
    }

    @Override
    public void dispose() {
        if (summaryItems != null) {
            summaryItems.removeListener(itemsChanged);
        }
        table().tree().removeInvalidationListener(summaryChanged);
        summary.reset();
        autoLayout.reset();
        getChildren().remove(autoLayout.probe());
        clearSpanHover();
        flow().removeEventFilter(MouseEvent.MOUSE_MOVED, spanMouseMoved);
        flow().removeEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, spanMouseMoved);
        flow().removeEventHandler(MouseEvent.MOUSE_EXITED, spanMouseExited);
        spans.reset();
        getSkinnable().removeEventFilter(ScrollEvent.SCROLL, emptyScrollHandler);
        columnProperties.forEach(property -> property.removeListener(columnChanged));
        columnProperties.clear();
        frame.visibleProperty().unbind();
        getChildren().removeAll(frame, emptyHorizontalBar, leftShadow, rightShadow, summary);
        super.dispose();
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        double viewportLimit = super.computePrefHeight(width, top, right, bottom, left);
        double height = top + snapSizeY(getTableHeaderRow().prefHeight(-1)) + bottom + summary.measure();
        int itemCount = getItemCount();
        if (itemCount == 0 || getSkinnable().getVisibleLeafColumns().isEmpty()) {
            Node placeholder = getSkinnable().getPlaceholder();
            double placeholderHeight = placeholder == null ? 0 : snapSizeY(placeholder.prefHeight(width));
            return height + Math.max(60, placeholderHeight);
        }

        double bodyHeight = 0;
        double fixedCellSize = getSkinnable().getFixedCellSize();
        if (fixedCellSize > 0) {
            bodyHeight += itemCount * fixedCellSize;
        } else {
            // Reuse VirtualFlow's measuring cell and stop once the viewport is full.
            VirtualFlow<TableRow<S>> flow = getVirtualFlow();
            var items = getSkinnable().getItems();
            for (int index = 0; index < itemCount && height + bodyHeight < viewportLimit; index++) {
                TableRow<S> row = flow.getCell(index);
                if (items != null && index < items.size() && row.getItem() != items.get(index)) {
                    // A visible or cached cell may still describe the row before insertion.
                    row.updateIndex(index);
                    row.applyCss();
                }
                bodyHeight += snapSizeY(row.prefHeight(-1));
            }
        }

        // TableViewSkin floors the body viewport to a whole logical pixel.
        // Leave room for that rounding and the parent's device-pixel snapping.
        return Math.min(viewportLimit, height + Math.ceil(bodyHeight) + snapSizeY(1));
    }

    private static final class TableFlow<S> extends VirtualFlow<TableRow<S>> {

        private static final double SCROLLBAR_INSET = 2;

        private static final double SCROLLBAR_THICKNESS = 6;

        private static final PseudoClass OVERFLOWING = PseudoClass.getPseudoClass("overflowing");

        TableFlow() {
            getHbar().setViewOrder(-2);
            getVbar().setViewOrder(-3);
        }

        void addSpanLayer(Pane layer) {
            getChildren().add(layer);
        }

        @Override
        protected void layoutChildren() {
            restoreNativeScrollBarSizes();
            super.layoutChildren();
            layoutOverlayScrollBars();
        }

        @Override
        public double scrollPixels(double delta) {
            if (!hasVerticalOverflow()) {
                return 0;
            }
            restoreNativeScrollBarSizes();
            double scrolled = super.scrollPixels(delta);
            // VirtualFlow also updates the native bar bounds during vertical scrolling.
            layoutOverlayScrollBars();
            return scrolled;
        }

        private void restoreNativeScrollBarSizes() {
            // VirtualFlow reads the actual bar dimensions when it updates its clipped viewport.
            ScrollBar horizontal = getHbar();
            ScrollBar vertical = getVbar();
            horizontal.resize(horizontal.getWidth(), horizontal.prefHeight(horizontal.getWidth()));
            vertical.resize(vertical.prefWidth(vertical.getHeight()), vertical.getHeight());
        }

        private double viewportWidth() {
            ScrollBar vertical = getVbar();
            double reservedWidth = vertical.isVisible() ? snapSizeX(vertical.prefWidth(-1)) : 0;
            return Math.max(0, getWidth() - reservedWidth);
        }

        private void layoutOverlayScrollBars() {
            ScrollBar horizontal = getHbar();
            ScrollBar vertical = getVbar();
            double insetX = Math.min(snapSizeX(SCROLLBAR_INSET), getWidth());
            double insetY = Math.min(snapSizeY(SCROLLBAR_INSET), getHeight());
            double height = Math.min(snapSizeY(SCROLLBAR_THICKNESS), getHeight() - insetY);
            if (horizontal.isVisible()) {
                horizontal.resizeRelocate(insetX, getHeight() - insetY - height,
                        Math.max(0, getWidth() - 2 * insetX - trailingScrollBarSpace(horizontal)), height);
            }
            boolean verticalOverflow = hasVerticalOverflow();
            vertical.pseudoClassStateChanged(OVERFLOWING, verticalOverflow);
            vertical.setMouseTransparent(!verticalOverflow);
            if (verticalOverflow) {
                double width = Math.min(snapSizeX(SCROLLBAR_THICKNESS), getWidth() - insetX);
                vertical.resizeRelocate(getWidth() - insetX - width, insetY, width,
                        Math.max(0, getHeight() - 2 * insetY - trailingScrollBarSpace(vertical)));
            }
        }

        private static double trailingScrollBarSpace(ScrollBar bar) {
            // Element Plus leaves a little travel unused because it subtracts both track insets
            // when computing thumb movement. The gap scales with the visible fraction.
            double range = bar.getMax() - bar.getMin();
            return range > 0 ? 2 * SCROLLBAR_INSET * Math.min(1, bar.getVisibleAmount() / range) : 0;
        }

        private boolean hasVerticalOverflow() {
            if (!getVbar().isVisible() || getCellCount() == 0) {
                return false;
            }
            double tolerance = snapSizeY(1);
            if (getFixedCellSize() > 0) {
                return getCellCount() * getFixedCellSize() > getHeight() + tolerance;
            }
            TableRow<S> first = getVisibleCell(0);
            TableRow<S> last = getVisibleCell(getCellCount() - 1);
            return first == null || last == null || first.getLayoutY() < -tolerance
                    || last.getLayoutY() + last.getHeight() > getHeight() + tolerance;
        }

        ScrollBar horizontalBar() {
            return getHbar();
        }

        ScrollBar verticalBar() {
            return getVbar();
        }

        List<TableRow<S>> visibleRows() {
            return getCells();
        }

        void rebuildRows() {
            recreateCells();
        }

        void refreshRowSizes() {
            for (TableRow<S> row : getCells()) {
                row.requestLayout();
            }
            reconfigureCells();
        }
    }
}
