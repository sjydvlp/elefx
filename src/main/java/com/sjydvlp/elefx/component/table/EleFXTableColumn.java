package com.sjydvlp.elefx.component.table;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.component.tag.EleFXTag;
import com.sjydvlp.elefx.component.tooltip.EleFXTooltip;
import com.sjydvlp.elefx.component.tooltip.EleFXTooltipPlacement;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.function.Function;

/** A JavaFX table column whose default cells wrap text or show an overflow tooltip. */
public class EleFXTableColumn<S, T> extends TableColumn<S, T> {

    private static final String SELECTION_COLUMN = "ele-table-selection-column";

    private final BooleanProperty showOverflowTooltip = new SimpleBooleanProperty(this, "showOverflowTooltip");

    private final ObjectProperty<Function<EleFXTableTooltipData<S, T>, ?>> tooltipFormatter = new SimpleObjectProperty<>(
            this, "tooltipFormatter");

    private final ObjectProperty<EleFXTableColumnFixed> fixed = new SimpleObjectProperty<>(this, "fixed",
            EleFXTableColumnFixed.NONE);

    public EleFXTableColumn() {
        this("");
    }

    public EleFXTableColumn(String text) {
        super(text);
        setSortable(false);
        setCellFactory(column -> new OverflowCell<>(this));
        graphicProperty().addListener(observable -> updateSelectionColumnStyle());
        textProperty().addListener(observable -> updateSelectionColumnStyle());
    }

    private void updateSelectionColumnStyle() {
        if (getGraphic() instanceof CheckBox && (getText() == null || getText().isBlank())) {
            if (!getStyleClass().contains(SELECTION_COLUMN)) {
                getStyleClass().add(SELECTION_COLUMN);
            }
        } else {
            getStyleClass().remove(SELECTION_COLUMN);
        }
    }

    public final boolean isShowOverflowTooltip() {
        return showOverflowTooltip.get();
    }

    public final void setShowOverflowTooltip(boolean showOverflowTooltip) {
        this.showOverflowTooltip.set(showOverflowTooltip);
    }

    public final BooleanProperty showOverflowTooltipProperty() {
        return showOverflowTooltip;
    }

    public final Function<EleFXTableTooltipData<S, T>, ?> getTooltipFormatter() {
        return tooltipFormatter.get();
    }

    /** Returns text or a fresh Node for overflowing cells. A null formatter inherits the table formatter. */
    public final void setTooltipFormatter(Function<EleFXTableTooltipData<S, T>, ?> value) {
        tooltipFormatter.set(value);
    }

    public final ObjectProperty<Function<EleFXTableTooltipData<S, T>, ?>> tooltipFormatterProperty() {
        return tooltipFormatter;
    }

    /** For a grouped header, set this property on the top-level column to pin the entire group. */
    public final EleFXTableColumnFixed getFixed() {
        EleFXTableColumnFixed value = fixed.get();
        return value == null ? EleFXTableColumnFixed.NONE : value;
    }

    public final void setFixed(EleFXTableColumnFixed value) {
        fixed.set(value);
    }

    public final ObjectProperty<EleFXTableColumnFixed> fixedProperty() {
        return fixed;
    }

    /**
     * Reads a plain value from each row. Use {@link #setCellValueFactory} when the row
     * exposes an observable property that the cell should track.
     */
    public final void setValueGetter(Function<? super S, ? extends T> getter) {
        Objects.requireNonNull(getter, "getter");
        setCellValueFactory(cell -> {
            S row = cell.getValue();
            T value = row == null ? null : getter.apply(row);
            return new ReadOnlyObjectWrapper<>(value).getReadOnlyProperty();
        });
    }

    /**
     * Installs a cell factory that displays a node created from the current row item.
     * Return a fresh node (or {@code null}) for each invocation. Empty cells have no graphic.
     */
    public final void setCellGraphicFactory(Function<? super S, ? extends Node> factory) {
        Objects.requireNonNull(factory, "factory");
        setCellFactory(new GraphicFactory<>(factory));
    }

    static record GraphicFactory<S, T> (Function<? super S, ? extends Node> renderer)
            implements
                javafx.util.Callback<TableColumn<S, T>, TableCell<S, T>> {

        @Override
        public TableCell<S, T> call(TableColumn<S, T> column) {
            return new GraphicCell<>((EleFXTableColumn<S, T>) column, renderer);
        }
    }

    final Function<? super S, ? extends Node> graphicRenderer() {
        if (getCellFactory()instanceof GraphicFactory<S, T> factory) {
            return factory.renderer();
        }
        return null;
    }

    private static final class GraphicCell<S, T> extends TooltipCell<S, T> {

        private final Function<? super S, ? extends Node> factory;

        private final OverflowGraphic overflowGraphic = new OverflowGraphic();

