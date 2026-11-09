package com.sjydvlp.elefx.internal;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 * Internal host for background hover content. Painting in the owner's Scene
 * preserves native window occlusion without activating or raising the owner.
 */
public final class ScenePopupOverlay {

    private final Scene scene;

    private final Node content;

    private final Pane layer = new Pane();

    private final Parent host;

    private final boolean managed;

    public ScenePopupOverlay(Scene scene, Node content) {
        this.scene = scene;
        this.content = content;
        this.managed = content.isManaged();
        Parent root = scene.getRoot();
        if (!(root instanceof Pane) && !(root instanceof Group)) {
            RootHost wrapper = new RootHost(root);
            scene.setRoot(wrapper);
            wrapper.getChildren().add(root);
            root = wrapper;
        }
        host = root;
        layer.setManaged(false);
        layer.setPickOnBounds(false);
        layer.setViewOrder(-Double.MAX_VALUE);
        content.setManaged(false);
        layer.getChildren().add(content);
        if (host instanceof Pane pane)
            pane.getChildren().add(layer);
        else
            ((Group) host).getChildren().add(layer);
    }

    public boolean isShowing() {
        return layer.getScene() == scene;
    }

    public void moveTo(double screenX, double screenY) {
        Point2D point = layer.screenToLocal(screenX, screenY);
        if (point != null) content.relocate(point.getX(), point.getY());
    }

    public void hide() {
        layer.getChildren().remove(content);
        content.setManaged(managed);
        if (host instanceof Pane pane)
            pane.getChildren().remove(layer);
        else
            ((Group) host).getChildren().remove(layer);
        // Multiple popups can share a wrapper around a non-Pane scene root.
        // The final popup restores the original root, regardless of hide order.
        if (host instanceof RootHost wrapper && scene.getRoot() == wrapper
                && wrapper.getChildren().size() == 1) {
            wrapper.getChildren().remove(wrapper.original);
            scene.setRoot(wrapper.original);
        }
    }

    private static final class RootHost extends Pane {

        private final Parent original;

        private RootHost(Parent original) {
            this.original = original;
        }

        @Override
        protected void layoutChildren() {
            original.resizeRelocate(0, 0, getWidth(), getHeight());
        }
    }
}
