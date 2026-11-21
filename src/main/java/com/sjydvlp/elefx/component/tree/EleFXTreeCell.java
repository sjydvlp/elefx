package com.sjydvlp.elefx.component.tree;

import com.sjydvlp.elefx.component.checkbox.EleFXCheckbox;
import com.sjydvlp.elefx.component.icon.EleFXCursors;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

final class EleFXTreeCell<T> extends TreeCell<T> {

    private static final PseudoClass LOADING = PseudoClass.getPseudoClass("loading");

    private final EleFXTree<T> tree;

    private final EleFXCheckbox<Void> box = new EleFXCheckbox<>();

    // JavaFX skips disabled controls during mouse picking.
    private final StackPane checkboxArea = new StackPane(box);

    private static final class DisabledCheckboxCursor {

        private static final Cursor INSTANCE = EleFXCursors.createBlockedCursor();
    }

    private final HBox content = new HBox(8);

    private final Label label = new Label();

    private final EleFXIcon loadingIcon = new EleFXIcon(EleFXIconType.LOADING, 14);

    private final InvalidationListener loadingChanged = observable -> {
        updateLoading();
        // An empty load can change isLeaf() without changing TreeItem.leafProperty().
        var disclosure = getDisclosureNode();
        var item = getTreeItem();
        if (disclosure != null && item != null && item.isLeaf()) {
            disclosure.setVisible(false);
            requestLayout();
        }
    };

    private final WeakInvalidationListener weakLoadingChanged = new WeakInvalidationListener(loadingChanged);

    private ReadOnlyBooleanProperty itemLoading;

    private final javafx.beans.InvalidationListener expansionChanged = o -> updateExpansion();

    private final javafx.scene.shape.Rectangle revealClip = new javafx.scene.shape.Rectangle();

    private final javafx.scene.transform.Rotate caretOffset = new javafx.scene.transform.Rotate();

    private Node animatedCaret;

