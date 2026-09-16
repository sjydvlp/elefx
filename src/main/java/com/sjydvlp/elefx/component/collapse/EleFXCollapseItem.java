package com.sjydvlp.elefx.component.collapse;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** A titled, expandable section belonging to an {@link EleFXCollapse}. */
public class EleFXCollapseItem extends VBox {

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final EleFXIcon defaultExpandIcon = new EleFXIcon(EleFXIconType.ARROW_RIGHT, 16);

    private final EleFXIcon defaultCollapseIcon = new EleFXIcon(EleFXIconType.ARROW_DOWN, 16);

    private final ObjectProperty<Node> expandIcon = new SimpleObjectProperty<>(this, "expandIcon", defaultExpandIcon);

    private final ObjectProperty<Node> collapseIcon = new SimpleObjectProperty<>(this, "collapseIcon",
            defaultCollapseIcon);

    private final ReadOnlyBooleanWrapper expanded = new ReadOnlyBooleanWrapper(this, "expanded", false);

    private final HBox header = new HBox();

    private final HBox titleBox = new HBox();

    private final VBox content = new VBox();

    private final Label titleLabel = new Label();

    private EleFXCollapseIconPosition iconPosition = EleFXCollapseIconPosition.RIGHT;

    private boolean expandIconSetLocally;

    private boolean collapseIconSetLocally;

    private boolean applyingCollapseIcons;

    private boolean collapsing;

    private Duration animationDuration = Duration.millis(200);

    private RotateTransition iconTransition;

    private Timeline contentTransition;

    private final Rectangle contentClip = new Rectangle();

    public EleFXCollapseItem() {
        initialize();
    }

