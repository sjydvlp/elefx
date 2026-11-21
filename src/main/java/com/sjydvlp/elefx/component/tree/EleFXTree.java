package com.sjydvlp.elefx.component.tree;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

/**
 * A virtualized tree with a hidden forest root. TreeItem instances are stable node identities.
 * Modify getItems() and TreeItem.getChildren() on the JavaFX application thread.
 */
public class EleFXTree<T> extends StackPane implements Themable {

    public enum DropPosition {
        BEFORE,
        INSIDE,
        AFTER
    }

    public record Drop<T> (TreeItem<T> source, TreeItem<T> target, DropPosition position) {
    }

    public enum DragPhase {
        START,
        ENTER,
        LEAVE,
        OVER,
        DROP,
        END
    }

    /** The target is absent at START and at an unsuccessful END. Position is the accepted drop location, if any. */
    public record NodeDrag<T> (DragPhase phase, TreeItem<T> source, TreeItem<T> target, DropPosition position) {
    }

    private final TreeItem<T> root = new TreeItem<>();

    private final TreeView<T> view = new TreeView<>(root) {

        @Override
        protected Skin<?> createDefaultSkin() {
            return new RevealSkin<>(this, animationRevision, () -> contentHeight(-1));
        }
    };

    private static final class RevealSkin<T> extends javafx.scene.control.skin.TreeViewSkin<T> {

        RevealSkin(TreeView<T> view, ReadOnlyIntegerProperty revision, DoubleSupplier contentHeight) {
            super(view);
            var flow = (RevealFlow<T>) getVirtualFlow();
            flow.contentHeight = contentHeight;
            registerChangeListener(revision, ignored -> flow.remeasureRows());
        }

        @Override
        protected javafx.scene.control.skin.VirtualFlow<TreeCell<T>> createVirtualFlow() {
            return new RevealFlow<>();
        }
    }

    private static final class RevealFlow<T> extends javafx.scene.control.skin.VirtualFlow<TreeCell<T>> {

        private DoubleSupplier contentHeight = () -> Double.POSITIVE_INFINITY;

        void remeasureRows() {
            reconfigureCells();
        }

        @Override
        protected void layoutChildren() {
            boolean contentFits = contentHeight.getAsDouble() <= getHeight() + 1e-6;
            if (contentFits && !getHbar().isVisible()) setPosition(0);
            super.layoutChildren();
            // VirtualFlow can retain a scrollbar at the exact fit boundary after remeasuring rows.
            if (contentFits && !getHbar().isVisible()) getVbar().setVisible(false);
        }
    }

    final ReadOnlyIntegerWrapper animationRevision = new ReadOnlyIntegerWrapper();

    private final Map<TreeItem<T>, BranchReveal> reveals = new IdentityHashMap<>();

    private final class BranchReveal {

        final Map<TreeItem<T>, Integer> rows = new IdentityHashMap<>();

        final DoubleProperty progress = new SimpleDoubleProperty();

        final javafx.animation.Timeline timeline = new javafx.animation.Timeline();

        boolean closing;

        void indexChildren(TreeItem<T> parent) {
            for (var child : parent.getChildren()) {
                rows.put(child, rows.size());
                if (child.isExpanded()) indexChildren(child);
            }
        }
    }

    static final double ROW_HEIGHT = 26;

    private final Label empty = new Label("No Data");

    private final Set<TreeItem<T>> checked = new LinkedHashSet<>();

    private final Set<TreeItem<T>> partial = new LinkedHashSet<>();

    private final Map<TreeItem<T>, TreeItem<T>> originals = new IdentityHashMap<>();

    private final ReadOnlyObjectWrapper<TreeItem<T>> current = new ReadOnlyObjectWrapper<>(this, "currentItem");

    private final BooleanProperty showCheckbox = new SimpleBooleanProperty(this, "showCheckbox");

    private final BooleanProperty checkStrictly = new SimpleBooleanProperty(this, "checkStrictly");

    private final BooleanProperty accordion = new SimpleBooleanProperty(this, "accordion");

    private final BooleanProperty expandOnClickNode = new SimpleBooleanProperty(this, "expandOnClickNode", true);

    private final BooleanProperty checkOnClickNode = new SimpleBooleanProperty(this, "checkOnClickNode");

    private final BooleanProperty checkOnClickLeaf = new SimpleBooleanProperty(this, "checkOnClickLeaf", true);

    private final BooleanProperty draggable = new SimpleBooleanProperty(this, "draggable");

    private final BooleanProperty highlightCurrent = new SimpleBooleanProperty(this, "highlightCurrent");

    private final EventHandler<MouseEvent> clearFocusOnOutsidePress = event -> {
        Node target = event.getTarget()instanceof Node node ? node : null;
        for (Node node = target; node != null; node = node.getParent()) {
            if (node == this) return;
        }
        clearRowFocus();
    };

