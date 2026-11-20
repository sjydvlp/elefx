package com.sjydvlp.elefx.component.table;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Element Plus styling for JavaFX's virtualized table. By default the table
 * fits short lists and scrolls when they exceed the standard table viewport.
 * Set a preferred height explicitly to use a different viewport.
 */
public class EleFXTable<S> extends TableView<S> implements Themable {

    private static final PseudoClass STRIPE = PseudoClass.getPseudoClass("stripe");

    private static final PseudoClass BORDERED = PseudoClass.getPseudoClass("bordered");

    private static final PseudoClass GROUPED = PseudoClass.getPseudoClass("grouped");

    private static final PseudoClass LARGE = PseudoClass.getPseudoClass("large");

    private static final PseudoClass SMALL = PseudoClass.getPseudoClass("small");

    private static final PseudoClass HIGHLIGHT_CURRENT_ROW = PseudoClass.getPseudoClass("highlight-current-row");

    private static final PseudoClass KEYBOARD_NAVIGATION = PseudoClass.getPseudoClass("keyboard-navigation");

    private static final PseudoClass POINTER_FOCUS = PseudoClass.getPseudoClass("pointer-focus");

    private static final PseudoClass COLUMN_MENU_VISIBLE = PseudoClass.getPseudoClass("column-menu-visible");

    private final BooleanProperty showOverflowTooltip = new SimpleBooleanProperty(this, "showOverflowTooltip");

    private final ObjectProperty<Function<EleFXTableTooltipData<S, ?>, ?>> tooltipFormatter = new SimpleObjectProperty<>(
            this, "tooltipFormatter");

    private final BooleanProperty stripe = new SimpleBooleanProperty(this, "stripe");

    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered");

    private final BooleanBinding grouped = Bindings.createBooleanBinding(
            () -> getVisibleLeafColumns().stream().anyMatch(column -> column.getParentColumn() != null),
            getVisibleLeafColumns());

    private final BooleanBinding effectiveBordered = bordered.or(grouped);

    private final BooleanProperty highlightCurrentRow = new SimpleBooleanProperty(this, "highlightCurrentRow");

    private final BooleanProperty preserveExpandedContent = new SimpleBooleanProperty(this, "preserveExpandedContent");

    private final ObjectProperty<EleFXTableLayout> tableLayout = new SimpleObjectProperty<>(this, "tableLayout",
            EleFXTableLayout.FIXED);

    private final BooleanProperty showSummary = new SimpleBooleanProperty(this, "showSummary");

    private final StringProperty sumText = new SimpleStringProperty(this, "sumText", "Sum");

    private final ObjectProperty<Function<SummaryContext<S>, ? extends List<?>>> summaryMethod = new SimpleObjectProperty<>(
            this, "summaryMethod");

    private final ObjectProperty<Function<SpanContext<S>, CellSpan>> spanMethod = new SimpleObjectProperty<>(this,
            "spanMethod");

    private final Set<S> expandedRows = Collections.newSetFromMap(new IdentityHashMap<>());

    private final Map<S, Node> expandedContent = new IdentityHashMap<>();

    private final InvalidationListener expansionItemsChanged = observable -> retainExpansionItems();

    private final WeakInvalidationListener weakExpansionItemsChanged = new WeakInvalidationListener(
            expansionItemsChanged);

    private EleFXTableExpandColumn<S> expandColumn;

