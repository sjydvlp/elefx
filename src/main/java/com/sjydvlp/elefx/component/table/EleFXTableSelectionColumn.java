package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/** A checkbox column with a select-all header and optional tree selection. */
public class EleFXTableSelectionColumn<S> extends EleFXTableColumn<S, S> {

    private record CheckState(BooleanProperty selected, BooleanProperty partial) {

        private CheckState() {
            this(new SimpleBooleanProperty(), new SimpleBooleanProperty());
        }
    }

    private enum Coverage {

        NONE,
        UNCHECKED,
        PARTIAL,
        CHECKED;

        Coverage merge(Coverage other) {
            if (this == NONE) {
                return other;
            }
            return other == NONE || this == other ? this : PARTIAL;
        }
    }

    private final Function<? super S, ?> rowKey;

    private final ObjectProperty<Predicate<? super S>> selectable = new SimpleObjectProperty<>(row -> true);

    private final BooleanProperty checkStrictly = new SimpleBooleanProperty(this, "checkStrictly", true);

    private final Map<Object, CheckState> checked = new HashMap<>();

    private final Map<Object, EleFXTableTree.Node<S>> treeNodes = new HashMap<>();

    private final List<S> rows = new ArrayList<>();

    private final ObservableList<S> selectedItems = FXCollections.observableArrayList();

    private final ObservableList<S> readOnlySelectedItems = FXCollections.unmodifiableObservableList(selectedItems);

    private final CheckBox selectAll = createCheckBox();

    private final ListChangeListener<S> rowsChanged = change -> synchronizeFlatRows();

    private final WeakListChangeListener<S> weakRowsChanged = new WeakListChangeListener<>(rowsChanged);

    private final ChangeListener<ObservableList<S>> itemsChanged = (observable, oldItems,
                                                                    newItems) -> observeRows(oldItems, newItems);

    private final InvalidationListener treeChanged = observable -> synchronizeRows();

    private final WeakInvalidationListener weakTreeChanged = new WeakInvalidationListener(treeChanged);

    public EleFXTableSelectionColumn() {
        this(Function.identity());
    }

    /** Uses a stable, unique key to retain selection when a row is replaced. */
    public EleFXTableSelectionColumn(Function<? super S, ?> rowKey) {
        this.rowKey = Objects.requireNonNull(rowKey, "rowKey");
        setMinWidth(55);
        setMaxWidth(55);
        setValueGetter(Function.identity());
        setGraphic(selectAll);
        selectAll.setAccessibleText("Select all selectable rows");
        selectAll.setDisable(true);
        selectAll.setOnAction(event -> setAllSelectable());

        setCellGraphicFactory(this::createRowCheckBox);
        var graphicFactory = getCellFactory();
        setCellFactory(column -> {
            var cell = graphicFactory.call(column);
            cell.getStyleClass().add("check-box-table-cell");
            return cell;
        });

        selectable.addListener(observable -> updateSelection());
        checkStrictly.addListener(observable -> updateSelection());
        tableViewProperty().addListener((observable, oldTable, newTable) -> {
            if (oldTable != null) {
                oldTable.itemsProperty().removeListener(itemsChanged);
            }
            if (newTable != null) {
                newTable.itemsProperty().addListener(itemsChanged);
            }
            if (oldTable instanceof EleFXTable<S> table) {
                table.tree().removeInvalidationListener(weakTreeChanged);
            }
            if (newTable instanceof EleFXTable<S> table) {
                table.tree().addInvalidationListener(weakTreeChanged);
            }
            observeRows(oldTable == null ? null : oldTable.getItems(),
                    newTable == null ? null : newTable.getItems());
        });
    }

    public final void setSelectable(Predicate<? super S> predicate) {
        selectable.set(Objects.requireNonNull(predicate, "selectable"));
    }

    /** Rows are independent by default. Set false to link selection between tree parents and children. */
    public final boolean isCheckStrictly() {
        return checkStrictly.get();
    }

    public final void setCheckStrictly(boolean value) {
        checkStrictly.set(value);
    }

    public final BooleanProperty checkStrictlyProperty() {
        return checkStrictly;
    }

    /** An observable, read-only list of checked rows in table order. */
    final ObservableList<S> getSelectedItems() {
        return readOnlySelectedItems;
    }

    /** A null selected value toggles the row. Pass false for ignoreSelectable to enforce the predicate. */
    final void toggleRowSelection(S row, Boolean selected, boolean ignoreSelectable) {
        if (row == null) {
            return;
        }
        Object key = rowKey.apply(row);
        var state = checked.get(key);
        if (state == null || (!ignoreSelectable && !selectable.get().test(row))) {
            return;
        }
        boolean value = selected == null ? !state.selected().get() : selected;
        var node = treeNodes.get(key);
        if (!isCheckStrictly() && node != null) {
            selectBranch(node, value, ignoreSelectable);
        } else {
            state.selected().set(value);
        }
        updateSelection();
    }

    final void clearSelection() {
        checked.values().forEach(state -> state.selected().set(false));
        updateSelection();
    }

