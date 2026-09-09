package com.sjydvlp.elefx.component.container;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Element Plus inspired page container.
 *
 * <p>
 * Containers arrange their direct children horizontally or vertically. In the default
 * {@link EleFXContainerDirection#AUTO} mode, a direct {@link EleFXHeader} or
 * {@link EleFXFooter} selects a vertical layout, matching Element Plus. Within either
 * direction, {@link EleFXMain} and nested containers receive the remaining space on the primary axis.
 * </p>
 */
public class EleFXContainer extends Pane implements Themable {

    private static final String STYLE_CLASS = "ele-container";

    private final ObjectProperty<EleFXContainerDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXContainerDirection.AUTO);

    public EleFXContainer() {
        initialize();
    }

    public EleFXContainer(Node... children) {
        this();
        getChildren().addAll(children);
    }

    public EleFXContainerDirection getDirection() {
        return direction.get();
    }

    public ObjectProperty<EleFXContainerDirection> directionProperty() {
        return direction;
    }

    public void setDirection(EleFXContainerDirection direction) {
        this.direction.set(direction == null ? EleFXContainerDirection.AUTO : direction);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CONTAINER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    @Override
    public Orientation getContentBias() {
        return isVertical() ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }

    @Override
    protected double computeMinWidth(double height) {
        return insetsWidth() + primarySize(true, true);
    }

    @Override
    protected double computeMinHeight(double width) {
        return insetsHeight() + primarySize(false, true);
    }

    @Override
    protected double computePrefWidth(double height) {
        return insetsWidth() + primarySize(true, false);
    }

    @Override
    protected double computePrefHeight(double width) {
        return insetsHeight() + primarySize(false, false);
    }

    @Override
    protected void layoutChildren() {
        boolean vertical = isVertical();
        double x = snappedLeftInset();
        double y = snappedTopInset();
        double width = Math.max(0, getWidth() - insetsWidth());
        double height = Math.max(0, getHeight() - insetsHeight());
        double availablePrimary = vertical ? height : width;
        int mainCount = (int) getManagedChildren().stream().filter(EleFXContainer::isFlexible).count();
        double fixedPrimary = getManagedChildren().stream()
                .filter(node -> !isFlexible(node))
                .mapToDouble(node -> preferredPrimary(node, vertical))
                .sum();
        double mainPrimary = mainCount == 0 ? 0 : Math.max(0, availablePrimary - fixedPrimary) / mainCount;

        for (Node child : getManagedChildren()) {
            double primary = isFlexible(child) ? mainPrimary : preferredPrimary(child, vertical);
            if (vertical) {
                child.resizeRelocate(x, y, width, primary);
                y += primary;
            } else {
                child.resizeRelocate(x, y, primary, height);
                x += primary;
            }
        }
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Element Plus containers use flex: 1. Store the equivalent parent constraints up front;
        // JavaFX applies them when this node is subsequently added to an HBox or VBox.
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        direction.addListener(observable -> requestLayout());
        getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> requestLayout());
        sceneBuilderIntegration();
    }

    private boolean isVertical() {
        return switch (getDirection() == null ? EleFXContainerDirection.AUTO : getDirection()) {
            case VERTICAL -> true;
            case HORIZONTAL -> false;
            case AUTO -> getManagedChildren().stream()
                    .anyMatch(node -> node instanceof EleFXHeader || node instanceof EleFXFooter);
        };
    }

    private double primarySize(boolean width, boolean minimum) {
        boolean crossAxis = width == isVertical();
        if (crossAxis) {
            return getManagedChildren().stream()
                    .mapToDouble(node -> size(node, width, minimum))
                    .max().orElse(0);
        }
        return getManagedChildren().stream()
                .mapToDouble(node -> size(node, width, minimum))
                .sum();
    }

    private static double size(Node node, boolean width, boolean minimum) {
        if (width) {
            return minimum ? node.minWidth(-1) : node.prefWidth(-1);
        }
        return minimum ? node.minHeight(-1) : node.prefHeight(-1);
    }

    private static double preferredPrimary(Node node, boolean vertical) {
        return Math.max(0, vertical ? node.prefHeight(-1) : node.prefWidth(-1));
    }

    private static boolean isFlexible(Node node) {
        return node instanceof EleFXMain || node instanceof EleFXContainer;
    }

    private double insetsWidth() {
        return snappedLeftInset() + snappedRightInset();
    }

    private double insetsHeight() {
        return snappedTopInset() + snappedBottomInset();
    }
}