        private final InvalidationListener rowItemChanged = observable -> updateGraphic();

        private final WeakInvalidationListener weakRowItemChanged = new WeakInvalidationListener(rowItemChanged);

        private TableRow<S> renderedRow;

        private S renderedItem;

        private int renderedIndex = -1;

        private GraphicCell(EleFXTableColumn<S, T> column, Function<? super S, ? extends Node> factory) {
            super(column);
            this.factory = factory;
            overflowGraphic.ellipsis.fontProperty().bind(fontProperty());
            overflowGraphic.ellipsis.textFillProperty().bind(textFillProperty());
            tableRowProperty().addListener((observable, oldRow, row) -> {
                if (oldRow != null) {
                    oldRow.itemProperty().removeListener(weakRowItemChanged);
                }
                if (row != null) {
                    row.itemProperty().addListener(weakRowItemChanged);
                }
                updateGraphic();
            });
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            updateGraphic();
        }

        private void updateGraphic() {
            TableRow<S> row = getTableRow();
            var table = getTableView();
            var items = table == null ? null : table.getItems();
            int index = getIndex();
            if (isEmpty() || row == null || index != row.getIndex() || items == null
                    || index < 0 || index >= items.size()) {
                renderedRow = null;
                renderedItem = null;
                renderedIndex = -1;
                overflowGraphic.setContent(null);
                setGraphic(null);
                return;
            }
            S item = items.get(index);
            if (row != renderedRow || item != renderedItem || index != renderedIndex) {
                resetTooltip();
                Node graphic = factory.apply(item);
                overflowGraphic.setContent(graphic);
                setGraphic(graphic == null ? null : overflowGraphic);
                renderedRow = row;
                renderedItem = item;
                renderedIndex = index;
            }
        }

        @Override
        protected void layoutChildren() {
            overflowGraphic.setEllipsisEnabled(isOverflowTooltipEnabled());
            super.layoutChildren();
        }
    }

    private static final class OverflowGraphic extends Region {

        private final Pane viewport = new Pane();

        private final Rectangle clip = new Rectangle();

        private final Label ellipsis = new Label("…");

        private Node content;

        private boolean ellipsisEnabled;

        OverflowGraphic() {
            setMinWidth(0);
            setMaxWidth(USE_PREF_SIZE);
            ellipsis.setMouseTransparent(true);
            getChildren().addAll(viewport, ellipsis);
        }

        void setContent(Node node) {
            content = node;
            viewport.getChildren().clear();
            if (node != null) {
                viewport.getChildren().add(node);
            }
            requestLayout();
        }

        void setEllipsisEnabled(boolean enabled) {
            if (ellipsisEnabled != enabled) {
                ellipsisEnabled = enabled;
                requestLayout();
            }
        }

        @Override
        protected double computePrefWidth(double height) {
            return content == null ? 0 : content.prefWidth(height);
        }

        @Override
        protected double computePrefHeight(double width) {
            return content == null ? 0 : content.prefHeight(-1);
        }

        @Override
        protected void layoutChildren() {
            if (content == null) {
                ellipsis.setVisible(false);
                return;
            }
            double width = getWidth();
            double contentWidth = snappedGraphicWidth();
            boolean overflowing = ellipsisEnabled && contentWidth > width;
            double markerWidth = overflowing ? Math.min(width, snapSizeX(ellipsis.prefWidth(-1))) : 0;
            double markerGap = overflowing && content instanceof HBox box
                    && box.getChildren().stream().allMatch(EleFXTag.class::isInstance)
                            ? Math.min(Math.max(0, box.getSpacing()), width - markerWidth)
                            : 0;
            double viewportWidth = width - markerWidth - markerGap;
            viewport.resizeRelocate(0, 0, viewportWidth, getHeight());
            content.autosize();
            content.relocate(0, snapPositionY((getHeight() - content.getLayoutBounds().getHeight()) / 2));
            if (overflowing) {
                clip.setWidth(completeTagWidth(viewportWidth));
                clip.setHeight(getHeight());
                viewport.setClip(clip);
            } else {
                viewport.setClip(null);
            }
            ellipsis.setVisible(overflowing);
            if (overflowing) {
                double markerHeight = snapSizeY(ellipsis.prefHeight(markerWidth));
                ellipsis.resizeRelocate(clip.getWidth() == 0 ? 0 : clip.getWidth() + markerGap,
                        snapPositionY((getHeight() - markerHeight) / 2),
                        markerWidth, markerHeight);
            }
        }

        private double snappedGraphicWidth() {
            return snapSizeX(content.prefWidth(-1));
        }

