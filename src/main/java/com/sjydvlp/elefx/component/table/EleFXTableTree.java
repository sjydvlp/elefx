package com.sjydvlp.elefx.component.table;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Maintains the visible projection and branch state of an EleFXTable. */
final class EleFXTableTree<S> {

    record Row(int depth, boolean branch, boolean expanded, boolean loading) {
    }

    record Node<S> (S item, List<Node<S>> children) {
    }

    private static final class Branch<S> {

        private final S item;

        private Boolean expanded;

        private List<? extends S> loaded;

        private Object request;

        private Branch(S item) {
            this.item = item;
        }
    }

    private final EleFXTable<S> table;

    private final ObservableList<S> visible = FXCollections.observableArrayList();

    private final ObservableList<S> view = FXCollections.unmodifiableObservableList(visible);

    private final Map<Object, Branch<S>> branches = new HashMap<>();

    private final Map<S, Row> rows = new IdentityHashMap<>();

    private final SimpleIntegerProperty revision = new SimpleIntegerProperty();

    private final InvalidationListener sourceChanged = observable -> rebuild();

    private final WeakInvalidationListener weakSourceChanged = new WeakInvalidationListener(sourceChanged);

    private final Callback<TableView<S>, Boolean> sortPolicy = ignored -> {
        rebuild();
        return true;
    };

    private ObservableList<S> source;

    private List<Node<S>> roots = List.of();

    private Callback<TableView<S>, Boolean> previousSortPolicy;

    private Function<? super S, ?> rowKey = Function.identity();

    private Function<? super S, ? extends List<? extends S>> childrenGetter;

    private Predicate<? super S> hasChildrenGetter;

    private BiConsumer<? super S, Consumer<List<? extends S>>> loader;

    private boolean defaultExpandAll;

    private boolean active;

    EleFXTableTree(EleFXTable<S> table) {
        this.table = table;
        table.getVisibleLeafColumns().addListener((InvalidationListener) observable -> {
            if (active) {
                updateRevision();
            }
        });
    }

    boolean isActive() {
        return active;
    }

    ObservableList<S> getSource() {
        return source;
    }

    List<Node<S>> roots() {
        return roots;
    }

    boolean projects(ObservableList<S> roots, ObservableList<S> items) {
        return source == roots && items == view;
    }

    void setRowKey(Function<? super S, ?> getter) {
        rowKey = getter == null ? Function.identity() : getter;
        branches.clear();
        rebuild();
    }

    void setChildrenGetter(Function<? super S, ? extends List<? extends S>> getter) {
        childrenGetter = getter;
        configure();
    }

    void setHasChildrenGetter(Predicate<? super S> getter) {
        hasChildrenGetter = getter;
        rebuild();
    }

    void setDefaultExpandAll(boolean expanded) {
        defaultExpandAll = expanded;
        rebuild();
    }

    void setLoader(BiConsumer<? super S, Consumer<List<? extends S>>> value) {
        loader = value;
        for (var branch : branches.values()) {
            if (branch.request != null || branch.loaded != null) {
                branch.expanded = null;
            }
            branch.request = null;
            branch.loaded = null;
        }
        configure();
    }

    private void configure() {
        boolean enabled = childrenGetter != null || loader != null;
        if (enabled && !active) {
            active = true;
            previousSortPolicy = table.getSortPolicy();
            replaceSource(table.getItems());
            table.setSortPolicy(sortPolicy);
            table.setItems(view);
        } else if (!enabled && active) {
            active = false;
            var roots = source;
            replaceSource(null);
            table.setItems(roots);
            if (table.getSortPolicy() == sortPolicy) {
                table.setSortPolicy(previousSortPolicy);
            }
            rows.clear();
            this.roots = List.of();
            visible.clear();
            updateRevision();
        }
        rebuild();
    }

    void itemsChanged(ObservableList<S> items) {
        if (active && items != view) {
            replaceSource(items);
            rebuild();
            table.setItems(view);
        }
    }

    private void replaceSource(ObservableList<S> items) {
        if (source != null) {
            source.removeListener(weakSourceChanged);
        }
        source = items;
        branches.clear();
        if (source != null) {
            source.addListener(weakSourceChanged);
        }
    }

    void addInvalidationListener(InvalidationListener listener) {
        revision.addListener(listener);
    }