    public EleFXCollapseItem(String title, Node... content) {
        this();
        setTitle(title);
        getContent().getChildren().addAll(content);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String value) {
        name.set(value == null ? "" : value);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    /** Optional custom header node, used instead of {@link #getTitle()}. */
    public Node getTitleNode() {
        return titleNode.get();
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    /**
     * Legacy single icon used for both states. It rotates between collapsed and expanded states.
     * Use {@link #setExpandIcon(Node)} and {@link #setCollapseIcon(Node)} to customize both states separately.
     */
    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    /** Icon shown while this item is collapsed (the action available is expand). */
    public Node getExpandIcon() {
        return expandIcon.get();
    }

    public ObjectProperty<Node> expandIconProperty() {
        return expandIcon;
    }

    public void setExpandIcon(Node value) {
        expandIconSetLocally = true;
        expandIcon.set(value == null ? defaultExpandIcon : value);
    }

    /** Icon shown while this item is expanded (the action available is collapse). */
    public Node getCollapseIcon() {
        return collapseIcon.get();
    }

    public ObjectProperty<Node> collapseIconProperty() {
        return collapseIcon;
    }

    public void setCollapseIcon(Node value) {
        collapseIconSetLocally = true;
        collapseIcon.set(value == null ? defaultCollapseIcon : value);
    }

    public boolean isExpanded() {
        return expanded.get();
    }

    public ReadOnlyBooleanProperty expandedProperty() {
        return expanded.getReadOnlyProperty();
    }

    /** Container holding this item's default content. */
    public VBox getContent() {
        return content;
    }

    public HBox getHeader() {
        return header;
    }

    void setExpandedFromCollapse(boolean value) {
        if (isExpanded() == value) return;
        collapsing = !value;
        expanded.set(value);
        refreshIcon();
        animateContent(value);
        animateIcon(value);
        updateStateClasses();
    }

    void setAnimationDurationFromCollapse(Duration value) {
        animationDuration = value == null ? Duration.millis(200) : value;
    }

    void setIconsFromCollapse(Node expand, Node collapse) {
        applyingCollapseIcons = true;
        try {
            if (!expandIconSetLocally) expandIcon.set(expand == null ? defaultExpandIcon : expand);
            if (!collapseIconSetLocally) collapseIcon.set(collapse == null ? defaultCollapseIcon : collapse);
        } finally {
            applyingCollapseIcons = false;
        }
    }

    void setIconPosition(EleFXCollapseIconPosition position) {
        iconPosition = position == null ? EleFXCollapseIconPosition.RIGHT : position;
        Node displayIcon = displayIcon();
        header.getChildren().remove(displayIcon);
        if (iconPosition == EleFXCollapseIconPosition.LEFT)
            header.getChildren().add(0, displayIcon);
        else
            header.getChildren().add(displayIcon);
    }

    private void initialize() {
        getStyleClass().add("ele-collapse-item");
        setFillWidth(true);
        setSpacing(0);
        header.getStyleClass().add("ele-collapse-item__header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setFocusTraversable(true);
        titleBox.getStyleClass().add("ele-collapse-item__title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleLabel.getStyleClass().add("ele-collapse-item__title-text");
        content.getStyleClass().add("ele-collapse-item__wrap");
        content.setFillWidth(true);
        content.setVisible(false);
        content.setManaged(false);
        content.setClip(contentClip);
        content.layoutBoundsProperty().addListener((observable, oldBounds, bounds) -> {
            contentClip.setWidth(bounds.getWidth());
            contentClip.setHeight(bounds.getHeight());
        });
        header.getChildren().addAll(titleBox, displayIcon());
        getChildren().addAll(header, content);
        title.addListener(observable -> refreshTitle());
        titleNode.addListener(observable -> refreshTitle());
        icon.addListener(observable -> refreshIcon());
        expandIcon.addListener(observable -> {
            if (!applyingCollapseIcons) expandIconSetLocally = true;
            refreshIcon();
        });
        collapseIcon.addListener(observable -> {
            if (!applyingCollapseIcons) collapseIconSetLocally = true;
            refreshIcon();
        });
        disableProperty().addListener(observable -> updateStateClasses());
        header.setOnMouseClicked(event -> toggle());
        header.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) {
                toggle();
                event.consume();
            }
        });
        refreshTitle();
        refreshIcon();
        setExpandedFromCollapse(false);
    }

    private void toggle() {
        if (getParent()instanceof EleFXCollapse collapse) collapse.requestToggle(this);
    }

    private void refreshTitle() {
        titleBox.getChildren().setAll(getTitleNode() == null ? titleLabel : getTitleNode());
        titleLabel.setText(getTitle());
    }

    private void refreshIcon() {
        Node displayIcon = displayIcon();
        if (!displayIcon.getStyleClass().contains("ele-collapse-item__arrow"))
            displayIcon.getStyleClass().add("ele-collapse-item__arrow");
        header.getChildren().removeIf(node -> node != titleBox);
        if (iconPosition == EleFXCollapseIconPosition.LEFT)
            header.getChildren().add(0, displayIcon);
        else
            header.getChildren().add(displayIcon);
    }

    private void animateIcon(boolean expanded) {
        Node displayIcon = displayIcon();
        if (getIcon() == null) {
            displayIcon.setRotate(0);
            return;
        }
        double targetAngle = expanded ? 90 : 0;
        if (displayIcon.getScene() == null || animationDuration.lessThanOrEqualTo(Duration.ZERO)) {
            displayIcon.setRotate(targetAngle);
            return;
        }
        if (iconTransition != null) iconTransition.stop();
        iconTransition = new RotateTransition(animationDuration, displayIcon);
        iconTransition.setFromAngle(displayIcon.getRotate());
        iconTransition.setToAngle(targetAngle);
        iconTransition.setInterpolator(Interpolator.EASE_BOTH);
        iconTransition.play();
    }

    private Node displayIcon() {
        if (getIcon() != null) return getIcon();
        if (isExpanded()) return getCollapseIcon() == null ? defaultCollapseIcon : getCollapseIcon();
        return getExpandIcon() == null ? defaultExpandIcon : getExpandIcon();
    }

    private void animateContent(boolean expand) {
        if (contentTransition != null) contentTransition.stop();
        if (content.getScene() == null || animationDuration.lessThanOrEqualTo(Duration.ZERO)) {
            finishContentAnimation(expand);
            return;
        }
        content.setManaged(true);
        content.setVisible(true);
        content.setMinHeight(0);
        double currentHeight = Math.max(0, content.getHeight());
        double targetHeight;
        if (expand) {
            content.setPrefHeight(Region.USE_COMPUTED_SIZE);
            content.setMaxHeight(Region.USE_COMPUTED_SIZE);
            content.applyCss();
            targetHeight = content.prefHeight(getWidth() > 0 ? getWidth() : -1);
            content.setPrefHeight(0);
            content.setMaxHeight(0);
            content.setOpacity(0);
            currentHeight = 0;
        } else {
            targetHeight = 0;
            content.setPrefHeight(currentHeight);
            content.setMaxHeight(currentHeight);
        }
        contentTransition = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(content.prefHeightProperty(), currentHeight),
                        new KeyValue(content.maxHeightProperty(), currentHeight),
                        new KeyValue(content.opacityProperty(), expand ? 0 : 1)),
                new KeyFrame(animationDuration,
                        new KeyValue(content.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(content.maxHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(content.opacityProperty(), expand ? 1 : 0, Interpolator.EASE_BOTH)));
        contentTransition.setOnFinished(event -> finishContentAnimation(expand));
        contentTransition.play();
    }

    private void finishContentAnimation(boolean expanded) {
        content.setOpacity(1);
        content.setMinHeight(Region.USE_COMPUTED_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setMaxHeight(Region.USE_COMPUTED_SIZE);
        content.setVisible(expanded);
        content.setManaged(expanded);
        collapsing = false;
        updateStateClasses();
    }

    private void updateStateClasses() {
        getStyleClass().removeAll("ele-collapse-item--active", "ele-collapse-item--disabled",
                "ele-collapse-item--collapsing");
        if (isExpanded() || collapsing) getStyleClass().add("ele-collapse-item--active");
        if (collapsing) getStyleClass().add("ele-collapse-item--collapsing");
        if (isDisabled()) getStyleClass().add("ele-collapse-item--disabled");
    }
}