        private double completeTagWidth(double availableWidth) {
            if (content instanceof EleFXTag) {
                return content.getBoundsInParent().getMaxX() <= availableWidth ? availableWidth : 0;
            }
            if (!(content instanceof HBox box)
                    || !box.getChildren().stream().filter(Node::isManaged).allMatch(EleFXTag.class::isInstance)) {
                return availableWidth;
            }
            box.layout();
            double cutoff = availableWidth;
            double completeWidth = 0;
            for (Node tag : box.getChildren().stream().filter(Node::isManaged).toList()) {
                var bounds = tag.getBoundsInParent();
                if (bounds.getMaxX() > availableWidth) {
                    cutoff = Math.min(cutoff, bounds.getMinX());
                } else {
                    completeWidth = Math.max(completeWidth, bounds.getMaxX());
                }
            }
            // Keep the clip inside the gap so antialiasing cannot expose the next tag's border.
            return Math.max(0, Math.min(cutoff, completeWidth));
        }
    }

    private abstract static class TooltipCell<S, T> extends TableCell<S, T> {

        private final EleFXTableColumn<S, T> column;

        private final InvalidationListener settingsChanged = observable -> refreshTooltipSettings();

        private final WeakInvalidationListener weakSettingsChanged = new WeakInvalidationListener(settingsChanged);

        private final InvalidationListener tooltipRowChanged = observable -> invalidateTooltip();

        private final WeakInvalidationListener weakTooltipRowChanged = new WeakInvalidationListener(tooltipRowChanged);

        private EleFXTooltip overflowTooltip;

        TooltipCell(EleFXTableColumn<S, T> column) {
            this.column = column;
            column.showOverflowTooltipProperty().addListener(weakSettingsChanged);
            column.tooltipFormatterProperty().addListener(weakSettingsChanged);
            tableViewProperty().addListener((observable, previous, current) -> {
                if (previous instanceof EleFXTable<S> table) {
                    table.showOverflowTooltipProperty().removeListener(weakSettingsChanged);
                    table.tooltipFormatterProperty().removeListener(weakSettingsChanged);
                }
                if (current instanceof EleFXTable<S> table) {
                    table.showOverflowTooltipProperty().addListener(weakSettingsChanged);
                    table.tooltipFormatterProperty().addListener(weakSettingsChanged);
                }
                refreshTooltipSettings();
            });
            tableRowProperty().addListener((observable, previous, current) -> {
                if (previous != null) {
                    previous.itemProperty().removeListener(weakTooltipRowChanged);
                }
                if (current != null) {
                    current.itemProperty().addListener(weakTooltipRowChanged);
                }
                invalidateTooltip();
            });
            sceneProperty().addListener(observable -> invalidateTooltip());
            indexProperty().addListener(observable -> invalidateTooltip());
            refreshTooltipSettings();
        }

        final boolean isOverflowTooltipEnabled() {
            return column.isShowOverflowTooltip()
                    || getTableView()instanceof EleFXTable<S> table && table.isShowOverflowTooltip();
        }

        private void refreshTooltipSettings() {
            setWrapText(!isOverflowTooltipEnabled());
            invalidateTooltip();
        }

        private void invalidateTooltip() {
            resetTooltip();
            requestLayout();
        }

        final void resetTooltip() {
            if (overflowTooltip != null) {
                overflowTooltip.setDisabled(true);
                overflowTooltip.setContentNode(null);
            }
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            resetTooltip();
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            var row = getTableRow();
            if (!isOverflowTooltipEnabled() || getScene() == null || isEmpty()
                    || row == null || row.getItem() == null || row.getIndex() != getIndex()
                    || !isContentOverflowing()) {
                resetTooltip();
                return;
            }
            if (overflowTooltip == null) {
                overflowTooltip = new EleFXTooltip(this, "");
                overflowTooltip.setDisabled(true);
                overflowTooltip.setPlacement(EleFXTooltipPlacement.TOP);
                overflowTooltip.getFallbackPlacements().add(EleFXTooltipPlacement.BOTTOM);
                overflowTooltip.setOffset(0);
                overflowTooltip.getTriggerKeys().clear();
            }
            if (!overflowTooltip.isDisabled()) {
                return;
            }

            var data = new EleFXTableTooltipData<>(row.getItem(), column, getItem());
            var formatter = column.getTooltipFormatter();
            Object content = null;
            if (formatter != null) {
                content = formatter.apply(data);
            } else if (getTableView()instanceof EleFXTable<S> table && table.getTooltipFormatter() != null) {
                content = table.getTooltipFormatter().apply(data);
            }
            if (content == null) {
                content = getText() != null ? getText() : getItem();
                if (content instanceof Node) {
                    return;
                }
            }
            if (content instanceof Node node) {
                overflowTooltip.setContentNode(node);
                overflowTooltip.setEnterable(true);
                overflowTooltip.setHideAfter(200);
            } else {
                String text = Objects.toString(content, "");
                if (text.isEmpty()) {
                    return;
                }
                overflowTooltip.setContent(text);
                overflowTooltip.setEnterable(false);
                overflowTooltip.setHideAfter(0);
            }
            overflowTooltip.setDisabled(false);
        }

