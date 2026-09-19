package com.sjydvlp.elefx.component.timeline;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * One activity in an {@link EleFXTimeline}. Text properties are convenience
 * defaults; {@link #contentNodeProperty()} and {@link #iconProperty()} let
 * applications supply arbitrary JavaFX content, matching Element Plus slots.
 */
public class EleFXTimelineItem extends HBox {

    private final StringProperty timestamp = new SimpleStringProperty(this, "timestamp", "");

    private final StringProperty content = new SimpleStringProperty(this, "content", "");

    private final BooleanProperty hideTimestamp = new SimpleBooleanProperty(this, "hideTimestamp", false);

    private final BooleanProperty center = new SimpleBooleanProperty(this, "center", false);

    private final BooleanProperty hollow = new SimpleBooleanProperty(this, "hollow", false);

    private final StringProperty color = new SimpleStringProperty(this, "color", "");

    private final ObjectProperty<EleFXTimelineItemType> type = new SimpleObjectProperty<>(this, "type");

    private final ObjectProperty<EleFXTimelineItemSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXTimelineItemSize.NORMAL);

    private final ObjectProperty<EleFXTimelineItemPlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXTimelineItemPlacement.BOTTOM);

    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>(this, "contentNode");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final StackPane rail = new StackPane();

    private final Region tail = new Region();

    private final StackPane dot = new StackPane();

    private final Region defaultDot = new Region();

    private final VBox body = new VBox();

    private final StackPane bodyColumn = new StackPane();

    private final StackPane spacer = new StackPane();

    private final Label timestampLabel = new Label();

    private final Label contentLabel = new Label();

    public EleFXTimelineItem() {
        initialize();
    }

    public EleFXTimelineItem(String content) {
        this();
        setContent(content);
    }

    public EleFXTimelineItem(String content, String timestamp) {
        this(content);
        setTimestamp(timestamp);
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }

    public void setTimestamp(String value) {
        timestamp.set(value == null ? "" : value);
    }

    public String getContent() {
        return content.get();
    }

    public StringProperty contentProperty() {
        return content;
    }

    public void setContent(String value) {
        content.set(value == null ? "" : value);
    }

    public boolean isHideTimestamp() {
        return hideTimestamp.get();
    }

    public BooleanProperty hideTimestampProperty() {
        return hideTimestamp;
    }

    public void setHideTimestamp(boolean value) {
        hideTimestamp.set(value);
    }

    public boolean isCenter() {
        return center.get();
    }

    public BooleanProperty centerProperty() {
        return center;
    }

    public void setCenter(boolean value) {
        center.set(value);
    }

    public boolean isHollow() {
        return hollow.get();
    }

    public BooleanProperty hollowProperty() {
        return hollow;
    }

    public void setHollow(boolean value) {
        hollow.set(value);
    }

    public String getColor() {
        return color.get();
    }

    public StringProperty colorProperty() {
        return color;
    }

    public void setColor(String value) {
        color.set(value == null ? "" : value.trim());
    }

    public EleFXTimelineItemType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXTimelineItemType> typeProperty() {
        return type;
    }

    public void setType(EleFXTimelineItemType value) {
        type.set(value);
    }

    public EleFXTimelineItemSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXTimelineItemSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXTimelineItemSize value) {
        size.set(value == null ? EleFXTimelineItemSize.NORMAL : value);
    }

    public EleFXTimelineItemPlacement getPlacement() {
        return placement.get();
    }

    public ObjectProperty<EleFXTimelineItemPlacement> placementProperty() {
        return placement;
    }

    public void setPlacement(EleFXTimelineItemPlacement value) {
        placement.set(value == null ? EleFXTimelineItemPlacement.BOTTOM : value);
    }

    public Node getContentNode() {
        return contentNode.get();
    }

    public ObjectProperty<Node> contentNodeProperty() {
        return contentNode;
    }

    public void setContentNode(Node value) {
        contentNode.set(value);
    }

    /** Custom icon content, corresponding to Element Plus's {@code icon} property. */
    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    void configure(boolean end, boolean last, boolean alternating) {
        getStyleClass().removeIf(
                value -> value.startsWith("ele-timeline-item--side-") || value.equals("ele-timeline-item--last"));
        getStyleClass().add("ele-timeline-item--side-" + (end ? "end" : "start"));
        if (last) getStyleClass().add("ele-timeline-item--last");
        if (alternating) {
            bodyColumn.setMinWidth(0);
            bodyColumn.setPrefWidth(0);
            spacer.setMinWidth(0);
            spacer.setPrefWidth(0);
            getChildren().setAll(end ? new Node[] {spacer, rail, bodyColumn} : new Node[] {bodyColumn, rail, spacer});
            HBox.setHgrow(bodyColumn, Priority.ALWAYS);
            HBox.setHgrow(spacer, Priority.ALWAYS);
        } else {
            bodyColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
            getChildren().setAll(end ? new Node[] {bodyColumn, rail} : new Node[] {rail, bodyColumn});
            HBox.setHgrow(bodyColumn, Priority.ALWAYS);
            HBox.setHgrow(spacer, Priority.NEVER);
        }
        boolean alignRight = alternating ? !end : end;
        body.setAlignment(alignRight ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        timestampLabel.setAlignment(alignRight ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        contentLabel.setAlignment(alignRight ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    }

    double nodeCenterY() {
        Bounds bounds = dot.getBoundsInParent();
        return rail.getLayoutY() + bounds.getMinY() + bounds.getHeight() / 2;
    }

    /** Centres the node on the visible timestamp/content area, excluding item spacing. */
    void alignCenteredNode() {
        if (!isCenter() || body.getChildren().isEmpty()) return;
        double top = Double.POSITIVE_INFINITY;
        double bottom = Double.NEGATIVE_INFINITY;
        for (Node child : body.getChildren()) {
            Bounds bounds = child.getBoundsInParent();
            top = Math.min(top, bounds.getMinY());
            bottom = Math.max(bottom, bounds.getMaxY());
        }
        if (!Double.isFinite(top) || !Double.isFinite(bottom)) return;
        double nodeHeight = dot.getLayoutBounds().getHeight();
        double contentCenter = bodyColumn.getLayoutY() + body.getLayoutY() + (top + bottom) / 2;
        StackPane.setAlignment(dot, Pos.TOP_CENTER);
        dot.setTranslateY(contentCenter - rail.getLayoutY() - nodeHeight / 2);
    }

    /** Scales the tail visually without changing this item's layout size. */
    void connectTailTo(double startY, double endY) {
        double tailHeight = tail.getHeight();
        if (tailHeight <= 0) return;
        double railTop = rail.getLayoutY();
        double start = startY - railTop;
        double end = endY - railTop;
        tail.setScaleY(Math.max(0, (end - start) / tailHeight));
        // Scale is around the Region's centre, so this translation pins both ends.
        tail.setTranslateY((start + end - tailHeight) / 2);
    }

    private void initialize() {
        getStyleClass().add("ele-timeline-item");
        // The rail must receive the item's full height; otherwise StackPane lays its
        // tail out at its computed (zero) height and the connecting line disappears.
        setFillHeight(true);
        setMaxWidth(Double.MAX_VALUE);
        rail.getStyleClass().add("ele-timeline-item__rail");
        rail.setMaxHeight(Double.MAX_VALUE);
        tail.getStyleClass().add("ele-timeline-item__tail");
        tail.setMaxHeight(Double.MAX_VALUE);
        dot.getStyleClass().add("ele-timeline-item__node");
        defaultDot.getStyleClass().add("ele-timeline-item__default-dot");
        rail.getChildren().addAll(tail, dot);
        StackPane.setAlignment(tail, Pos.TOP_CENTER);
        StackPane.setAlignment(dot, Pos.TOP_CENTER);
        body.getStyleClass().add("ele-timeline-item__body");
        body.setMaxWidth(Double.MAX_VALUE);
        bodyColumn.getChildren().add(body);
        bodyColumn.setMaxWidth(Double.MAX_VALUE);
        spacer.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(body, Pos.TOP_LEFT);
        timestampLabel.getStyleClass().add("ele-timeline-item__timestamp");
        contentLabel.getStyleClass().add("ele-timeline-item__content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        InvalidationListener refresh = ignored -> refresh();
        timestamp.addListener(refresh);
        content.addListener(refresh);
        hideTimestamp.addListener(refresh);
        center.addListener(refresh);
        hollow.addListener(refresh);
        color.addListener(refresh);
        type.addListener(refresh);
        size.addListener(refresh);
        placement.addListener(refresh);
        contentNode.addListener(refresh);
        icon.addListener(refresh);
        refresh();
    }

    private void refresh() {
        timestampLabel.setText(getTimestamp());
        contentLabel.setText(getContent());
        Node actualContent = getContentNode() == null ? contentLabel : getContentNode();
        boolean showTimestamp = !isHideTimestamp() && !getTimestamp().isBlank();
        timestampLabel.setManaged(showTimestamp);
        timestampLabel.setVisible(showTimestamp);
        body.getChildren().setAll(getPlacement() == EleFXTimelineItemPlacement.TOP && showTimestamp
                ? new Node[] {timestampLabel, actualContent}
                : showTimestamp ? new Node[] {actualContent, timestampLabel} : new Node[] {actualContent});
        dot.getChildren().setAll(getIcon() == null ? defaultDot : getIcon());
        getStyleClass().removeIf(value -> value.startsWith("ele-timeline-item--type-")
                || value.startsWith("ele-timeline-item--size-") || value.equals("ele-timeline-item--hollow")
                || value.equals("ele-timeline-item--center") || value.equals("ele-timeline-item--custom-color"));
        getStyleClass().add("ele-timeline-item--size-" + getSize().name().toLowerCase());
        if (getType() != null) getStyleClass().add("ele-timeline-item--type-" + getType().name().toLowerCase());
        if (isHollow()) getStyleClass().add("ele-timeline-item--hollow");
        if (isCenter()) getStyleClass().add("ele-timeline-item--center");
        EleFXTimelineItemType effectiveType = getType() == null ? EleFXTimelineItemType.INFO : getType();
        String nodeColor = !getColor().isBlank() ? getColor() : switch (effectiveType) {
            case PRIMARY -> "-elefx-cp-1";
            case SUCCESS -> "-elefx-cs-1";
            case WARNING -> "-elefx-cw-1";
            case DANGER -> "-elefx-cd-1";
            case INFO -> "-elefx-ci-1";
        };
        double nodeBoxSize = getSize() == EleFXTimelineItemSize.LARGE ? 18 : 14;
        double dotSize = getSize() == EleFXTimelineItemSize.LARGE ? 16 : 12;
        if (getIcon() == null) {
            dot.setMinSize(nodeBoxSize, nodeBoxSize);
            dot.setPrefSize(nodeBoxSize, nodeBoxSize);
            dot.setMaxSize(nodeBoxSize, nodeBoxSize);
            defaultDot.setMinSize(dotSize, dotSize);
            defaultDot.setPrefSize(dotSize, dotSize);
            defaultDot.setMaxSize(dotSize, dotSize);
            dot.setTranslateY(2);
            tail.setTranslateY(9);
            dot.setStyle("");
            defaultDot.setStyle("-fx-background-color: " + (isHollow() ? "-elefx-co-white-bg" : nodeColor)
                    + "; -fx-border-color: " + nodeColor + ";");
        } else {
            double customHeight = getIcon()instanceof Region region
                    ? region.prefHeight(-1)
                    : getIcon().getLayoutBounds().getHeight();
            double customWidth = getIcon()instanceof Region region
                    ? region.prefWidth(-1)
                    : getIcon().getLayoutBounds().getWidth();
            customHeight = customHeight > 0 ? customHeight : nodeBoxSize;
            customWidth = customWidth > 0 ? customWidth : nodeBoxSize;
            double nodeWidth = Math.max(nodeBoxSize, customWidth);
            double nodeHeight = Math.max(nodeBoxSize, customHeight);
            dot.setMinSize(nodeWidth, nodeHeight);
            dot.setPrefSize(nodeWidth, nodeHeight);
            dot.setMaxSize(nodeWidth, nodeHeight);
            dot.setTranslateY(0);
            tail.setTranslateY(nodeHeight / 2);
            dot.setStyle("-fx-background-color: -elefx-co-white-bg; -fx-background-radius: 50%;");
            defaultDot.setStyle("");
        }
        if (!getColor().isBlank()) {
            getStyleClass().add("ele-timeline-item--custom-color");
        }
        if (!isCenter()) {
            StackPane.setAlignment(dot, Pos.TOP_CENTER);
        }
        requestLayout();
    }
}
