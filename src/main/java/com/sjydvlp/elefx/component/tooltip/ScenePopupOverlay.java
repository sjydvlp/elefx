package com.sjydvlp.elefx.component.tooltip;

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
final class ScenePopupOverlay {

    // Shared by tooltip and popover layers, including when their hide order differs.
    private static final String RESTORE_ROOT = "com.sjydvlp.elefx.sceneOverlay.restoreRoot";

    private final Scene scene;

    private final Node content;

    private final Pane layer = new Pane();

    private final Parent host;

    private final boolean managed;

    ScenePopupOverlay(Scene scene, Node content) {
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
        if (host.hasProperties() && host.getProperties().get(RESTORE_ROOT)instanceof Runnable restore) {
            restore.run();
        }
    }

    private static final class RootHost extends Pane {

        private final Parent original;

        private RootHost(Parent original) {
            this.original = original;
            getProperties().put(RESTORE_ROOT, (Runnable) () -> {
                Scene owner = getScene();
                if (owner != null && owner.getRoot() == this && getChildren().size() == 1
                        && getChildren().get(0) == original) {
                    getChildren().remove(original);
                    owner.setRoot(original);
                }
            });
        }

        @Override
        protected void layoutChildren() {
            original.resizeRelocate(0, 0, getWidth(), getHeight());
        }
    }
}