    private Props<T> props = new Props<>(Objects::toString, value -> false);

    private Function<? super T, ?> nodeKey = Function.identity();

    private Function<TreeItem<T>, Node> renderer;

    private Predicate<TreeItem<T>> disabled = item -> false;

    private Predicate<? super T> filter;

    private Predicate<TreeItem<T>> allowDrag = item -> true;

    private Predicate<Drop<T>> allowDrop = drop -> true;

    private Consumer<TreeItem<T>> onCheckChange;

    private Consumer<TreeItem<T>> onNodeClick;

    private Consumer<Drop<T>> onDrop;

    private Consumer<NodeDrag<T>> onNodeDrag;

    private Consumer<TreeItem<T>> onNodeDragStart;

    private BiConsumer<TreeItem<T>, TreeItem<T>> onNodeDragEnter;

    private BiConsumer<TreeItem<T>, TreeItem<T>> onNodeDragLeave;

    private BiConsumer<TreeItem<T>, TreeItem<T>> onNodeDragOver;

    private Consumer<Drop<T>> onNodeDragEnd;

    private boolean defaultExpandAll;

    private TreeItem<T> dragged;

    private boolean rebuilding;

    public EleFXTree() {
        getStyleClass().add("ele-tree");
        setMaxHeight(USE_PREF_SIZE);
        maxHeightProperty().addListener(observable -> requestLayout());
        empty.getStyleClass().add("ele-tree__empty");
        empty.setMouseTransparent(true);
        view.setShowRoot(false);
        view.setCellFactory(v -> new EleFXTreeCell<>(this));
        view.expandedItemCountProperty().addListener(observable -> requestLayout());
        animationRevision.addListener(observable -> requestLayout());
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, clearFocusOnOutsidePress);
            if (newScene != null) newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, clearFocusOnOutsidePress);
        });
        view.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            var item = getCurrentItem();
            var focused = view.getFocusModel().getFocusedItem();
            boolean rtl = view.getEffectiveNodeOrientation() == javafx.geometry.NodeOrientation.RIGHT_TO_LEFT;
            KeyCode collapseKey = rtl ? KeyCode.RIGHT : KeyCode.LEFT;
            if (event.getTarget() == view && focused != null && focused.isExpanded()
                    && (event.getCode() == collapseKey || event.getCode() == KeyCode.SUBTRACT)) {
                if (!isClosing(focused)) toggleBranch(focused);
                event.consume();
                return;
            }
            if (event.getTarget() == view && event.getCode() == KeyCode.SPACE
                    && isShowCheckbox() && item != null) {
                setChecked(item, !isChecked(item));
                event.consume();
            }
        });
        root.setExpanded(true);
        EventHandler<TreeItem.TreeModificationEvent<T>> expand = e -> animateExpansion(e.getTreeItem());
        EventHandler<TreeItem.TreeModificationEvent<T>> collapse = e -> {
            finishReveal(e.getTreeItem());
        };
        view.rootProperty().addListener((o, previous, next) -> {
            for (var branch : List.copyOf(reveals.keySet()))
                finishReveal(branch);
            previous.removeEventHandler(TreeItem.branchExpandedEvent(), expand);
            previous.removeEventHandler(TreeItem.branchCollapsedEvent(), collapse);
            next.addEventHandler(TreeItem.branchExpandedEvent(), expand);
            next.addEventHandler(TreeItem.branchCollapsedEvent(), collapse);
        });
        root.addEventHandler(TreeItem.branchExpandedEvent(), expand);
        root.addEventHandler(TreeItem.branchCollapsedEvent(), collapse);
        getChildren().addAll(view, empty);
        root.addEventHandler(TreeItem.<T>childrenModificationEvent(), this::childrenChanged);
        root.addEventHandler(TreeItem.<T>valueChangedEvent(), e -> {
            if (filter != null) rebuild();
        });
        view.getSelectionModel().selectedItemProperty().addListener((o, old, item) -> {
            if (!rebuilding) current.set(original(item));
        });
        showCheckbox.addListener(o -> view.refresh());
        checkStrictly.addListener(o -> {
            partial.clear();
            if (!isCheckStrictly()) recompute(root);
            view.refresh();
        });
        highlightCurrent.addListener(
                o -> pseudoClassStateChanged(PseudoClass.getPseudoClass("highlight-current"), isHighlightCurrent()));
        sceneBuilderIntegration();
    }

    public ObservableList<TreeItem<T>> getItems() {
        return root.getChildren();
    }

    @Override
    protected double computeMinHeight(double width) {
        return Math.min(prefHeight(width), maxHeight(width));
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + contentHeight(width) + snappedBottomInset();
    }

    private double contentHeight(double width) {
        int rows = view.getExpandedItemCount();
        double rowHeight = rowHeight();
        double hiddenRows = 0;
        for (var reveal : reveals.values())
            hiddenRows += reveal.rows.size() * (1 - reveal.progress.get());
        return rows == 0
                ? Math.max(rowHeight, empty.prefHeight(width))
                : snapSizeY(rowHeight * (rows - hiddenRows));
    }

    double rowHeight() {
        return snapSizeY(ROW_HEIGHT);
    }

    /**
     * Replaces the tree with ordinary nodes built from the supplied data and the current
     * children mapping. Call on the JavaFX application thread.
     */
    public void setData(Collection<? extends T> data) {
        var items = data.stream().map(this::createDataItem).toList();
        loadGeneration++;
        getItems().setAll(items);
    }

    private TreeItem<T> createDataItem(T value) {
        var item = new TreeItem<>(value);
        var children = props.children().apply(value);
        if (children != null && !children.isEmpty()) {
            item.getChildren().setAll(children.stream().map(this::createDataItem).toList());
            item.setExpanded(defaultExpandAll);
        }
        return item;
    }

    /** The hidden root has level 0 and null data. Visible roots start at level 1. */
    public record LoadNode<T> (T data, int level) {
    }

    /**
     * Supplies children using resolve, or reports a retryable load failure using reject.
     * Only the first callback invocation is used.
     */
    @FunctionalInterface
    public interface LoadFunction<T> {

        void load(LoadNode<T> node, Consumer<Collection<? extends T>> resolve, Runnable reject);
    }

    private boolean lazy;

    private LoadFunction<T> load;

    /** Maps a node to its label, its children for setData, and its known leaf status in lazy mode. */
    public record Props<T> (Function<? super T, String> label,
            Function<? super T, ? extends Collection<? extends T>> children,
            Predicate<? super T> isLeaf) {

        public Props {
            Objects.requireNonNull(label);
            Objects.requireNonNull(children);
            Objects.requireNonNull(isLeaf);
        }

        public Props(Function<? super T, String> label, Predicate<? super T> isLeaf) {
            this(label, value -> List.of(), isLeaf);
        }
    }

    private long loadGeneration;

    public boolean isLazy() {
        return lazy;
    }

    /**
     * Enables loading from the hidden root when a loader is configured.
     * Disabling lazy loading clears the generated nodes and invalidates pending requests.
     * Configure lazy loading on the JavaFX application thread.
     */
    public void setLazy(boolean lazy) {
        if (this.lazy == lazy) return;
        this.lazy = lazy;
        restartLazyLoad();
    }

    /** Sets the loader. Setting it again starts a new root request when lazy loading is enabled. */
    public void setLoad(LoadFunction<T> load) {
        this.load = Objects.requireNonNull(load);
        if (lazy) restartLazyLoad();
    }

    public Props<T> getProps() {
        return props;
    }

    /** Sets the label for all nodes and the mappings used when building new nodes. */
    public void setProps(Props<T> props) {
        this.props = Objects.requireNonNull(props);
        view.refresh();
    }

    /**
     * Updates the supplied mappings by property name on records, JavaBeans, or maps.
     * Supported keys are label, children, and isLeaf. Omitted keys retain their current mappings.
     */
    public void setProps(Map<String, String> mappings) {
        if (mappings.isEmpty()) return;

        var label = props.label();
        var children = props.children();
        var isLeaf = props.isLeaf();
        for (var entry : mappings.entrySet()) {
            var reader = new PropertyReader(Objects.requireNonNull(entry.getValue()));
            switch (entry.getKey()) {
                case "label" -> label = value -> Objects.toString(reader.read(value), "");
                case "children" -> children = value -> readChildren(reader, value);
                case "isLeaf" -> isLeaf = value -> Boolean.TRUE.equals(reader.read(value));
                default -> throw new IllegalArgumentException("Unknown tree prop '" + entry.getKey() + "'");
            }
        }
        setProps(new Props<>(label, children, isLeaf));
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends T> readChildren(PropertyReader reader, T value) {
        Object children = reader.read(value);
        if (children == null) return List.of();
        if (children instanceof Collection<?> items) return (Collection<? extends T>) items;
        throw new IllegalArgumentException("Tree property '" + reader.name + "' must be a collection");
    }

    private static final class PropertyReader extends ClassValue<Method> {

        private final String name;

        private PropertyReader(String name) {
            if (name.isBlank()) throw new IllegalArgumentException("Tree property name must not be blank");
            this.name = name;
        }

        @Override
        protected Method computeValue(Class<?> type) {
            if (type.isRecord()) {
                for (var component : type.getRecordComponents()) {
                    if (name.equals(component.getName())) {
                        Method method = component.getAccessor();
                        method.trySetAccessible();
                        return method;
                    }
                }
            }
            String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            for (String accessor : List.of("get" + suffix, "is" + suffix)) {
                try {
                    Method method = type.getMethod(accessor);
                    if (method.getReturnType() == void.class
                            || Modifier.isStatic(method.getModifiers()))
                        continue;
                    method.trySetAccessible();
                    return method;
                } catch (NoSuchMethodException ignored) {
                    // Try the next accessor convention.
                }
            }
            throw new IllegalArgumentException("No tree property '" + name + "' on " + type.getName());
        }

        private Object read(Object value) {
            if (value == null) return null;
            if (value instanceof Map<?, ?> map) return map.get(name);
            try {
                return get(value.getClass()).invoke(value);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalArgumentException("Cannot read tree property '" + name + "'", failure);
            }
        }
    }

    private void restartLazyLoad() {
        loadGeneration++;
        if (load == null) return;
        getItems().clear();
        if (lazy) requestChildren(new LoadNode<>(null, 0), getItems()::setAll, () -> {
        });
    }

    private void requestChildren(LoadNode<T> node, Consumer<List<TreeItem<T>>> accept, Runnable reject) {
        long generation = loadGeneration;
        var result = new CompletableFuture<List<T>>();
        result.whenComplete((children, failure) -> Platform.runLater(() -> {
            if (generation != loadGeneration) return;
            if (failure != null) {
                reject.run();
                return;
            }
            try {
                var items = children.stream().map(child -> createLazyItem(child, node.level() + 1)).toList();
                accept.accept(items);
            } catch (RuntimeException invalidChildren) {
                reject.run();
            }
        }));
        try {
            load.load(node, children -> {
                try {
                    result.complete(List.copyOf(children));
                } catch (RuntimeException invalidChildren) {
                    result.completeExceptionally(invalidChildren);
                }
            }, () -> result.cancel(false));
        } catch (RuntimeException failure) {
            result.completeExceptionally(failure);
        }
    }

    private TreeItem<T> createLazyItem(T value, int level) {
        return props.isLeaf().test(value) ? new TreeItem<>(value) : new LazyItem(value, level);
    }

    ReadOnlyBooleanProperty loadingProperty(TreeItem<?> item) {
        return item instanceof EleFXTree<?>.LazyItem lazyItem ? lazyItem.loading.getReadOnlyProperty() : null;
    }

    private final class LazyItem extends TreeItem<T> {

        private final ReadOnlyBooleanWrapper loading = new ReadOnlyBooleanWrapper();

        private boolean loaded;

        private LazyItem(T value, int level) {
            super(value);
            expandedProperty().addListener((observable, oldValue, expanded) -> {
                if (!expanded || !lazy || loaded || loading.get() || !belongs(this)) return;
                loading.set(true);
                requestChildren(new LoadNode<>(value, level), children -> {
                    getChildren().setAll(children);
                    if (children.isEmpty()) setExpanded(false);
                    loaded = true;
                    loading.set(false);
                    if (isExpanded()) animateExpansion(this);
                }, () -> {
                    setExpanded(false);
                    loading.set(false);
                });
            });
        }

        @Override
        public boolean isLeaf() {
            return loaded && super.isLeaf();
        }
    }

    private void animateExpansion(TreeItem<T> branch) {
        var parent = branch.getParent();
        boolean visible = getScene() != null && getScene().getWindow() != null
                && getScene().getWindow().isShowing() && parent != null && view.getRow(branch) >= 0;
        if (parent != null && isAccordion()) {
            for (var sibling : parent.getChildren()) {
                if (sibling == branch || !sibling.isExpanded() || isClosing(sibling)) continue;
                if (visible && !sibling.getChildren().isEmpty())
                    playReveal(sibling, true);
                else
                    sibling.setExpanded(false);
            }
        }
        if (visible && !branch.getChildren().isEmpty()) playReveal(branch, false);
    }

    private boolean isClosing(TreeItem<T> branch) {
        var reveal = reveals.get(branch);
        return reveal != null && reveal.closing;
    }

    double caretProgress(TreeItem<T> item) {
        var reveal = reveals.get(item);
        return reveal == null ? 1 : reveal.progress.get();
    }

    double revealedHeight(TreeItem<T> item) {
        double height = rowHeight();
        for (var reveal : reveals.values()) {
            Integer index = reveal.rows.get(item);
            if (index != null) {
                double edge = reveal.progress.get() * reveal.rows.size() * height;
                return Math.max(0, Math.min(height, edge - index * height));
            }
        }
        return height;
    }

    void toggleBranch(TreeItem<T> branch) {
        if (!branch.isExpanded())
            branch.setExpanded(true);
        else if (branch.getChildren().isEmpty())
            branch.setExpanded(false);
        else if (isClosing(branch))
            animateExpansion(branch);
        else
            playReveal(branch, true);
    }

    private void playReveal(TreeItem<T> branch, boolean close) {
        // Siblings can move together. Finish other levels before measuring a nested subtree.
        for (var active : List.copyOf(reveals.keySet())) {
            if (active.getParent() != branch.getParent()) finishReveal(active);
        }
        var reveal = reveals.computeIfAbsent(branch, item -> {
            var state = new BranchReveal();
            state.indexChildren(item);
            state.progress.set(close ? 1 : 0);
            state.progress.addListener((obs, old, value) -> animationRevision.set(animationRevision.get() + 1));
            return state;
        });
        double from = reveal.progress.get();
        reveal.timeline.stop();
        reveal.closing = close;
        double to = close ? 0 : 1;
        reveal.progress.set(from);
        reveal.timeline.getKeyFrames().setAll(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                        new javafx.animation.KeyValue(reveal.progress, from)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(Math.max(1, 300 * Math.abs(to - from))),
                        new javafx.animation.KeyValue(reveal.progress, to,
                                javafx.animation.Interpolator.SPLINE(0.42, 0, 0.58, 1))));
        animationRevision.set(animationRevision.get() + 1);
        reveal.timeline.setOnFinished(event -> finishReveal(branch));
        reveal.timeline.playFromStart();
    }

    private void finishReveal(TreeItem<T> branch) {
        var reveal = reveals.remove(branch);
        if (reveal == null) return;
        reveal.timeline.stop();
        if (reveal.closing) branch.setExpanded(false);
        animationRevision.set(animationRevision.get() + 1);
    }

    public ReadOnlyObjectProperty<TreeItem<T>> currentItemProperty() {
        return current.getReadOnlyProperty();
    }

    public TreeItem<T> getCurrentItem() {
        return current.get();
    }

    public StringProperty emptyTextProperty() {
        return empty.textProperty();
    }

    public void setEmptyText(String value) {
        empty.setText(value);
    }

    public void setEmptyGraphic(Node graphic) {
        empty.setGraphic(graphic);
    }

    /** Keys should be unique among all loaded nodes. By default the value itself is the key. */
    public void setNodeKey(Function<? super T, ?> getter) {
        nodeKey = Objects.requireNonNull(getter);
    }

    public TreeItem<T> getNode(Object key) {
        return descendants(root).filter(item -> Objects.equals(key, nodeKey.apply(item.getValue())))
                .findFirst().orElse(null);
    }

    public List<?> getCheckedKeys() {
        return getCheckedKeys(false);
    }

    public List<?> getCheckedKeys(boolean leafOnly) {
        return getCheckedNodes(leafOnly, false).stream().map(item -> nodeKey.apply(item.getValue())).toList();
    }

    public List<?> getHalfCheckedKeys() {
        return getHalfCheckedNodes().stream().map(item -> nodeKey.apply(item.getValue())).toList();
    }

    public void setCheckedKeys(Collection<?> keys) {
        setCheckedKeys(keys, false);
    }

    public void setCheckedKeys(Collection<?> keys, boolean leafOnly) {
        var wanted = new HashSet<>(keys);
        setCheckedNodes(descendants(root).filter(item -> wanted.contains(nodeKey.apply(item.getValue()))).toList(),
                leafOnly);
    }

    /** Expands matching loaded nodes and their ancestors. */
    public void expandKeys(Collection<?> keys) {
        var wanted = new HashSet<>(keys);
        descendants(root).filter(item -> wanted.contains(nodeKey.apply(item.getValue()))).toList()
                .forEach(item -> {
                    for (var node = item; node != root; node = node.getParent())
                        node.setExpanded(true);
                });
    }

    public void expandAll() {
        descendants(root).filter(item -> !item.isLeaf()).toList().forEach(item -> item.setExpanded(true));
    }

    /** Applies to the current tree and subsequent data supplied to setData. */
    public void setDefaultExpandAll(boolean value) {
        defaultExpandAll = value;
        if (value) expandAll();
    }

    private java.util.stream.Stream<TreeItem<T>> descendants(TreeItem<T> parent) {
        return parent.getChildren().stream().flatMap(
                child -> java.util.stream.Stream.concat(java.util.stream.Stream.of(child), descendants(child)));
    }

    public void setLabelGetter(Function<? super T, String> getter) {
        setProps(new Props<>(getter, props.children(), props.isLeaf()));
    }

    private Function<? super T, String> nodeLabelStyle;

    /**
     * Replaces the current label style rule. Only matching nodes receive the supplied
     * inline JavaFX CSS on their built-in labels. Other labels follow the stylesheets.
     */
    public void setLabelStyleIf(String style, Predicate<? super T> condition) {
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(condition, "condition");
        nodeLabelStyle = value -> condition.test(value) ? style : "";
        view.refresh();
    }

    /** Removes the inline label style rule for this tree. */
    public void clearLabelStyle() {
        nodeLabelStyle = null;
        view.refresh();
    }

    String nodeLabelStyle(T value) {
        return nodeLabelStyle == null ? null : nodeLabelStyle.apply(value);
    }

    public void setNodeGraphicFactory(Function<TreeItem<T>, Node> factory) {
        renderer = factory;
        view.refresh();
    }

    public void setDisabledPredicate(Predicate<TreeItem<T>> predicate) {
        disabled = Objects.requireNonNull(predicate);
        if (!isCheckStrictly()) recompute(root);
        view.refresh();
    }

    public void setOnCheckChange(Consumer<TreeItem<T>> handler) {
        onCheckChange = handler;
    }

    public void setOnNodeClick(Consumer<TreeItem<T>> handler) {
        onNodeClick = handler;
    }

    public void setOnNodeDrop(Consumer<Drop<T>> handler) {
        onDrop = handler;
    }

    public void setOnNodeDrag(Consumer<NodeDrag<T>> handler) {
        onNodeDrag = handler;
    }

    public void setOnNodeDragStart(Consumer<TreeItem<T>> handler) {
        onNodeDragStart = handler;
    }

    public void setOnNodeDragEnter(BiConsumer<TreeItem<T>, TreeItem<T>> handler) {
        onNodeDragEnter = handler;
    }

    public void setOnNodeDragLeave(BiConsumer<TreeItem<T>, TreeItem<T>> handler) {
        onNodeDragLeave = handler;
    }

    public void setOnNodeDragOver(BiConsumer<TreeItem<T>, TreeItem<T>> handler) {
        onNodeDragOver = handler;
    }

    /** On cancellation, the source is present and both target and position are null. */
    public void setOnNodeDragEnd(Consumer<Drop<T>> handler) {
        onNodeDragEnd = handler;
    }

    public void setAllowDrag(Predicate<TreeItem<T>> predicate) {
        allowDrag = Objects.requireNonNull(predicate);
    }

    public void setAllowDrop(Predicate<Drop<T>> predicate) {
        allowDrop = Objects.requireNonNull(predicate);
    }

    public BooleanProperty showCheckboxProperty() {
        return showCheckbox;
    }

    public boolean isShowCheckbox() {
        return showCheckbox.get();
    }

    public void setShowCheckbox(boolean value) {
        showCheckbox.set(value);
    }

    public BooleanProperty checkStrictlyProperty() {
        return checkStrictly;
    }

    public boolean isCheckStrictly() {
        return checkStrictly.get();
    }

    public void setCheckStrictly(boolean value) {
        checkStrictly.set(value);
    }

    public BooleanProperty accordionProperty() {
        return accordion;
    }

    public boolean isAccordion() {
        return accordion.get();
    }

    public void setAccordion(boolean value) {
        accordion.set(value);
    }

    public BooleanProperty expandOnClickNodeProperty() {
        return expandOnClickNode;
    }

    public boolean isExpandOnClickNode() {
        return expandOnClickNode.get();
    }

    public void setExpandOnClickNode(boolean value) {
        expandOnClickNode.set(value);
    }

    public BooleanProperty checkOnClickNodeProperty() {
        return checkOnClickNode;
    }

    public boolean isCheckOnClickNode() {
        return checkOnClickNode.get();
    }

    public void setCheckOnClickNode(boolean value) {
        checkOnClickNode.set(value);
    }

    public BooleanProperty checkOnClickLeafProperty() {
        return checkOnClickLeaf;
    }

    public boolean isCheckOnClickLeaf() {
        return checkOnClickLeaf.get();
    }

    public void setCheckOnClickLeaf(boolean value) {
        checkOnClickLeaf.set(value);
    }

    public BooleanProperty draggableProperty() {
        return draggable;
    }

    public boolean isDraggable() {
        return draggable.get();
    }

    public void setDraggable(boolean value) {
        draggable.set(value);
    }

    public BooleanProperty highlightCurrentProperty() {
        return highlightCurrent;
    }

    public boolean isHighlightCurrent() {
        return highlightCurrent.get();
    }

    public void setHighlightCurrent(boolean value) {
        highlightCurrent.set(value);
    }

    public boolean isChecked(TreeItem<T> item) {
        return checked.contains(item);
    }

    public boolean isHalfChecked(TreeItem<T> item) {
        return partial.contains(item);
    }

    public List<TreeItem<T>> getCheckedNodes() {
        return getCheckedNodes(false, false);
    }

    public List<TreeItem<T>> getCheckedNodes(boolean leafOnly) {
        return getCheckedNodes(leafOnly, false);
    }

    public List<TreeItem<T>> getCheckedNodes(boolean leafOnly, boolean includeHalfChecked) {
        var nodes = includeHalfChecked
                ? descendants(root).filter(item -> checked.contains(item) || partial.contains(item))
                : checked.stream();
        return nodes.filter(item -> !leafOnly || item.isLeaf()).toList();
    }

    public List<TreeItem<T>> getHalfCheckedNodes() {
        return List.copyOf(partial);
    }

    public void setChecked(TreeItem<T> item, boolean value) {
        if (!belongs(item) || disabled.test(item)) return;
        cascade(item, value);
        for (var parent = item.getParent(); !isCheckStrictly() && parent != root; parent = parent.getParent()) {
            aggregate(parent);
        }
        view.refresh();
        if (onCheckChange != null) onCheckChange.accept(item);
    }

    void toggleCheckbox(TreeItem<T> item) {
        boolean selected = isCheckStrictly() ? isChecked(item) : isEffectivelyChecked(item);
        setChecked(item, !selected);
    }

    private boolean isEffectivelyChecked(TreeItem<T> item) {
        var children = item.getChildren();
        if (children.isEmpty()) return isDisabled(item) || isChecked(item);
        return children.stream().allMatch(this::isEffectivelyChecked);
    }

    public void setCheckedNodes(Collection<TreeItem<T>> items) {
        setCheckedNodes(items, false);
    }

    /** With leafOnly, applies the request to loaded leaves and derives parent states from them. */
    public void setCheckedNodes(Collection<TreeItem<T>> items, boolean leafOnly) {
        var selected = List.copyOf(items);
        checked.clear();
        partial.clear();
        for (var item : selected) {
            if (!belongs(item) || isDisabled(item)) continue;
            if (leafOnly && !isCheckStrictly() && !item.isLeaf()) {
                descendants(item).filter(TreeItem::isLeaf).filter(leaf -> !isDisabled(leaf)).forEach(checked::add);
            } else {
                cascade(item, true);
            }
        }
        if (!isCheckStrictly()) recompute(root);
        view.refresh();
    }

    private void cascade(TreeItem<T> item, boolean value) {
        if (isDisabled(item) && (isCheckStrictly() || item.isLeaf())) return;
        partial.remove(item);
        if (value)
            checked.add(item);
        else
            checked.remove(item);
        if (!isCheckStrictly()) {
            for (var child : item.getChildren())
                cascade(child, value);
            aggregate(item);
        }
    }

    private void aggregate(TreeItem<T> item) {
        var children = item.getChildren();
        if (children.isEmpty()) {
            partial.remove(item);
            return;
        }
        int full = 0;
        boolean any = false;
        for (var child : children) {
            if (checked.contains(child)) full++;
            any |= partial.contains(child);
        }
        if (full == children.size())
            checked.add(item);
        else
            checked.remove(item);
        if (full < children.size() && (full > 0 || any))
            partial.add(item);
        else
            partial.remove(item);
    }

    private void recompute(TreeItem<T> item) {
        for (var child : item.getChildren())
            recompute(child);
        if (item != root) aggregate(item);
    }

    private boolean belongs(TreeItem<T> item) {
        if (item == root) return false;
        while (item != null && item != root)
            item = item.getParent();
        return item == root;
    }

    private void childrenChanged(TreeItem.TreeModificationEvent<T> event) {
        if (rebuilding) return;
        if (!isCheckStrictly() && checked.contains(event.getTreeItem()) && event.wasAdded()) {
            for (var child : event.getAddedChildren())
                cascade(child, true);
        }
        checked.removeIf(item -> !belongs(item));
        partial.removeIf(item -> !belongs(item));
        if (!isCheckStrictly()) recompute(root);
        rebuild();
    }

    /** A matching descendant keeps its ancestors visible. Passing null restores the original expansion state. */
    public void filter(Predicate<? super T> predicate) {
        filter = predicate;
        rebuild();
    }

    private void rebuild() {
        var selected = getCurrentItem();
        rebuilding = true;
        try {
            originals.clear();
            TreeItem<T> displayed = root;
            if (filter != null) {
                displayed = new TreeItem<>();
                for (var item : getItems()) {
                    var match = project(item);
                    if (match != null) displayed.getChildren().add(match);
                }
                displayed.setExpanded(true);
            }
            view.setRoot(displayed);
            empty.setVisible(displayed.getChildren().isEmpty());
            if (selected != null) setCurrentItem(belongs(selected) ? selected : null);
            view.refresh();
        } finally {
            rebuilding = false;
        }
    }

    private TreeItem<T> project(TreeItem<T> source) {
        var displayed = new TreeItem<T>(source.getValue()) {

            @Override
            public boolean isLeaf() {
                return source.isLeaf();
            }
        };
        for (var child : source.getChildren()) {
            var match = project(child);
            if (match != null) displayed.getChildren().add(match);
        }
        if (displayed.getChildren().isEmpty() && !filter.test(source.getValue())) return null;
        displayed.setExpanded(true);
        originals.put(displayed, source);
        return displayed;
    }

    public void setCurrentItem(TreeItem<T> item) {
        TreeItem<T> displayed = item;
        if (filter != null && item != null) {
            displayed = originals.entrySet().stream().filter(entry -> entry.getValue() == item)
                    .map(Map.Entry::getKey).findFirst().orElse(null);
        }
        if (displayed == null || !belongs(item)) {
            view.getSelectionModel().clearSelection();
            current.set(null);
        } else {
            view.getSelectionModel().select(displayed);
            current.set(item);
        }
    }

    TreeItem<T> original(TreeItem<T> item) {
        return originals.getOrDefault(item, item);
    }

    boolean isDisabled(TreeItem<T> item) {
        return disabled.test(item);
    }

    String label(TreeItem<T> item) {
        return props.label().apply(item.getValue());
    }

    Node graphic(TreeItem<T> item) {
        return renderer == null ? item.getGraphic() : renderer.apply(item);
    }

    void clearRowFocus() {
        view.getFocusModel().focus(-1);
    }

    void focusRow(int row) {
        view.requestFocus();
        view.getFocusModel().focus(row);
    }

    void activate(TreeItem<T> displayed) {
        var item = original(displayed);
        if (isExpandOnClickNode() && !displayed.isLeaf()) toggleBranch(displayed);
        if (isShowCheckbox() && (isCheckOnClickNode() || (isCheckOnClickLeaf() && item.isLeaf()))) {
            setChecked(item, !isChecked(item));
        }
        int row = view.getRow(displayed);
        if (row >= 0) focusRow(row);
        if (onNodeClick != null) onNodeClick.accept(item);
    }

    boolean startDrag(TreeItem<T> source) {
        if (!isDraggable() || !allowDrag.test(source)) return false;
        dragged = source;
        return true;
    }

    void notifyDrag(DragPhase phase, TreeItem<T> target, DropPosition position) {
        if (dragged == null) return;
        switch (phase) {
            case START -> {
                if (onNodeDragStart != null) onNodeDragStart.accept(dragged);
            }
            case ENTER -> {
                if (onNodeDragEnter != null) onNodeDragEnter.accept(dragged, target);
            }
            case LEAVE -> {
                if (onNodeDragLeave != null) onNodeDragLeave.accept(dragged, target);
            }
            case OVER -> {
                if (onNodeDragOver != null) onNodeDragOver.accept(dragged, target);
            }
            case END -> {
                if (onNodeDragEnd != null) onNodeDragEnd.accept(new Drop<>(dragged, target, position));
            }
            case DROP -> {
                if (onDrop != null) onDrop.accept(new Drop<>(dragged, target, position));
            }
        }
        if (onNodeDrag != null)
            onNodeDrag.accept(new NodeDrag<>(phase, dragged, target, position));
    }

    void endDrag(Drop<T> drop) {
        try {
            notifyDrag(DragPhase.END, drop == null ? null : drop.target(), drop == null ? null : drop.position());
        } finally {
            dragged = null;
        }
    }

    Drop<T> dropTarget(TreeItem<T> target, DropPosition position) {
        if (!isDraggable() || dragged == null || !belongs(dragged) || !belongs(target)) return null;
        for (var ancestor = target; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == dragged) return null;
        }
        var drop = new Drop<>(dragged, target, position);
        return allowDrop.test(drop) ? drop : null;
    }

    void move(Drop<T> drop) {
        var source = drop.source();
        var target = drop.target();
        rebuilding = true;
        try {
            source.getParent().getChildren().remove(source);
            if (drop.position() == DropPosition.INSIDE) {
                target.getChildren().add(source);
                target.setExpanded(true);
            } else {
                var siblings = target.getParent().getChildren();
                int index = siblings.indexOf(target);
                siblings.add(index + (drop.position() == DropPosition.AFTER ? 1 : 0), source);
            }
        } finally {
            rebuilding = false;
        }
        if (!isCheckStrictly()) recompute(root);
        rebuild();
        setCurrentItem(source);
        notifyDrag(DragPhase.DROP, target, drop.position());
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TREE;
    }

    @Override
    public String getUserAgentStylesheet() {
        return getTheme().toData();
    }
}
