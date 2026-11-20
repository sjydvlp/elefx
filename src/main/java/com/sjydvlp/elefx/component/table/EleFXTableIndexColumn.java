package com.sjydvlp.elefx.component.table;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;

import java.util.function.IntUnaryOperator;

/** Displays row numbers in the current view order. */
public class EleFXTableIndexColumn<S> extends EleFXTableColumn<S, S> {

    private final ObjectProperty<IntUnaryOperator> indexMethod = new SimpleObjectProperty<>(this, "indexMethod");

    public EleFXTableIndexColumn() {
        this("");
    }

    public EleFXTableIndexColumn(String title) {
        super(title);
        setMinWidth(48);
        setPrefWidth(48);
        setMaxWidth(48);
        setResizable(false);
        setEditable(false);
        setValueGetter(row -> row);
        setCellFactory(column -> new IndexCell());
    }

    public final IntUnaryOperator getIndexMethod() {
        return indexMethod.get();
    }

    /** Receives a zero-based row index. Pass null to restore one-based numbering. */
    public final void setIndexMethod(IntUnaryOperator method) {
        indexMethod.set(method);
    }

    public final ObjectProperty<IntUnaryOperator> indexMethodProperty() {
        return indexMethod;
    }

    private final class IndexCell extends TableCell<S, S> {

        private final InvalidationListener methodChanged = observable -> updateText();

        private IndexCell() {
            indexMethod.addListener(new WeakInvalidationListener(methodChanged));
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            updateText();
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            updateText();
        }

        private void updateText() {
            int index = getIndex();
            if (isEmpty() || index < 0) {
                setText(null);
                return;
            }
            var method = getIndexMethod();
            setText(Integer.toString(method == null ? index + 1 : method.applyAsInt(index)));
        }
    }
}