    EleFXTreeCell(EleFXTree<T> tree) {
        this.tree = tree;
        setMinHeight(0);
        tree.animationRevision.addListener(new javafx.beans.WeakInvalidationListener(expansionChanged));
        treeItemProperty().addListener(o -> updateExpansion());
        revealClip.widthProperty().bind(widthProperty());
        content.setAlignment(Pos.CENTER_LEFT);
        label.getStyleClass().add("ele-tree__label");
        content.setMinHeight(EleFXTree.ROW_HEIGHT);
        loadingIcon.getStyleClass().add("ele-tree__loading-icon");
        loadingIcon.setMouseTransparent(true);
        loadingIcon.managedProperty().bind(loadingIcon.visibleProperty());
        sceneProperty().addListener(observable -> updateLoading());
        updateLoading();
        setAlignment(Pos.TOP_LEFT);
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (isEmpty() || e.getButton() != MouseButton.PRIMARY || getTreeItem().isLeaf()) return;
            var disclosure = getDisclosureNode();
            if (disclosure != null
                    && disclosure.getBoundsInParent().contains(sceneToLocal(e.getSceneX(), e.getSceneY()))) {
                tree.toggleBranch(getTreeItem());
                e.consume();
            }
        });
        box.setFocusTraversable(false);
        checkboxArea.setPickOnBounds(true);
        checkboxArea.setMaxHeight(USE_PREF_SIZE);
        checkboxArea.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (box.isDisabled()) {
                tree.focusRow(getIndex());
                event.consume();
            } else {
                tree.clearRowFocus();
            }
        });
        checkboxArea.addEventFilter(MouseEvent.MOUSE_RELEASED, this::consumeDisabledCheckboxEvent);
        checkboxArea.addEventFilter(MouseEvent.MOUSE_CLICKED, this::consumeDisabledCheckboxEvent);
        checkboxArea.addEventFilter(MouseEvent.DRAG_DETECTED, this::consumeDisabledCheckboxEvent);
        checkboxArea.addEventHandler(MouseEvent.MOUSE_PRESSED, MouseEvent::consume);
        checkboxArea.addEventHandler(MouseEvent.MOUSE_RELEASED, MouseEvent::consume);
        box.setOnAction(e -> {
            var item = tree.original(getTreeItem());
            if (item != null) tree.toggleCheckbox(item);
        });
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (isEmpty() || e.getButton() != MouseButton.PRIMARY || e.getClickCount() != 1) return;
            for (Node n = e.getPickResult().getIntersectedNode(); n != null && n != this; n = n.getParent()) {
                if (n == checkboxArea || n == getDisclosureNode() || n instanceof ButtonBase) return;
            }
            tree.activate(getTreeItem());
        });
        setOnDragDetected(e -> {
            if (isEmpty() || !tree.startDrag(tree.original(getTreeItem()))) return;
            var board = startDragAndDrop(TransferMode.MOVE);
            var data = new ClipboardContent();
            data.putString(tree.label(tree.original(getTreeItem())));
            board.setContent(data);
            tree.notifyDrag(EleFXTree.DragPhase.START, null, null);
            e.consume();
        });
        setOnDragEntered(e -> {
            if (!isEmpty()) tree.notifyDrag(EleFXTree.DragPhase.ENTER, tree.original(getTreeItem()), null);
        });
        setOnDragOver(e -> {
            var drop = target(e);
            if (!isEmpty()) tree.notifyDrag(EleFXTree.DragPhase.OVER, tree.original(getTreeItem()),
                    drop == null ? null : drop.position());
            indicate(drop);
            if (drop != null) {
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            }
        });
        setOnDragExited(e -> {
            indicate(null);
            if (!isEmpty()) tree.notifyDrag(EleFXTree.DragPhase.LEAVE, tree.original(getTreeItem()), null);
        });
        setOnDragDone(e -> {
            indicate(null);
            tree.endDrag(null);
        });
        setOnDragDropped(e -> {
            var drop = target(e);
            if (drop != null) tree.move(drop);
            e.setDropCompleted(drop != null);
            indicate(null);
            tree.endDrag(drop);
            e.consume();
        });
    }

    private void consumeDisabledCheckboxEvent(MouseEvent event) {
        if (box.isDisabled()) event.consume();
    }

    private EleFXTree.Drop<T> target(DragEvent event) {
        if (isEmpty()) return null;
        double y = event.getY() / getHeight();
        var position = y < 0.25
                ? EleFXTree.DropPosition.BEFORE
                : y > 0.75 ? EleFXTree.DropPosition.AFTER : EleFXTree.DropPosition.INSIDE;
        return tree.dropTarget(tree.original(getTreeItem()), position);
    }

    private void updateExpansion() {
        var item = getTreeItem();
        double progress = tree.caretProgress(item);
        double height = tree.revealedHeight(item);
        setPrefHeight(height);
        setClip(height < tree.rowHeight() ? revealClip : null);
        revealClip.setHeight(height);
        Node caret = progress < 1 && item != null && getDisclosureNode() != null
                ? getDisclosureNode().lookup(".arrow")
                : null;
        if (animatedCaret != caret) {
            if (animatedCaret != null) animatedCaret.getTransforms().remove(caretOffset);
            animatedCaret = caret;
            if (caret != null) caret.getTransforms().add(caretOffset);
        }
        if (caret != null) {
            caretOffset.setPivotX(caret.getLayoutBounds().getCenterX());
            caretOffset.setPivotY(caret.getLayoutBounds().getCenterY());
            caretOffset.setAngle(-90 * (1 - progress));
        }
    }

    private void indicate(EleFXTree.Drop<T> drop) {
        for (var pos : EleFXTree.DropPosition.values()) {
            var state = PseudoClass.getPseudoClass("drop-" + pos.name().toLowerCase(java.util.Locale.ROOT));
            pseudoClassStateChanged(state, drop != null && drop.position() == pos);
        }
    }

    private void observeLoading(TreeItem<T> item) {
        var loading = tree.loadingProperty(item);
        if (itemLoading != loading) {
            if (itemLoading != null) itemLoading.removeListener(weakLoadingChanged);
            itemLoading = loading;
            if (itemLoading != null) itemLoading.addListener(weakLoadingChanged);
        }
        updateLoading();
    }

    private void updateLoading() {
        boolean loading = itemLoading != null && itemLoading.get();
        pseudoClassStateChanged(LOADING, loading);
        loadingIcon.setVisible(loading);
        loadingIcon.setLoading(loading && getScene() != null);
    }

    @Override
    protected void updateItem(T value, boolean empty) {
        super.updateItem(value, empty);
        content.getChildren().clear();
        setText(null);
        setGraphic(null);
        var item = empty || getTreeItem() == null ? null : tree.original(getTreeItem());
        observeLoading(item);
        if (item == null) return;
        String text = tree.label(item);
        setAccessibleText(text);
        if (tree.isShowCheckbox()) {
            box.setAccessibleText(text);
            box.setSelected(tree.isChecked(item));
            box.setIndeterminate(tree.isHalfChecked(item));
            boolean disabled = tree.isDisabled(item);
            box.setDisable(disabled);
            checkboxArea.setCursor(disabled ? DisabledCheckboxCursor.INSTANCE : null);
            content.getChildren().add(checkboxArea);
        }
        Node graphic = tree.graphic(item);
        if (graphic == null) {
            label.setText(text);
            label.setStyle(tree.nodeLabelStyle(item.getValue()));
            graphic = label;
        }
        content.getChildren().addAll(loadingIcon, graphic);
        setGraphic(content);
    }
}
