package com.sjydvlp.elefx.component.divider;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A visual separator modelled after Element Plus {@code el-divider}.
 *
 * <p>
 * Horizontal dividers optionally display text or an arbitrary JavaFX node. The
 * node is the JavaFX equivalent of Element Plus' default slot. Vertical dividers
 * intentionally display only a line, matching Element Plus.
 * </p>
 */
public class EleFXDivider extends Pane implements Themable {

    private static final String STYLE_CLASS = "ele-divider";

    private static final double CONTENT_GAP = 20;

    private final ObjectProperty<EleFXDividerDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXDividerDirection.HORIZONTAL);

    private final ObjectProperty<EleFXDividerContentPosition> contentPosition = new SimpleObjectProperty<>(this,
            "contentPosition", EleFXDividerContentPosition.CENTER);

    private final ObjectProperty<EleFXDividerBorderStyle> borderStyle = new SimpleObjectProperty<>(this,
            "borderStyle", EleFXDividerBorderStyle.SOLID);

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final Region firstLine = new Region();

    private final Region secondLine = new Region();

    private final StackPane contentContainer = new StackPane();

    private final Label textContent = new Label();

    private String directionClass;

    private String positionClass;

    private String borderStyleClass;

    public EleFXDivider() {
        initialize();
    }

    public EleFXDivider(String text) {
        this();
        setText(text);
    }

    public EleFXDivider(Node content) {
        this();
        setContent(content);
    }

    public EleFXDividerDirection getDirection() {
        return direction.get();
    }

    public ObjectProperty<EleFXDividerDirection> directionProperty() {
        return direction;
    }

    public void setDirection(EleFXDividerDirection value) {
        direction.set(value == null ? EleFXDividerDirection.HORIZONTAL : value);
    }

    public EleFXDividerContentPosition getContentPosition() {
        return contentPosition.get();
    }

    public ObjectProperty<EleFXDividerContentPosition> contentPositionProperty() {
        return contentPosition;
    }

    public void setContentPosition(EleFXDividerContentPosition value) {
        contentPosition.set(value == null ? EleFXDividerContentPosition.CENTER : value);
    }

    public EleFXDividerBorderStyle getBorderStyle() {
        return borderStyle.get();
    }

    public ObjectProperty<EleFXDividerBorderStyle> borderStyleProperty() {
        return borderStyle;
    }

    public void setBorderStyle(EleFXDividerBorderStyle value) {
        borderStyle.set(value == null ? EleFXDividerBorderStyle.SOLID : value);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    /** Returns the optional node rendered in the horizontal divider's content area. */
    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node value) {
        if (value != null && value.getParent() != null && !contentContainer.getChildren().contains(value))
            throw new IllegalArgumentException("divider content must not already have a parent");
        content.set(value);
    }

    public Node getContentNode() {
        return getContent();
    }

    public void setContentNode(Node value) {
        setContent(value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.DIVIDER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + snappedRightInset()
                + (getDirection() == EleFXDividerDirection.VERTICAL ? lineThickness() : 0);
    }

    @Override
    protected double computePrefWidth(double height) {
        if (getDirection() == EleFXDividerDirection.VERTICAL)
            return snappedLeftInset() + snappedRightInset() + lineThickness();
        return snappedLeftInset() + snappedRightInset()
                + (hasContent() ? contentContainer.prefWidth(-1) + CONTENT_GAP * 2 : 0);
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeHeight(width, true);
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeHeight(width, false);
    }

    @Override
    protected void layoutChildren() {
        double x = snappedLeftInset(), y = snappedTopInset();
        double width = Math.max(0, getWidth() - x - snappedRightInset());
        double height = Math.max(0, getHeight() - y - snappedBottomInset());
        if (getDirection() == EleFXDividerDirection.VERTICAL) {
            firstLine.resizeRelocate(x, y, width, height);
            secondLine.resizeRelocate(0, 0, 0, 0);
            contentContainer.resizeRelocate(0, 0, 0, 0);
            return;
        }
        double thickness = lineThickness();
        double lineY = y + Math.max(0, (height - thickness) / 2);
        if (!hasContent()) {
            firstLine.resizeRelocate(x, lineY, width, thickness);
            secondLine.resizeRelocate(0, 0, 0, 0);
            contentContainer.resizeRelocate(0, 0, 0, 0);
            return;
        }
        // Text padding belongs to the container; do not add it again to the lines.
        double contentWidth = Math.min(contentContainer.prefWidth(-1), width);
        double contentHeight = contentContainer.prefHeight(contentWidth);
        double offset = Math.min(CONTENT_GAP, Math.max(0, (width - contentWidth) / 2));
        double contentX = x + switch (getContentPosition()) {
            case LEFT -> offset;
            case RIGHT -> width - contentWidth - offset;
            case CENTER -> (width - contentWidth) / 2;
        };
        firstLine.resizeRelocate(x, lineY, Math.max(0, contentX - x), thickness);
        contentContainer.resizeRelocate(contentX, lineY + thickness / 2 - contentHeight / 2,
                contentWidth, contentHeight);
        secondLine.resizeRelocate(contentX + contentWidth, lineY,
                Math.max(0, x + width - contentX - contentWidth), thickness);
    }

    private double computeHeight(double width, boolean minimum) {
        // Horizontal content overlays the line instead of increasing its height.
        double contentHeight = getDirection() == EleFXDividerDirection.VERTICAL ? 14 : lineThickness();
        return snappedTopInset() + snappedBottomInset() + Math.max(lineThickness(), contentHeight);
    }

    private double lineThickness() {
        // Element Plus keeps border-width at 1px even for double. At this width,
        // browsers render it as a single line rather than expanding it to 3px.
        return 1;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        firstLine.getStyleClass().add("ele-divider__line");
        secondLine.getStyleClass().add("ele-divider__line");
        contentContainer.getStyleClass().add("ele-divider__text");
        textContent.getStyleClass().add("ele-divider__text-label");
        textContent.textProperty().bind(text);
        getChildren().addAll(firstLine, secondLine, contentContainer);
        direction.addListener((o, oldValue, newValue) -> {
            refreshStyles();
            refreshContent();
        });
        contentPosition.addListener((o, oldValue, newValue) -> refreshStyles());
        borderStyle.addListener((o, oldValue, newValue) -> refreshStyles());
        text.addListener(o -> refreshContent());
        content.addListener((o, oldValue, newValue) -> refreshContent());
        refreshStyles();
        refreshContent();
        sceneBuilderIntegration();
    }

    private void refreshStyles() {
        replaceStyleClass(directionClass, directionClass = getDirection().styleClass());
        replaceStyleClass(positionClass, positionClass = getContentPosition().styleClass());
        replaceStyleClass(borderStyleClass, borderStyleClass = getBorderStyle().styleClass());
        requestLayout();
    }

    private void replaceStyleClass(String oldClass, String newClass) {
        if (oldClass != null) getStyleClass().remove(oldClass);
        if (!getStyleClass().contains(newClass)) getStyleClass().add(newClass);
    }

    private void refreshContent() {
        contentContainer.getChildren().clear();
        Node node = getContent();
        if (node != null)
            contentContainer.getChildren().add(node);
        else if (!getText().isEmpty()) contentContainer.getChildren().add(textContent);
        boolean visible = getDirection() == EleFXDividerDirection.HORIZONTAL && (node != null || !getText().isEmpty());
        contentContainer.setVisible(visible);
        contentContainer.setManaged(visible);
        requestLayout();
    }

    private boolean hasContent() {
        return getDirection() == EleFXDividerDirection.HORIZONTAL && !contentContainer.getChildren().isEmpty();
    }
}