    private final ObjectProperty<EleFXTableSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXTableSize.DEFAULT);

    private final StringProperty emptyText = new SimpleStringProperty(this, "emptyText", "No Data");

    private FilteredList<S> filteredItems;

    private SortedList<S> sortedFilterItems;

    private final EleFXTableTree<S> tree = new EleFXTableTree<>(this);

    /** Enables overflow tooltips for EleFX columns in this table. */
    public final boolean isShowOverflowTooltip() {
        return showOverflowTooltip.get();
    }

    public final void setShowOverflowTooltip(boolean value) {
        showOverflowTooltip.set(value);
    }

    public final BooleanProperty showOverflowTooltipProperty() {
        return showOverflowTooltip;
    }

    public final Function<EleFXTableTooltipData<S, ?>, ?> getTooltipFormatter() {
        return tooltipFormatter.get();
    }

    /** Sets the default formatter. Return text, a fresh Node, or null for the cell value. */
    public final void setTooltipFormatter(Function<EleFXTableTooltipData<S, ?>, ?> value) {
        tooltipFormatter.set(value);
    }

    public final ObjectProperty<Function<EleFXTableTooltipData<S, ?>, ?>> tooltipFormatterProperty() {
        return tooltipFormatter;
    }

    public EleFXTable() {
        this(FXCollections.observableArrayList());
    }

    public EleFXTable(ObservableList<S> items) {
        super(items);
        if (items != null) {
            items.addListener(weakExpansionItemsChanged);
        }
        itemsProperty().addListener((observable, previous, current) -> {
            if (previous != null) {
                previous.removeListener(weakExpansionItemsChanged);
            }
            if (current != null) {
                current.addListener(weakExpansionItemsChanged);
            }
            retainExpansionItems();
            if (sortedFilterItems != null && current != sortedFilterItems
                    && !tree.projects(sortedFilterItems, current)) {
                sortedFilterItems.comparatorProperty().unbind();
                sortedFilterItems = null;
                filteredItems = null;
            }
            tree.itemsChanged(current);
        });
        getVisibleLeafColumns().addListener((InvalidationListener) observable -> refreshColumnFilters());
        preserveExpandedContent.addListener(observable -> {
            if (!isPreserveExpandedContent()) {
                expandedContent.keySet().retainAll(expandedRows);
            }
        });
        getStyleClass().add("ele-table");
        tableMenuButtonVisibleProperty()
                .addListener(observable -> pseudoClassStateChanged(COLUMN_MENU_VISIBLE, isTableMenuButtonVisible()));
        setMaxHeight(USE_PREF_SIZE);
        stripe.addListener(observable -> pseudoClassStateChanged(STRIPE, isStripe()));
        grouped.addListener((observable, previous, current) -> pseudoClassStateChanged(GROUPED, current));
        effectiveBordered.addListener(
                (observable, previous, current) -> pseudoClassStateChanged(BORDERED, current));
        highlightCurrentRow
                .addListener(observable -> pseudoClassStateChanged(HIGHLIGHT_CURRENT_ROW, isHighlightCurrentRow()));
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> pseudoClassStateChanged(KEYBOARD_NAVIGATION, false));
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::ignoreHeaderDragWithinClickThreshold);
        addEventFilter(KeyEvent.KEY_PRESSED, event -> pseudoClassStateChanged(KEYBOARD_NAVIGATION, true));
        focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                pseudoClassStateChanged(KEYBOARD_NAVIGATION, false);
            }
        });
        size.addListener(observable -> {
            pseudoClassStateChanged(LARGE, getSize() == EleFXTableSize.LARGE);
            pseudoClassStateChanged(SMALL, getSize() == EleFXTableSize.SMALL);
        });

        Label emptyLabel = new Label();
        emptyLabel.getStyleClass().add("ele-table__empty");
        emptyLabel.textProperty().bind(emptyText);
        setPlaceholder(emptyLabel);
        sceneBuilderIntegration();
    }

    /** Sets the unique, stable key used to identify tree rows. Defaults to the row value. */
    public final void setRowKey(Function<? super S, ?> rowKey) {
        tree.setRowKey(rowKey);
    }

    /**
     * Supplies children already present in each row. The first data column's
     * default EleFX cells show the tree controls. Pass null to clear the getter.
     */
    public final void setChildrenGetter(Function<? super S, ? extends List<? extends S>> getter) {
        tree.setChildrenGetter(getter);
    }

    /** Identifies rows whose children can be fetched by the tree loader. */
    public final void setHasChildrenGetter(Predicate<? super S> getter) {
        tree.setHasChildrenGetter(getter);
    }

    /** Expands known branches by default. Unloaded branches still load on demand. */
    public final void setDefaultExpandAll(boolean expanded) {
        tree.setDefaultExpandAll(expanded);
    }

    /**
     * Loads children on first expansion. Call resolve once with the result, or with
     * null to allow a retry. The callback can be called from a background thread.
     */
    public final void setTreeLoader(BiConsumer<? super S, Consumer<List<? extends S>>> loader) {
        tree.setLoader(loader);
    }

    /**
     * Returns the observable root data. While tree mode is enabled, getItems()
     * contains the visible rows. Use setItems() to replace the root data.
     */
    public final ObservableList<S> getTreeItems() {
        return tree.isActive() ? tree.getSource() : getItems();
    }

    EleFXTableTree<S> tree() {
        return tree;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EleFXTableSkin<>(this);
    }

    public final EleFXTableLayout getTableLayout() {
        var layout = tableLayout.get();
        return layout == null ? EleFXTableLayout.FIXED : layout;
    }

    public final void setTableLayout(EleFXTableLayout layout) {
        tableLayout.set(layout);
    }

    public final ObjectProperty<EleFXTableLayout> tableLayoutProperty() {
        return tableLayout;
    }

    /** Visible leaf columns and displayed root data, in table order. */
    public record SummaryContext<S> (List<TableColumn<S, ?>> columns, List<S> data) {
    }

    public final boolean isShowSummary() {
        return showSummary.get();
    }

    public final void setShowSummary(boolean value) {
        showSummary.set(value);
    }

    public final BooleanProperty showSummaryProperty() {
        return showSummary;
    }

    /** Text in the first column of the default summary row. */
    public final String getSumText() {
        return sumText.get();
    }

    public final void setSumText(String value) {
        sumText.set(value);
    }

    public final StringProperty sumTextProperty() {
        return sumText;
    }

    public final Function<SummaryContext<S>, ? extends List<?>> getSummaryMethod() {
        return summaryMethod.get();
    }

    /**
     * Sets a method that returns one value per visible leaf column. A Node is
     * embedded as a cell graphic and other values are displayed as text.
     * Passing null restores automatic sums.
     */
    public final void setSummaryMethod(Function<SummaryContext<S>, ? extends List<?>> method) {
        summaryMethod.set(method);
    }

    public final ObjectProperty<Function<SummaryContext<S>, ? extends List<?>>> summaryMethodProperty() {
        return summaryMethod;
    }

    /** A displayed cell, indexed from zero in the current row and visible leaf column order. */
    public record SpanContext<S> (S row, TableColumn<S, ?> column, int rowIndex, int columnIndex) {
    }

    /** A positive span merges cells. Use (0, 0) to hide a cell explicitly. */
    public record CellSpan(int rowSpan, int columnSpan) {

        public CellSpan {
            if ((rowSpan < 1 || columnSpan < 1) && (rowSpan != 0 || columnSpan != 0)) {
                throw new IllegalArgumentException("Spans must be positive or both zero");
            }
        }
    }

    public final Function<SpanContext<S>, CellSpan> getSpanMethod() {
        return spanMethod.get();
    }

    /**
     * Sets the merge rule. Return null for an ordinary cell. The table hides cells
     * covered by earlier spans and clips spans at the edges of the table and fixed-column bands.
     */
    public final void setSpanMethod(Function<SpanContext<S>, CellSpan> method) {
        spanMethod.set(method);
    }

    public final ObjectProperty<Function<SpanContext<S>, CellSpan>> spanMethodProperty() {
        return spanMethod;
    }

    @Override
    public void refresh() {
        super.refresh();
        if (getSkin()instanceof EleFXTableSkin<?> skin) {
            skin.refreshSummary();
            skin.refreshSpans();
            skin.refreshAutoLayout();
        }
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    static void trackPointerFocus(Button button) {
        button.addEventFilter(MouseEvent.MOUSE_PRESSED,
                event -> button.pseudoClassStateChanged(POINTER_FOCUS, true));
        button.addEventFilter(KeyEvent.KEY_PRESSED,
                event -> button.pseudoClassStateChanged(POINTER_FOCUS, false));
        ChangeListener<Node> focusOwnerChanged = (observable, previous, current) -> {
            if (previous == button && current != button) {
                button.pseudoClassStateChanged(POINTER_FOCUS, false);
            }
        };
        button.sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null) {
                previous.focusOwnerProperty().removeListener(focusOwnerChanged);
                button.pseudoClassStateChanged(POINTER_FOCUS, false);
            }
            if (current != null) {
                current.focusOwnerProperty().addListener(focusOwnerChanged);
            }
        });
    }

    /** Whether collapsing a row retains its content node for the next expansion. */
    public final boolean isPreserveExpandedContent() {
        return preserveExpandedContent.get();
    }

    public final void setPreserveExpandedContent(boolean value) {
        preserveExpandedContent.set(value);
    }

    public final BooleanProperty preserveExpandedContentProperty() {
        return preserveExpandedContent;
    }

    void updateExpandColumn(EleFXTableExpandColumn<S> column) {
        if (expandColumn != column) {
            expandColumn = column;
            expandedRows.clear();
            expandedContent.clear();
        }
    }

    boolean isRowExpanded(S item) {
        return expandedRows.contains(item);
    }

    void toggleRowExpansion(EleFXTableExpandColumn<S> column, S item) {
        if (expandColumn != column || getFixedCellSize() > 0) {
            return;
        }
        if (!expandedRows.remove(item)) {
            expandedRows.add(item);
        } else if (!isPreserveExpandedContent()) {
            expandedContent.remove(item);
        }
        if (getSkin()instanceof EleFXTableSkin<?> skin) {
            skin.expansionChanged();
        }
    }

    Node expandedContent(S item) {
        if (expandColumn == null || !isRowExpanded(item)) {
            return null;
        }
        return expandedContent.computeIfAbsent(item, expandColumn::createContent);
    }

    private void retainExpansionItems() {
        if (expandedRows.isEmpty() && expandedContent.isEmpty()) {
            return;
        }
        var items = getItems();
        Set<S> currentItems = Collections.newSetFromMap(new IdentityHashMap<>());
        if (items != null) {
            currentItems.addAll(items);
        }
        expandedRows.retainAll(currentItems);
        expandedContent.keySet().retainAll(currentItems);
    }

    private void ignoreHeaderDragWithinClickThreshold(MouseEvent event) {
        if (!event.isPrimaryButtonDown() || !event.isStillSincePress()
                || !(event.getTarget()instanceof Node target)) {
            return;
        }
        for (Node node = target; node != null && node != this; node = node.getParent()) {
            if (node instanceof TableColumnHeader header) {
                // Resize handles belong to the nested header, not the column's label header.
                if (!(header instanceof NestedTableColumnHeader) && header.getTableColumn() != null) {
                    event.consume();
                }
                return;
            }
        }
    }

    public final boolean isStripe() {
        return stripe.get();
    }

    public final void setStripe(boolean stripe) {
        this.stripe.set(stripe);
    }

    public final BooleanProperty stripeProperty() {
        return stripe;
    }

    public final boolean isBordered() {
        return bordered.get();
    }

    public final void setBordered(boolean bordered) {
        this.bordered.set(bordered);
    }

    public final BooleanProperty borderedProperty() {
        return bordered;
    }

    final BooleanBinding effectiveBorderedProperty() {
        return effectiveBordered;
    }

    public final boolean isHighlightCurrentRow() {
        return highlightCurrentRow.get();
    }

    public final void setHighlightCurrentRow(boolean highlightCurrentRow) {
        this.highlightCurrentRow.set(highlightCurrentRow);
    }

    public final BooleanProperty highlightCurrentRowProperty() {
        return highlightCurrentRow;
    }

    /** Toggles a row's checkbox, allowing rows excluded by the selectable predicate. */
    public final void toggleRowSelection(S row) {
        toggleRowSelection(row, null);
    }

    /** Sets a row's checkbox state, or toggles it when selected is null. */
    public final void toggleRowSelection(S row, Boolean selected) {
        toggleRowSelection(row, selected, true);
    }

    /** Pass false for ignoreSelectable to enforce the selection column's predicate. */
    public final void toggleRowSelection(S row, Boolean selected, boolean ignoreSelectable) {
        EleFXTableSelectionColumn<S> column = findSelectionColumn(getColumns());
        if (column != null) {
            column.toggleRowSelection(row, selected, ignoreSelectable);
        }
    }

    /** Clears the checkbox selection. */
    public final void clearSelection() {
        EleFXTableSelectionColumn<S> column = findSelectionColumn(getColumns());
        if (column != null) {
            column.clearSelection();
        }
    }

    /** Returns the first selection column's read-only list of checked rows, including collapsed children. */
    public final ObservableList<S> getSelectionRows() {
        EleFXTableSelectionColumn<S> column = findSelectionColumn(getColumns());
        return column == null ? FXCollections.emptyObservableList() : column.getSelectedItems();
    }

    /**
     * Displays a filterable, sortable view of the source. Modify the source list
     * to add, remove, or replace rows.
     */
    public final void setFilterItems(ObservableList<S> source) {
        FilteredList<S> filtered = new FilteredList<>(Objects.requireNonNull(source, "source"));
        SortedList<S> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(comparatorProperty());
        setItems(sorted);
        filteredItems = filtered;
        sortedFilterItems = sorted;
        refreshColumnFilters();
    }

    /** Clears all active column filters. */
    public final void clearFilter() {
        for (var column : filterColumns()) {
            column.clearFilterValues();
        }
        refreshColumnFilters();
    }

    /** Clears filters for the given column IDs, as assigned with TableColumn.setId(). */
    public final void clearFilter(String... columnIds) {
        List<String> ids = List.of(columnIds);
        for (var column : filterColumns()) {
            if (column.getId() != null && ids.contains(column.getId())) {
                column.clearFilterValues();
            }
        }
        refreshColumnFilters();
    }

    final void refreshColumnFilters() {
        if (filteredItems != null) {
            var active = filterColumns().stream().filter(EleFXTableFilterColumn::hasFilterValues).toList();
            filteredItems.setPredicate(active.isEmpty()
                    ? null
                    : row -> active.stream().allMatch(column -> column.matches(row)));
        }
    }

    private List<EleFXTableFilterColumn<S, ?>> filterColumns() {
        var filters = new ArrayList<EleFXTableFilterColumn<S, ?>>();
        for (TableColumn<S, ?> column : getVisibleLeafColumns()) {
            if (column instanceof EleFXTableFilterColumn<?, ?>) {
                @SuppressWarnings("unchecked")
                var filter = (EleFXTableFilterColumn<S, ?>) column;
                filters.add(filter);
            }
        }
        return filters;
    }

    @SuppressWarnings("unchecked")
    private EleFXTableSelectionColumn<S> findSelectionColumn(List<TableColumn<S, ?>> columns) {
        for (TableColumn<S, ?> column : columns) {
            if (column instanceof EleFXTableSelectionColumn<?>) {
                return (EleFXTableSelectionColumn<S>) column;
            }
            EleFXTableSelectionColumn<S> nested = findSelectionColumn(column.getColumns());
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    public final EleFXTableSize getSize() {
        return size.get();
    }

    public final void setSize(EleFXTableSize size) {
        this.size.set(size);
    }

    public final ObjectProperty<EleFXTableSize> sizeProperty() {
        return size;
    }

    public final String getEmptyText() {
        return emptyText.get();
    }

    public final void setEmptyText(String emptyText) {
        this.emptyText.set(emptyText);
    }

    public final StringProperty emptyTextProperty() {
        return emptyText;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public String getUserAgentStylesheet() {
        return EleFXThemes.TABLE.toData();
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TABLE;
    }
}
