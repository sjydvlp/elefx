package com.sjydvlp.elefx.component.table;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.util.Objects;
import java.util.function.Function;

/**
 * Adds a toggle that displays custom content below the row. Use one expand
 * column per table and leave the fixed cell size disabled for variable row heights.
 */
public class EleFXTableExpandColumn<S> extends EleFXTableColumn<S, S> {

    private final Function<? super S, ? extends Node> contentFactory;

    /** The factory must return a fresh node for each row and each creation. */
    public EleFXTableExpandColumn(Function<? super S, ? extends Node> contentFactory) {
        this.contentFactory = Objects.requireNonNull(contentFactory, "contentFactory");
        setPrefWidth(48);
        setMinWidth(48);
        setMaxWidth(48);
        setResizable(false);
        setValueGetter(item -> item);
        setCellFactory(column -> new ExpandCell());
    }

    Node createContent(S item) {
        return Objects.requireNonNull(contentFactory.apply(item), "expanded content");
    }

    private final class ExpandCell extends TableCell<S, S> {

        private final EleFXIcon icon = new EleFXIcon(EleFXIconType.ARROW_RIGHT, 12);

        private final Button toggle = new Button("", icon);

        ExpandCell() {
            setAlignment(Pos.CENTER);
            toggle.getStyleClass().add("ele-table__expand-toggle");
            EleFXTable.trackPointerFocus(toggle);
            toggle.setOnAction(event -> {
                S item = getTableRow() == null ? null : getTableRow().getItem();
                if (item != null && getTableView()instanceof EleFXTable<S> table) {
                    table.toggleRowExpansion(EleFXTableExpandColumn.this, item);
                    updateToggle(table.isRowExpanded(item));
                }
            });
        }

        @Override
        protected boolean isItemChanged(S oldItem, S newItem) {
            return oldItem != newItem;
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            toggle.disableProperty().unbind();
            if (empty || item == null || !(getTableView()instanceof EleFXTable<S> table)) {
                setGraphic(null);
                return;
            }
            toggle.disableProperty().bind(table.fixedCellSizeProperty().greaterThan(0));
            updateToggle(table.isRowExpanded(item));
            setGraphic(toggle);
        }

        private void updateToggle(boolean expanded) {
            icon.setType(expanded ? EleFXIconType.ARROW_DOWN : EleFXIconType.ARROW_RIGHT);
            toggle.setAccessibleText(expanded ? "Collapse row" : "Expand row");
        }
    }
}
