package com.sjydvlp.elefx.component.anchor;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Element Plus-inspired in-page navigation for a {@link ScrollPane}.
 * <p>
 * Links resolve their {@code href} (for example {@code #installation}) against the scroll
 * content's node id, or may bind a target directly with {@link EleFXAnchorLink#setTarget(Node)}.
 * </p>
 */
public class EleFXAnchor extends StackPane implements Themable {

    private final ObjectProperty<ScrollPane> container = new SimpleObjectProperty<>(this, "container");

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 0);

    private final DoubleProperty bound = new SimpleDoubleProperty(this, "bound", 15);

    private final IntegerProperty duration = new SimpleIntegerProperty(this, "duration", 300);

    private final BooleanProperty marker = new SimpleBooleanProperty(this, "marker", true);

    private final ObjectProperty<EleFXAnchorType> type = new SimpleObjectProperty<>(this, "type",
            EleFXAnchorType.DEFAULT);

    private final ObjectProperty<EleFXAnchorDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXAnchorDirection.VERTICAL);

    private final BooleanProperty selectScrollTop = new SimpleBooleanProperty(this, "selectScrollTop", false);

    private final ReadOnlyObjectWrapper<EleFXAnchorLink> activeLink = new ReadOnlyObjectWrapper<>(this, "activeLink");

    private final ObjectProperty<EventHandler<EleFXAnchorEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final ObjectProperty<EventHandler<EleFXAnchorEvent>> onClick = new SimpleObjectProperty<>(this, "onClick");

    private final ObservableList<EleFXAnchorLink> links = FXCollections.observableArrayList();

    private final Pane markerNode = new Pane();

    private final VBox verticalLinks = new VBox();

    private final HBox horizontalLinks = new HBox();

    private Timeline scrollAnimation;

    private ScrollPane observedContainer;

    private final ChangeListener<Number> scrollListener = (o, a, b) -> queueUpdate();

    private boolean updateQueued;

    public EleFXAnchor() {
        initialize();
    }

    public EleFXAnchor(EleFXAnchorLink... links) {
        this();
        getLinks().addAll(links);
    }

    public ScrollPane getContainer() {
        return container.get();
    }

    public void setContainer(ScrollPane value) {
        container.set(value);
    }

    public ObjectProperty<ScrollPane> containerProperty() {
        return container;
    }

    public double getOffset() {
        return offset.get();
    }

    public void setOffset(double value) {
        requireNonNegative(value, "offset");
        offset.set(value);
    }

    public DoubleProperty offsetProperty() {
        return offset;
    }

    public double getBound() {
        return bound.get();
    }

    public void setBound(double value) {
        requireNonNegative(value, "bound");
        bound.set(value);
    }

    public DoubleProperty boundProperty() {
        return bound;
    }

    public int getDuration() {
        return duration.get();
    }

    public void setDuration(int value) {
        if (value < 0) throw new IllegalArgumentException("duration must not be negative");
        duration.set(value);
    }

    public IntegerProperty durationProperty() {
        return duration;
    }

    public boolean isMarker() {
        return marker.get();
    }

    public void setMarker(boolean value) {
        marker.set(value);
    }

    public BooleanProperty markerProperty() {
        return marker;
    }

    public EleFXAnchorType getType() {
        return type.get();
    }

    public void setType(EleFXAnchorType value) {
        type.set(value == null ? EleFXAnchorType.DEFAULT : value);
    }

    public ObjectProperty<EleFXAnchorType> typeProperty() {
        return type;
    }

    public EleFXAnchorDirection getDirection() {
        return direction.get();
    }

    public void setDirection(EleFXAnchorDirection value) {
        direction.set(value == null ? EleFXAnchorDirection.VERTICAL : value);
    }

    public ObjectProperty<EleFXAnchorDirection> directionProperty() {
        return direction;
    }

    public boolean isSelectScrollTop() {
        return selectScrollTop.get();
    }

    public void setSelectScrollTop(boolean value) {
        selectScrollTop.set(value);
    }

    public BooleanProperty selectScrollTopProperty() {
        return selectScrollTop;
    }

    public ObservableList<EleFXAnchorLink> getLinks() {
        return links;
    }

    public EleFXAnchorLink getActiveLink() {
        return activeLink.get();
    }

    public ReadOnlyObjectProperty<EleFXAnchorLink> activeLinkProperty() {
        return activeLink.getReadOnlyProperty();
    }

    public EventHandler<EleFXAnchorEvent> getOnChange() {
        return onChange.get();
    }

    public void setOnChange(EventHandler<EleFXAnchorEvent> value) {
        onChange.set(value);
    }

    public ObjectProperty<EventHandler<EleFXAnchorEvent>> onChangeProperty() {
        return onChange;
    }

    public EventHandler<EleFXAnchorEvent> getOnClick() {
        return onClick.get();
    }

    public void setOnClick(EventHandler<EleFXAnchorEvent> value) {
        onClick.set(value);
    }

    public ObjectProperty<EventHandler<EleFXAnchorEvent>> onClickProperty() {
        return onClick;
    }

    /** Scrolls to a link's target, resolving an id href when necessary. */
    public void scrollTo(String href) {
        forEachLink(link -> {
            if (link.getHref().equals(href)) scrollTo(link);
        });
    }

    /** Scrolls to a specific link's target. */
    public void scrollTo(EleFXAnchorLink link) {
        ScrollPane pane = getContainer();
        Node target = resolveTarget(link);
        if (pane == null || target == null || pane.getContent() == null) return;
        Bounds contentBounds = pane.getContent().localToScene(pane.getContent().getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
        double range = verticalRange(pane);
        if (contentBounds == null || targetBounds == null || range <= 0) return;
        double desired = (targetBounds.getMinY() - contentBounds.getMinY() - getOffset()) / range;
        animateTo(pane, Math.max(0, Math.min(1, desired)));
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.ANCHOR;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    void activate(EleFXAnchorLink link) {
        EleFXAnchorEvent event = new EleFXAnchorEvent(this, this, EleFXAnchorEvent.CLICK, link);
        if (getOnClick() != null) getOnClick().handle(event);
        fireEvent(event);
        if (!event.isConsumed()) scrollTo(link);
    }

    void linksChanged() {
        rebuild();
    }

    private void initialize() {
        getStyleClass().add("ele-anchor");
        markerNode.getStyleClass().add("ele-anchor__marker");
        markerNode.setManaged(false);
        verticalLinks.getStyleClass().add("ele-anchor__list");
        horizontalLinks.getStyleClass().add("ele-anchor__list");
        getChildren().addAll(markerNode, verticalLinks);
        links.addListener((ListChangeListener<EleFXAnchorLink>) change -> rebuild());
        container.addListener((o, oldValue, newValue) -> observeContainer());
        offset.addListener(o -> queueUpdate());
        bound.addListener(o -> queueUpdate());
        selectScrollTop.addListener(o -> queueUpdate());
        marker.addListener(o -> markerNode.setVisible(isMarker()));
        type.addListener(o -> updateClasses());
        direction.addListener(o -> rebuild());
        layoutBoundsProperty().addListener(o -> updateMarker());
        markerNode.setVisible(isMarker());
        rebuild();
        sceneBuilderIntegration();
    }

    private void rebuild() {
        if (getDirection() == EleFXAnchorDirection.VERTICAL) {
            verticalLinks.getChildren().setAll(links);
            getChildren().setAll(markerNode, verticalLinks);
        } else {
            horizontalLinks.getChildren().setAll(links);
            getChildren().setAll(markerNode, horizontalLinks);
        }
        forEachLink(link -> link.setOwner(this));
        updateClasses();
        queueUpdate();
    }

    private void updateClasses() {
        getStyleClass().removeIf(s -> s.startsWith("ele-anchor--"));
        getStyleClass().add("ele-anchor--" + getDirection().name().toLowerCase());
        getStyleClass().add("ele-anchor--" + getType().name().toLowerCase());
    }

    private void observeContainer() {
        if (observedContainer != null) observedContainer.vvalueProperty().removeListener(scrollListener);
        observedContainer = getContainer();
        if (observedContainer != null) {
            observedContainer.vvalueProperty().addListener(scrollListener);
            observedContainer.viewportBoundsProperty().addListener(o -> queueUpdate());
            observedContainer.contentProperty().addListener(o -> queueUpdate());
        }
        queueUpdate();
    }

    private void queueUpdate() {
        if (!updateQueued) {
            updateQueued = true;
            Platform.runLater(() -> {
                updateQueued = false;
                updateActiveLink();
            });
        }
    }

    private void updateActiveLink() {
        ScrollPane pane = getContainer();
        if (pane == null || pane.getContent() == null) {
            setActive(null);
            return;
        }
        Bounds viewport = viewportInScene(pane);
        if (viewport == null) return;
        final EleFXAnchorLink[] candidate = {null};
        double trigger = viewport.getMinY() + getOffset() + getBound();
        forEachLink(link -> {
            Node target = resolveTarget(link);
            if (target == null || !target.isVisible()) return;
            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
            if (targetBounds != null && targetBounds.getMinY() <= trigger) candidate[0] = link;
        });
        if (candidate[0] == null && isSelectScrollTop()) forEachLink(link -> {
            if (candidate[0] == null && resolveTarget(link) != null) candidate[0] = link;
        });
        setActive(candidate[0]);
    }

    private void setActive(EleFXAnchorLink link) {
        if (activeLink.get() == link) {
            updateMarker();
            return;
        }
        forEachLink(item -> item.setActive(item == link));
        activeLink.set(link);
        updateMarker();
        if (link != null) {
            EleFXAnchorEvent event = new EleFXAnchorEvent(this, this, EleFXAnchorEvent.CHANGE, link);
            if (getOnChange() != null) getOnChange().handle(event);
            fireEvent(event);
        }
    }

    private void updateMarker() {
        EleFXAnchorLink current = getActiveLink();
        if (!isMarker() || current == null || getDirection() != EleFXAnchorDirection.VERTICAL) {
            markerNode.setVisible(false);
            return;
        }
        Bounds sceneBounds = current.localToScene(current.getBoundsInLocal());
        Bounds bounds = sceneBounds == null ? null : sceneToLocal(sceneBounds);
        if (bounds == null) return;
        markerNode.setVisible(true);
        markerNode.resizeRelocate(0, bounds.getMinY(), 2, bounds.getHeight());
    }

    private Node resolveTarget(EleFXAnchorLink link) {
        if (link == null) return null;
        if (link.getTarget() != null) return link.getTarget();
        ScrollPane pane = getContainer();
        String href = link.getHref();
        if (pane == null || pane.getContent() == null || href == null || !href.startsWith("#") || href.length() == 1)
            return null;
        return pane.getContent().lookup("#" + href.substring(1));
    }

    private void animateTo(ScrollPane pane, double value) {
        if (scrollAnimation != null) scrollAnimation.stop();
        if (getDuration() == 0) {
            pane.setVvalue(value);
            return;
        }
        scrollAnimation = new Timeline(new KeyFrame(Duration.millis(getDuration()),
                new KeyValue(pane.vvalueProperty(), value, Interpolator.EASE_BOTH)));
        scrollAnimation.play();
    }

    private void forEachLink(java.util.function.Consumer<EleFXAnchorLink> consumer) {
        for (EleFXAnchorLink link : links)
            visit(link, consumer);
    }

    private void visit(EleFXAnchorLink link, java.util.function.Consumer<EleFXAnchorLink> consumer) {
        consumer.accept(link);
        for (EleFXAnchorLink child : link.getSubLinks())
            visit(child, consumer);
    }

    private static Bounds viewportInScene(ScrollPane pane) {
        Node viewport = pane.lookup(".viewport");
        return viewport == null
                ? pane.localToScene(pane.getViewportBounds())
                : viewport.localToScene(viewport.getBoundsInLocal());
    }

    private static double verticalRange(ScrollPane pane) {
        return Math.max(0, pane.getContent().getLayoutBounds().getHeight() - pane.getViewportBounds().getHeight());
    }

    private static void requireNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(name + " must be non-negative");
    }
}