    void removeInvalidationListener(InvalidationListener listener) {
        revision.removeListener(listener);
    }

    private void updateRevision() {
        revision.set(revision.get() + 1);
    }

    Row row(S item, TableColumn<S, ?> column) {
        if (active) {
            for (var leaf : table.getVisibleLeafColumns()) {
                if (leaf instanceof EleFXTableExpandColumn<?> || leaf instanceof EleFXTableSelectionColumn<?>
                        || leaf instanceof EleFXTableIndexColumn<?>) {
                    continue;
                }
                return leaf == column ? rows.get(item) : null;
            }
        }
        return null;
    }

    void toggle(S item) {
        Row row = rows.get(item);
        if (row == null || !row.branch()) {
            return;
        }
        Object key = rowKey.apply(item);
        Branch<S> branch = branches.get(key);
        branch.expanded = !row.expanded();
        if (branch.expanded && children(item, branch).isEmpty() && branch.request == null) {
            load(key, branch);
        } else {
            rebuild();
        }
    }

    private void load(Object key, Branch<S> branch) {
        Object request = new Object();
        branch.request = request;
        rebuild();
        try {
            loader.accept(branch.item, result -> {
                List<? extends S> loaded = result == null ? null : List.copyOf(result);
                Runnable complete = () -> complete(key, branch, request, loaded);
                if (Platform.isFxApplicationThread()) {
                    complete.run();
                } else {
                    Platform.runLater(complete);
                }
            });
        } catch (RuntimeException exception) {
            complete(key, branch, request, null);
            throw exception;
        }
    }

    private void complete(Object key, Branch<S> branch, Object request, List<? extends S> result) {
        if (branches.get(key) == branch && branch.request == request) {
            branch.request = null;
            branch.loaded = result;
            if (result == null) {
                branch.expanded = false;
            }
            rebuild();
        }
    }

    private List<? extends S> children(S item, Branch<S> branch) {
        if (branch.loaded != null) {
            return branch.loaded;
        }
        var children = childrenGetter == null ? null : childrenGetter.apply(item);
        return children == null ? List.of() : children;
    }

    private void rebuild() {
        if (!active) {
            return;
        }
        rows.clear();
        var keys = new HashSet<>();
        var next = new ArrayList<S>();
        roots = source == null ? List.of() : append(source, 0, true, next, keys);
        branches.keySet().retainAll(keys);
        replaceVisibleRows(next);
        updateRevision();
    }

    private void replaceVisibleRows(List<S> next) {
        int start = 0;
        int oldEnd = visible.size();
        int newEnd = next.size();
        while (start < oldEnd && start < newEnd && visible.get(start) == next.get(start)) {
            start++;
        }
        while (oldEnd > start && newEnd > start && visible.get(oldEnd - 1) == next.get(newEnd - 1)) {
            oldEnd--;
            newEnd--;
        }
        if (oldEnd == start) {
            if (newEnd > start) {
                visible.addAll(start, next.subList(start, newEnd));
            }
        } else if (newEnd == start) {
            visible.remove(start, oldEnd);
        } else {
            visible.setAll(next);
        }
    }

    private List<Node<S>> append(List<? extends S> siblings, int depth, boolean show, List<S> next, Set<Object> keys) {
        var ordered = new ArrayList<S>();
        for (S item : siblings) {
            if (item != null) {
                ordered.add(item);
            }
        }
        if (table.getComparator() != null) {
            ordered.sort(table.getComparator());
        }
        var nodes = new ArrayList<Node<S>>();
        for (S item : ordered) {
            Object key = rowKey.apply(item);
            if (!keys.add(key)) {
                continue;
            }
            Branch<S> branch = branches.compute(key,
                    (ignored, previous) -> previous != null && previous.item == item ? previous : new Branch<>(item));
            var children = children(item, branch);
            boolean known = !children.isEmpty();
            boolean loadable = branch.loaded == null && loader != null
                    && hasChildrenGetter != null && hasChildrenGetter.test(item);
            boolean expanded = branch.expanded == null ? defaultExpandAll && known : branch.expanded;
            if (show) {
                rows.put(item, new Row(depth, known || loadable, expanded, branch.request != null));
                next.add(item);
            }
            nodes.add(new Node<>(item, known
                    ? append(children, depth + 1, show && expanded, next, keys)
                    : List.of()));
        }
        return nodes;
    }
}