    private CheckBox createRowCheckBox(S row) {
        CheckBox checkBox = createCheckBox();
        Object key = rowKey.apply(row);
        var state = checked.computeIfAbsent(key, ignored -> new CheckState());
        checkBox.selectedProperty().bindBidirectional(state.selected());
        checkBox.indeterminateProperty().bindBidirectional(state.partial());
        checkBox.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !selectable.get().test(row), selectable));
        checkBox.setAccessibleText("Select row " + key);
        checkBox.setOnAction(event -> toggleRowSelection(row, checkBox.isSelected(), false));
        return checkBox;
    }

    private static CheckBox createCheckBox() {
        var checkBox = new CheckBox();
        EventHandler<MouseEvent> releaseFocus = event -> {
            if (event.getButton() != MouseButton.PRIMARY || !checkBox.isFocused()) {
                return;
            }
            for (Node node = event.getTarget()instanceof Node target ? target : null; node != null; node = node
                    .getParent()) {
                if (node == checkBox) {
                    return;
                }
            }
            // Layout panes do not take focus on click. Clear the old owner before
            // the clicked node handles the press and can request focus itself.
            checkBox.getScene().getRoot().requestFocus();
        };
        checkBox.focusedProperty().addListener((observable, previous, focused) -> {
            var scene = checkBox.getScene();
            if (scene != null) {
                if (focused) {
                    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, releaseFocus);
                } else {
                    scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, releaseFocus);
                }
            }
        });
        checkBox.sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null) {
                previous.removeEventFilter(MouseEvent.MOUSE_PRESSED, releaseFocus);
            }
            if (current != null && checkBox.isFocused()) {
                current.addEventFilter(MouseEvent.MOUSE_PRESSED, releaseFocus);
            }
        });
        return checkBox;
    }

    private void observeRows(ObservableList<S> oldItems, ObservableList<S> newItems) {
        if (oldItems != null) {
            oldItems.removeListener(weakRowsChanged);
        }
        if (newItems != null) {
            newItems.addListener(weakRowsChanged);
        }
        synchronizeFlatRows();
        if (activeTree() != null) {
            synchronizeRows();
        }
    }

    private EleFXTableTree<S> activeTree() {
        return getTableView()instanceof EleFXTable<S> table && table.tree().isActive() ? table.tree() : null;
    }

    private void synchronizeFlatRows() {
        if (activeTree() == null) {
            synchronizeRows();
        }
    }

    private void synchronizeRows() {
        rows.clear();
        treeNodes.clear();
        Set<Object> keys = new HashSet<>();
        var tree = activeTree();
        if (tree != null) {
            for (var root : tree.roots()) {
                collectBranch(root, false, keys);
            }
        } else {
            TableView<S> table = getTableView();
            if (table != null && table.getItems() != null) {
                for (S row : table.getItems()) {
                    if (row != null) {
                        rows.add(row);
                        Object key = rowKey.apply(row);
                        keys.add(key);
                        checked.computeIfAbsent(key, ignored -> new CheckState());
                    }
                }
            }
        }
        checked.keySet().retainAll(keys);
        updateSelection();
    }

    private void collectBranch(EleFXTableTree.Node<S> node, boolean inheritSelection, Set<Object> keys) {
        S row = node.item();
        Object key = rowKey.apply(row);
        keys.add(key);
        rows.add(row);
        treeNodes.put(key, node);
        var state = checked.computeIfAbsent(key, ignored -> {
            var created = new CheckState();
            created.selected().set(inheritSelection && selectable.get().test(row));
            return created;
        });
        // Newly loaded eligible children inherit a checked parent's selection.
        boolean inherit = !isCheckStrictly() && (inheritSelection || state.selected().get());
        for (var child : node.children()) {
            collectBranch(child, inherit, keys);
        }
    }

    private void selectBranch(EleFXTableTree.Node<S> node, boolean selected, boolean ignoreSelectable) {
        S row = node.item();
        if (ignoreSelectable || selectable.get().test(row)) {
            checked.get(rowKey.apply(row)).selected().set(selected);
        }
        for (var child : node.children()) {
            selectBranch(child, selected, ignoreSelectable);
        }
    }

    private Coverage updateBranch(EleFXTableTree.Node<S> node) {
        Coverage descendants = Coverage.NONE;
        for (var child : node.children()) {
            descendants = descendants.merge(updateBranch(child));
        }
        S row = node.item();
        var state = checked.get(rowKey.apply(row));
        if (!selectable.get().test(row)) {
            state.partial().set(false);
            return descendants;
        }
        state.partial().set(descendants == Coverage.PARTIAL);
        if (descendants == Coverage.NONE) {
            return state.selected().get() ? Coverage.CHECKED : Coverage.UNCHECKED;
        }
        state.selected().set(descendants == Coverage.CHECKED);
        return descendants;
    }

    private void setAllSelectable() {
        var eligible = rows.stream()
                .filter(selectable.get())
                .map(row -> checked.get(rowKey.apply(row)))
                .toList();
        boolean select = eligible.stream().anyMatch(state -> !state.selected().get());
        eligible.forEach(state -> state.selected().set(select));
        updateSelection();
    }

    private void updateSelection() {
        var tree = activeTree();
        if (tree != null && !isCheckStrictly()) {
            tree.roots().forEach(this::updateBranch);
        } else {
            checked.values().forEach(state -> state.partial().set(false));
        }
        var selection = new ArrayList<S>();
        int eligibleCount = 0;
        int checkedEligibleCount = 0;
        for (S row : rows) {
            boolean isChecked = checked.get(rowKey.apply(row)).selected().get();
            if (isChecked) {
                selection.add(row);
            }
            if (selectable.get().test(row)) {
                eligibleCount++;
                if (isChecked) {
                    checkedEligibleCount++;
                }
            }
        }
        selectAll.setDisable(eligibleCount == 0);
        boolean allSelected = eligibleCount > 0 && checkedEligibleCount == eligibleCount;
        selectAll.setSelected(allSelected);
        selectAll.setIndeterminate(!selection.isEmpty() && !allSelected);
        if (!sameSelection(selection)) {
            selectedItems.setAll(selection);
        }
    }

    private boolean sameSelection(List<S> selection) {
        if (selectedItems.size() != selection.size()) {
            return false;
        }
        for (int i = 0; i < selection.size(); i++) {
            if (selectedItems.get(i) != selection.get(i)) {
                return false;
            }
        }
        return true;
    }
}