        private boolean isContentOverflowing() {
            String text = getText();
            // The default Labeled skin exposes its rendered text with the public CSS class.
            if (text != null && !text.isEmpty()
                    && lookup(".text")instanceof Text rendered && !text.equals(rendered.getText())) {
                return true;
            }
            Node graphic = getGraphic();
            return graphic != null && graphic.prefWidth(-1) > getWidth() - snappedLeftInset() - snappedRightInset();
        }
    }

    private static final class OverflowCell<S, T> extends TooltipCell<S, T> {

        private final EleFXTableColumn<S, T> column;

        private HBox treeGraphic;

        private Region treeIndent;

        private Button treeToggle;

        private EleFXIcon treeIcon;

        private final InvalidationListener treeChanged = observable -> updateTreeGraphic();

        private final WeakInvalidationListener weakTreeChanged = new WeakInvalidationListener(treeChanged);

        private OverflowCell(EleFXTableColumn<S, T> column) {
            super(column);
            this.column = column;
            sceneProperty().addListener((observable, oldScene, scene) -> {
                updateTreeGraphic();
            });
            tableViewProperty().addListener((observable, previous, current) -> {
                if (previous instanceof EleFXTable<S> table) {
                    table.tree().removeInvalidationListener(weakTreeChanged);
                }
                if (current instanceof EleFXTable<S> table) {
                    table.tree().addInvalidationListener(weakTreeChanged);
                }
                updateTreeGraphic();
            });
            indexProperty().addListener(observable -> updateTreeGraphic());
        }

        @Override
        protected double computePrefHeight(double width) {
            // TableRowSkin measures its cells with an unconstrained width.
            return super.computePrefHeight(isWrapText() && width < 0 ? column.getWidth() : width);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(!empty && item != null && !(item instanceof Node) ? item.toString() : null);
            updateTreeGraphic();
        }

        private S rowItem() {
            var table = getTableView();
            int index = getIndex();
            return isEmpty() || table == null || table.getItems() == null
                    || index < 0 || index >= table.getItems().size() ? null : table.getItems().get(index);
        }

        private void updateTreeGraphic() {
            S item = rowItem();
            var row = item != null && getTableView()instanceof EleFXTable<S> table
                    ? table.tree().row(item, column)
                    : null;
            Node value = getItem()instanceof Node node && !isEmpty() ? node : null;
            if (row == null) {
                if (treeGraphic != null) {
                    treeIcon.setLoading(false);
                    if (treeGraphic.getChildren().size() > 2) {
                        treeGraphic.getChildren().remove(2);
                    }
                }
                if (getGraphic() != value) {
                    setGraphic(value);
                }
                return;
            }
            if (treeGraphic == null) {
                createTreeGraphic();
            }
            treeIndent.setPrefWidth(row.depth() * 16.0);
            treeToggle.setOpacity(row.branch() ? 1 : 0);
            treeToggle.setMouseTransparent(!row.branch());
            treeToggle.setFocusTraversable(row.branch());
            treeToggle.setDisable(!row.branch());
            treeIcon.setType(row.loading()
                    ? EleFXIconType.LOADING
                    : row.expanded() ? EleFXIconType.ARROW_DOWN : EleFXIconType.ARROW_RIGHT);
            treeIcon.setLoading(row.loading() && getScene() != null);
            treeToggle.setAccessibleText(row.loading()
                    ? "Loading children"
                    : row.expanded() ? "Collapse children" : "Expand children");
            var children = treeGraphic.getChildren();
            if (children.size() > 2 && children.get(2) != value) {
                children.remove(2);
            }
            if (value != null && children.size() == 2) {
                children.add(value);
            }
            if (getGraphic() != treeGraphic) {
                setGraphic(treeGraphic);
            }
        }

        private void createTreeGraphic() {
            treeIcon = new EleFXIcon(EleFXIconType.ARROW_RIGHT, 12);
            treeToggle = new Button("", treeIcon);
            treeToggle.getStyleClass().add("ele-table__expand-toggle");
            EleFXTable.trackPointerFocus(treeToggle);
            treeToggle.setOnAction(event -> {
                if (getTableView()instanceof EleFXTable<S> table) {
                    table.tree().toggle(rowItem());
                }
            });
            treeIndent = new Region();
            treeIndent.setMinWidth(Region.USE_PREF_SIZE);
            treeGraphic = new HBox(treeIndent, treeToggle);
            treeGraphic.setAlignment(Pos.CENTER_LEFT);
        }
    }
}
