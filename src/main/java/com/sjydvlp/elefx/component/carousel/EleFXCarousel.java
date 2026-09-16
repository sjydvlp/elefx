package com.sjydvlp.elefx.component.carousel;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.effect.Effect;
import javafx.scene.effect.MotionBlur;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Element Plus inspired carousel for cycling arbitrary JavaFX content.
 *
 * <p>
 * Use {@link #getItems()} to add {@link EleFXCarouselItem}s. The component uses JavaFX's
 * preferred height, so call {@link #setPrefHeight(double)} (or bind it) to select its height.
 * </p>
 */
public class EleFXCarousel extends VBox implements Themable {

    private static final Duration TRANSITION_DURATION = Duration.millis(500);

    private final ObservableList<EleFXCarouselItem> items = FXCollections.observableArrayList();

    private final IntegerProperty initialIndex = new SimpleIntegerProperty(this, "initialIndex", 0);

    private final ReadOnlyIntegerWrapper activeIndex = new ReadOnlyIntegerWrapper(this, "activeIndex", -1);

    private final BooleanProperty autoplay = new SimpleBooleanProperty(this, "autoplay", true);

    private final IntegerProperty interval = new SimpleIntegerProperty(this, "interval", 3000);

    private final BooleanProperty loop = new SimpleBooleanProperty(this, "loop", true);

    private final BooleanProperty pauseOnHover = new SimpleBooleanProperty(this, "pauseOnHover", true);

    private final ObjectProperty<EleFXCarouselTrigger> trigger = new SimpleObjectProperty<>(this, "trigger",
            EleFXCarouselTrigger.HOVER);

    private final ObjectProperty<EleFXCarouselArrow> arrow = new SimpleObjectProperty<>(this, "arrow",
            EleFXCarouselArrow.HOVER);

    private final ObjectProperty<EleFXCarouselIndicatorPosition> indicatorPosition = new SimpleObjectProperty<>(this,
            "indicatorPosition", EleFXCarouselIndicatorPosition.INSIDE);

    private final ObjectProperty<EleFXCarouselDirection> direction = new SimpleObjectProperty<>(this, "direction",
            EleFXCarouselDirection.HORIZONTAL);

    private final ObjectProperty<EleFXCarouselType> type = new SimpleObjectProperty<>(this, "type",
            EleFXCarouselType.DEFAULT);

    private final DoubleProperty cardScale = new SimpleDoubleProperty(this, "cardScale", .83);

    private final BooleanProperty motionBlur = new SimpleBooleanProperty(this, "motionBlur", false);

    private final ObjectProperty<Node> previousIcon = new SimpleObjectProperty<>(this, "previousIcon",
            new EleFXIcon(EleFXIconType.ARROW_LEFT, 16));

    private final ObjectProperty<Node> nextIcon = new SimpleObjectProperty<>(this, "nextIcon",
            new EleFXIcon(EleFXIconType.ARROW_RIGHT, 16));

    private final ObjectProperty<EventHandler<EleFXCarouselEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final StackPane stage = new StackPane();

    private final Rectangle stageClip = new Rectangle();

    private final FlowPane indicators = new FlowPane(Orientation.HORIZONTAL, 8, 0);

    private final Button previous = navigationButton("‹", "ele-carousel__arrow--left");

    private final Button next = navigationButton("›", "ele-carousel__arrow--right");

    private Timeline autoplayTimeline;

    private int transitionDirection = 1;

    /** Prevent layout-driven card rendering from resetting transforms mid-transition. */
    private boolean cardTransitionRunning;

    /** A motion transition owns the stage until its cleanup is complete. */
    private boolean transitionRunning;

    /** The most recent navigation request received while a motion transition is running. */
    private int pendingIndex = -1;

    private boolean pendingNotify;

    private int pendingDirection;

    public EleFXCarousel() {
        initialize();
    }

    public EleFXCarousel(EleFXCarouselItem... items) {
        this();
        getItems().addAll(items);
    }

    public ObservableList<EleFXCarouselItem> getItems() {
        return items;
    }

    public int getInitialIndex() {
        return initialIndex.get();
    }

    public IntegerProperty initialIndexProperty() {
        return initialIndex;
    }

    public void setInitialIndex(int value) {
        initialIndex.set(value);
    }

    public int getActiveIndex() {
        return activeIndex.get();
    }

    public ReadOnlyIntegerProperty activeIndexProperty() {
        return activeIndex.getReadOnlyProperty();
    }

    public boolean isAutoplay() {
        return autoplay.get();
    }

    public BooleanProperty autoplayProperty() {
        return autoplay;
    }

    public void setAutoplay(boolean value) {
        autoplay.set(value);
    }

    public int getInterval() {
        return interval.get();
    }

    public IntegerProperty intervalProperty() {
        return interval;
    }

    public void setInterval(int value) {
        if (value <= 0) throw new IllegalArgumentException("interval must be positive");
        interval.set(value);
    }

    public boolean isLoop() {
        return loop.get();
    }

    public BooleanProperty loopProperty() {
        return loop;
    }

    public void setLoop(boolean value) {
        loop.set(value);
    }

    public boolean isPauseOnHover() {
        return pauseOnHover.get();
    }

    public BooleanProperty pauseOnHoverProperty() {
        return pauseOnHover;
    }

    public void setPauseOnHover(boolean value) {
        pauseOnHover.set(value);
    }

    public EleFXCarouselTrigger getTrigger() {
        return trigger.get();
    }

    public ObjectProperty<EleFXCarouselTrigger> triggerProperty() {
        return trigger;
    }

    public void setTrigger(EleFXCarouselTrigger value) {
        trigger.set(value == null ? EleFXCarouselTrigger.HOVER : value);
    }

    public EleFXCarouselArrow getArrow() {
        return arrow.get();
    }

    public ObjectProperty<EleFXCarouselArrow> arrowProperty() {
        return arrow;
    }

    public void setArrow(EleFXCarouselArrow value) {
        arrow.set(value == null ? EleFXCarouselArrow.HOVER : value);
    }

    public EleFXCarouselIndicatorPosition getIndicatorPosition() {
        return indicatorPosition.get();
    }

    public ObjectProperty<EleFXCarouselIndicatorPosition> indicatorPositionProperty() {
        return indicatorPosition;
    }

    public void setIndicatorPosition(EleFXCarouselIndicatorPosition value) {
        indicatorPosition.set(value == null ? EleFXCarouselIndicatorPosition.INSIDE : value);
    }

    public EleFXCarouselDirection getDirection() {
        return direction.get();
    }

    public ObjectProperty<EleFXCarouselDirection> directionProperty() {
        return direction;
    }

    public void setDirection(EleFXCarouselDirection value) {
        direction.set(value == null ? EleFXCarouselDirection.HORIZONTAL : value);
    }

    public EleFXCarouselType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXCarouselType> typeProperty() {
        return type;
    }

    public void setType(EleFXCarouselType value) {
        type.set(value == null ? EleFXCarouselType.DEFAULT : value);
    }

    public double getCardScale() {
        return cardScale.get();
    }

    public DoubleProperty cardScaleProperty() {
        return cardScale;
    }

    public void setCardScale(double value) {
        if (value <= 0 || value > 1) throw new IllegalArgumentException("cardScale must be in (0, 1]");
        cardScale.set(value);
    }

    public boolean isMotionBlur() {
        return motionBlur.get();
    }

    public BooleanProperty motionBlurProperty() {
        return motionBlur;
    }

    public void setMotionBlur(boolean value) {
        motionBlur.set(value);
    }

    /** Icon displayed by the button that activates the preceding slide. */
    public Node getPreviousIcon() {
        return previousIcon.get();
    }

    public ObjectProperty<Node> previousIconProperty() {
        return previousIcon;
    }

    public void setPreviousIcon(Node value) {
        previousIcon.set(value);
    }

    /** Icon displayed by the button that activates the following slide. */
    public Node getNextIcon() {
        return nextIcon.get();
    }

    public ObjectProperty<Node> nextIconProperty() {
        return nextIcon;
    }

    public void setNextIcon(Node value) {
        nextIcon.set(value);
    }

    public EventHandler<EleFXCarouselEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<EleFXCarouselEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<EleFXCarouselEvent> value) {
        onChange.set(value);
    }

    /** Selects an item by zero-based index. */
    public void setActiveItem(int index) {
        activate(index, true);
    }

    /** Selects the first item with the supplied name. */
    public void setActiveItem(String name) {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).getName().equals(name)) {
                activate(i, true);
                return;
            }
    }

    public void next() {
        activate(getActiveIndex() + 1, true, 1);
    }

    public void prev() {
        activate(getActiveIndex() - 1, true, -1);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CAROUSEL;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-carousel");
        setSpacing(8);
        setFillWidth(true);
        setFocusTraversable(false);
        stage.getStyleClass().add("ele-carousel__container");
        stage.setMinHeight(150);
        stage.setPrefHeight(300);
        stage.setClip(stageClip);
        stage.layoutBoundsProperty().addListener((observable, oldBounds, bounds) -> {
            stageClip.setWidth(bounds.getWidth());
            stageClip.setHeight(bounds.getHeight());
        });
        stage.widthProperty().addListener((observable, oldWidth, width) -> {
            if (!cardTransitionRunning && getType() == EleFXCarouselType.CARD
                    && !items.isEmpty() && width.doubleValue() > 0)
                render(false);
        });
        stage.heightProperty().addListener((observable, oldHeight, height) -> {
            if (!cardTransitionRunning && getType() == EleFXCarouselType.CARD
                    && !items.isEmpty() && height.doubleValue() > 0)
                render(false);
        });
        VBox.setVgrow(stage, Priority.ALWAYS);
        indicators.getStyleClass().add("ele-carousel__indicators");
        indicators.setAlignment(Pos.CENTER);
        indicators.setMaxHeight(Region.USE_PREF_SIZE);
        stage.getChildren().addAll(previous, next, indicators);
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        // Keep the navigation layer above dynamically replaced carousel items.
        previous.setViewOrder(-1);
        next.setViewOrder(-1);
        StackPane.setAlignment(indicators, Pos.BOTTOM_CENTER);
        StackPane.setMargin(previous, new Insets(0, 0, 0, 12));
        StackPane.setMargin(next, new Insets(0, 12, 0, 0));
        getChildren().add(stage);
        previous.setOnAction(e -> prev());
        next.setOnAction(e -> next());
        previous.setGraphic(getPreviousIcon());
        next.setGraphic(getNextIcon());
        previousIcon.addListener((observable, oldValue, value) -> previous.setGraphic(value));
        nextIcon.addListener((observable, oldValue, value) -> next.setGraphic(value));
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!isEventFrom(event, previous) && isInside(stage, previous, event)) {
                prev();
                event.consume();
                return;
            }
            if (!isEventFrom(event, next) && isInside(stage, next, event)) {
                next();
                event.consume();
                return;
            }
            Node node = event.getPickResult().getIntersectedNode();
            while (node != null && !(node instanceof EleFXCarouselItem))
                node = node.getParent();
            if (node instanceof EleFXCarouselItem item
                    && item.getStyleClass().contains("ele-carousel__item--card-side"))
                setActiveItem(items.indexOf(item));
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.UP)
                prev();
            else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.DOWN) next();
        });
        setOnMouseEntered(e -> {
            updateArrowVisibility();
            if (isPauseOnHover()) stopAutoplay();
        });
        setOnMouseExited(e -> {
            updateArrowVisibility();
            if (isPauseOnHover()) restartAutoplay();
        });
        items.addListener((ListChangeListener<EleFXCarouselItem>) c -> rebuild());
        initialIndex.addListener((o, old, value) -> {
            if (getActiveIndex() < 0) activate(value.intValue(), false);
        });
        autoplay.addListener(o -> restartAutoplay());
        interval.addListener(o -> restartAutoplay());
        loop.addListener(o -> updateNavigationDisabled());
        arrow.addListener(o -> updateArrowVisibility());
        trigger.addListener(o -> rebuildIndicators());
        indicatorPosition.addListener(o -> rebuild());
        direction.addListener(o -> refreshDirectionClass());
        type.addListener(o -> rebuild());
        cardScale.addListener(o -> render(false));
        motionBlur.addListener(o -> updateMotionBlurClass());
        refreshDirectionClass();
        updateMotionBlurClass();
        rebuild();
        sceneBuilderIntegration();
    }

    private Button navigationButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("ele-carousel__arrow", styleClass);
        button.setText("");
        button.setFocusTraversable(false);
        return button;
    }

    private boolean isEventFrom(MouseEvent event, Node ancestor) {
        Node node = event.getPickResult().getIntersectedNode();
        while (node != null) {
            if (node == ancestor) return true;
            node = node.getParent();
        }
        return false;
    }

    private boolean isInside(StackPane parent, Node node, MouseEvent event) {
        return node.getBoundsInParent().contains(parent.sceneToLocal(event.getSceneX(), event.getSceneY()));
    }

    private void rebuild() {
        if (items.isEmpty()) {
            activeIndex.set(-1);
            render(false);
            stopAutoplay();
            return;
        }
        int index = getActiveIndex() < 0 ? getInitialIndex() : Math.min(getActiveIndex(), items.size() - 1);
        activeIndex.set(normalize(index));
        render(false);
        restartAutoplay();
    }

    private void activate(int requested, boolean notify) {
        activate(requested, notify, 0);
    }

    private void activate(int requested, boolean notify, int directionHint) {
        if (items.isEmpty()) return;
        int index = normalize(requested);
        if (transitionRunning) {
            // Do not let overlapping transitions remove each other's carousel nodes. Keeping only
            // the latest request also makes rapid repeated arrow presses feel responsive.
            pendingIndex = directionHint != 0 && pendingIndex >= 0 && pendingDirection == directionHint
                    ? normalize(pendingIndex + directionHint)
                    : index;
            pendingNotify = notify;
            pendingDirection = directionHint;
            return;
        }
        if (index == getActiveIndex()) return;
        int old = getActiveIndex();
        transitionDirection = directionHint != 0
                ? directionHint
                : resolveShortestDirection(old, index);
        activeIndex.set(index);
        if (isMotionBlur() && old >= 0) {
            if (getType() == EleFXCarouselType.CARD)
                playCardMotionBlurTransition(items.get(old), items.get(index));
            else
                playMotionBlurTransition(items.get(old), items.get(index));
        } else
            render(true);
        restartAutoplay();
        if (notify && old >= 0) {
            EleFXCarouselEvent event = new EleFXCarouselEvent(this, this, index, old);
            fireEvent(event);
            if (getOnChange() != null) getOnChange().handle(event);
        }
    }

    private int normalize(int index) {
        if (isLoop()) return Math.floorMod(index, items.size());
        return Math.max(0, Math.min(items.size() - 1, index));
    }

    private int resolveShortestDirection(int from, int to) {
        if (!isLoop()) return to > from ? 1 : -1;
        int forwardDistance = Math.floorMod(to - from, items.size());
        int backwardDistance = Math.floorMod(from - to, items.size());
        return forwardDistance <= backwardDistance ? 1 : -1;
    }

    private void render(boolean animate) {
        stage.getChildren().removeIf(node -> node instanceof EleFXCarouselItem);
        for (EleFXCarouselItem item : items) {
            item.getStyleClass().removeAll("ele-carousel__item--active", "ele-carousel__item--card-side");
            item.setPrefWidth(Region.USE_COMPUTED_SIZE);
            item.setMinWidth(Region.USE_COMPUTED_SIZE);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setPrefHeight(Region.USE_COMPUTED_SIZE);
            item.setMinHeight(Region.USE_COMPUTED_SIZE);
            item.setMaxHeight(Double.MAX_VALUE);
            item.setManaged(true);
            item.setScaleX(1);
            item.setScaleY(1);
            item.setTranslateX(0);
            item.setTranslateY(0);
            item.setOpacity(1);
            item.setViewOrder(0);
        }
        if (!items.isEmpty()) {
            if (getType() == EleFXCarouselType.CARD && items.size() > 1)
                renderCardItems();
            else
                stage.getChildren().add(0, items.get(getActiveIndex()));
            if (animate) {
                Node active = items.get(getActiveIndex());
                playFadeTransition(active);
            }
        }
        rebuildIndicators();
        // Slides are rebuilt dynamically; keep interactive overlays above their content.
        previous.toFront();
        next.toFront();
        if (indicators.getParent() == stage) indicators.toFront();
        updateArrowVisibility();
        updateNavigationDisabled();
    }

    private void renderCardItems() {
        int current = getActiveIndex();
        double cardSize = Math.max(1,
                (getDirection() == EleFXCarouselDirection.HORIZONTAL ? stage.getWidth() : stage.getHeight()) * .5);
        int previousIndex = normalize(current - 1);
        int nextIndex = normalize(current + 1);
        if (previousIndex != current)
            addCard(items.get(previousIndex), "ele-carousel__item--card-side", -1, cardSize);
        if (nextIndex != current && nextIndex != previousIndex)
            addCard(items.get(nextIndex), "ele-carousel__item--card-side", 1, cardSize);
        EleFXCarouselItem active = items.get(current);
        configureCardSize(active, cardSize);
        positionCard(active, cardSize);
        setCardTranslate(active, cardTranslate(0));
        active.setViewOrder(0);
        active.getStyleClass().add("ele-carousel__item--active");
        stage.getChildren().add(active);
    }

    private void addCard(EleFXCarouselItem item, String styleClass, int relativeIndex, double cardSize) {
        item.getStyleClass().remove("ele-carousel__item--active");
        if (!item.getStyleClass().contains(styleClass)) item.getStyleClass().add(styleClass);
        configureCardSize(item, cardSize);
        positionCard(item, cardSize);
        item.setScaleX(getCardScale());
        item.setScaleY(getCardScale());
        setCardTranslate(item, cardTranslate(relativeIndex));
        item.setOpacity(1);
        item.setViewOrder(1);
        stage.getChildren().add(0, item);
    }

    private void configureCardSize(EleFXCarouselItem item, double cardSize) {
        if (getDirection() == EleFXCarouselDirection.HORIZONTAL) {
            item.setPrefWidth(cardSize);
            item.setMinWidth(cardSize);
            item.setMaxWidth(cardSize);
            double crossSize = Math.max(1, stage.getHeight());
            item.setPrefHeight(crossSize);
            item.setMinHeight(crossSize);
            item.setMaxHeight(crossSize);
        } else {
            item.setPrefHeight(cardSize);
            item.setMinHeight(cardSize);
            item.setMaxHeight(cardSize);
            double crossSize = Math.max(1, stage.getWidth());
            item.setPrefWidth(crossSize);
            item.setMinWidth(crossSize);
            item.setMaxWidth(crossSize);
        }
    }

    /**
     * Card slides must not be managed by the stage: StackPane otherwise recalculates their
     * vertical alignment while a scale transition is running. Element Plus places every card at
     * the leading half-size origin; the visible card positions are then produced solely by the
     * same transform formula for both axes.
     */
    private void positionCard(EleFXCarouselItem item, double cardSize) {
        boolean horizontal = getDirection() == EleFXCarouselDirection.HORIZONTAL;
        double width = horizontal ? cardSize : Math.max(1, stage.getWidth());
        double height = horizontal ? Math.max(1, stage.getHeight()) : cardSize;
        item.setManaged(false);
        item.resizeRelocate(0, 0, width, height);
    }

    private double cardTranslate(int relativeIndex) {
        double primarySize = getDirection() == EleFXCarouselDirection.HORIZONTAL
                ? stage.getWidth()
                : stage.getHeight();
        return primarySize * ((2 - getCardScale()) * relativeIndex + 1) / 4;
    }

    private void setCardTranslate(EleFXCarouselItem item, double value) {
        if (getDirection() == EleFXCarouselDirection.HORIZONTAL) {
            item.setTranslateX(value);
            item.setTranslateY(0);
        } else {
            item.setTranslateX(0);
            item.setTranslateY(value);
        }
    }

    private void playFadeTransition(Node node) {
        FadeTransition fade = new FadeTransition(TRANSITION_DURATION, node);
        node.setOpacity(.4);
        fade.setFromValue(.4);
        fade.setToValue(1);
        fade.play();
    }

    private void playMotionBlurTransition(EleFXCarouselItem oldItem, EleFXCarouselItem newItem) {
        transitionRunning = true;
        boolean horizontal = getDirection() == EleFXCarouselDirection.HORIZONTAL;
        double distance = horizontal ? stage.getWidth() : stage.getHeight();
        if (distance <= 0) distance = horizontal ? stage.getPrefWidth() : stage.getPrefHeight();

        stage.getChildren().removeIf(node -> node instanceof EleFXCarouselItem);
        for (EleFXCarouselItem item : items) {
            item.getStyleClass().removeAll("ele-carousel__item--active", "ele-carousel__item--card-side");
            item.setPrefWidth(Region.USE_COMPUTED_SIZE);
            item.setMinWidth(Region.USE_COMPUTED_SIZE);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setPrefHeight(Region.USE_COMPUTED_SIZE);
            item.setMinHeight(Region.USE_COMPUTED_SIZE);
            item.setMaxHeight(Double.MAX_VALUE);
            item.setManaged(true);
            item.setScaleX(1);
            item.setScaleY(1);
            item.setTranslateX(0);
            item.setTranslateY(0);
            item.setOpacity(1);
            item.setViewOrder(0);
        }
        stage.getChildren().addAll(oldItem, newItem);

        if (horizontal)
            newItem.setTranslateX(transitionDirection * distance);
        else
            newItem.setTranslateY(transitionDirection * distance);

        TranslateTransition oldSlide = new TranslateTransition(TRANSITION_DURATION, oldItem);
        TranslateTransition newSlide = new TranslateTransition(TRANSITION_DURATION, newItem);
        if (horizontal) {
            oldSlide.setToX(-transitionDirection * distance);
            newSlide.setToX(0);
        } else {
            oldSlide.setToY(-transitionDirection * distance);
            newSlide.setToY(0);
        }

        Effect oldEffect = oldItem.getEffect();
        Effect newEffect = newItem.getEffect();
        MotionBlur blur = new MotionBlur(horizontal ? 0 : 90, 32);
        oldItem.setEffect(blur);
        newItem.setEffect(blur);
        Timeline blurTransition = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 32)),
                new KeyFrame(TRANSITION_DURATION, new KeyValue(blur.radiusProperty(), 0)));
        ParallelTransition transition = new ParallelTransition(oldSlide, newSlide, blurTransition);
        transition.setOnFinished(event -> {
            if (oldItem.getEffect() == blur) oldItem.setEffect(oldEffect);
            if (newItem.getEffect() == blur) newItem.setEffect(newEffect);
            render(false);
            finishMotionTransition();
        });
        rebuildIndicators();
        previous.toFront();
        next.toFront();
        if (indicators.getParent() == stage) indicators.toFront();
        updateArrowVisibility();
        updateNavigationDisabled();
        transition.play();
    }

    private void playCardMotionBlurTransition(EleFXCarouselItem oldItem, EleFXCarouselItem newItem) {
        transitionRunning = true;
        cardTransitionRunning = true;
        boolean horizontal = getDirection() == EleFXCarouselDirection.HORIZONTAL;
        double cardSize = Math.max(1, (horizontal ? stage.getWidth() : stage.getHeight()) * .5);
        if (oldItem.getParent() != stage) stage.getChildren().add(0, oldItem);
        if (newItem.getParent() != stage) stage.getChildren().add(0, newItem);
        configureCardSize(oldItem, cardSize);
        configureCardSize(newItem, cardSize);
        positionCard(oldItem, cardSize);
        positionCard(newItem, cardSize);
        oldItem.setScaleX(1);
        oldItem.setScaleY(1);
        setCardTranslate(oldItem, cardTranslate(0));
        newItem.setScaleX(getCardScale());
        newItem.setScaleY(getCardScale());
        setCardTranslate(newItem, cardTranslate(transitionDirection));
        // Keep this z-order for the whole transition. Reordering it at completion causes a
        // one-frame flash when the outgoing card still overlaps the newly active card.
        for (EleFXCarouselItem item : items)
            item.setViewOrder(2);
        oldItem.setViewOrder(1);
        newItem.setViewOrder(0);

        TranslateTransition oldSlide = new TranslateTransition(TRANSITION_DURATION, oldItem);
        TranslateTransition newSlide = new TranslateTransition(TRANSITION_DURATION, newItem);
        ScaleTransition oldScale = new ScaleTransition(TRANSITION_DURATION, oldItem);
        ScaleTransition newScale = new ScaleTransition(TRANSITION_DURATION, newItem);
        if (horizontal) {
            oldSlide.setToX(cardTranslate(-transitionDirection));
            newSlide.setToX(cardTranslate(0));
        } else {
            oldSlide.setToY(cardTranslate(-transitionDirection));
            newSlide.setToY(cardTranslate(0));
        }
        oldScale.setToX(getCardScale());
        oldScale.setToY(getCardScale());
        newScale.setToX(1);
        newScale.setToY(1);

        EleFXCarouselItem incomingItem = null;
        TranslateTransition incomingSlide = null;
        if (items.size() > 2) {
            int incomingIndex = normalize(getActiveIndex() + transitionDirection);
            EleFXCarouselItem candidate = items.get(incomingIndex);
            if (candidate != oldItem && candidate != newItem) {
                incomingItem = candidate;
                if (incomingItem.getParent() != stage) stage.getChildren().add(0, incomingItem);
                configureCardSize(incomingItem, cardSize);
                positionCard(incomingItem, cardSize);
                incomingItem.setScaleX(getCardScale());
                incomingItem.setScaleY(getCardScale());
                incomingItem.setOpacity(1);
                incomingItem.setViewOrder(2);
                setCardTranslate(incomingItem, transitionDirection > 0
                        ? (3 + getCardScale()) * (horizontal ? stage.getWidth() : stage.getHeight()) / 4
                        : -(1 + getCardScale()) * (horizontal ? stage.getWidth() : stage.getHeight()) / 4);
                incomingSlide = new TranslateTransition(TRANSITION_DURATION, incomingItem);
                if (horizontal)
                    incomingSlide.setToX(cardTranslate(transitionDirection));
                else
                    incomingSlide.setToY(cardTranslate(transitionDirection));
            }
        }

        Effect oldEffect = oldItem.getEffect();
        Effect newEffect = newItem.getEffect();
        MotionBlur oldBlur = new MotionBlur(horizontal ? 0 : 90, 24);
        MotionBlur newBlur = new MotionBlur(horizontal ? 0 : 90, 24);
        oldItem.setEffect(oldBlur);
        newItem.setEffect(newBlur);
        Timeline blurTransition = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(oldBlur.radiusProperty(), 24),
                        new KeyValue(newBlur.radiusProperty(), 24)),
                new KeyFrame(TRANSITION_DURATION,
                        new KeyValue(oldBlur.radiusProperty(), 0),
                        new KeyValue(newBlur.radiusProperty(), 0)));
        ParallelTransition transition = incomingSlide == null
                ? new ParallelTransition(oldSlide, newSlide, oldScale, newScale, blurTransition)
                : new ParallelTransition(oldSlide, newSlide, oldScale, newScale, incomingSlide, blurTransition);
        EleFXCarouselItem finalIncomingItem = incomingItem;
        transition.setOnFinished(event -> {
            if (oldItem.getEffect() == oldBlur) oldItem.setEffect(oldEffect);
            if (newItem.getEffect() == newBlur) newItem.setEffect(newEffect);
            finishCardMotionBlurTransition(oldItem, newItem, finalIncomingItem);
            cardTransitionRunning = false;
            finishMotionTransition();
        });
        rebuildIndicators();
        previous.toFront();
        next.toFront();
        if (indicators.getParent() == stage) indicators.toFront();
        updateArrowVisibility();
        updateNavigationDisabled();
        transition.play();
    }

    private void finishCardMotionBlurTransition(
                                                EleFXCarouselItem oldItem, EleFXCarouselItem newItem,
                                                EleFXCarouselItem incomingItem) {
        stage.getChildren().removeIf(node -> node instanceof EleFXCarouselItem
                && node != oldItem && node != newItem && node != incomingItem);
        for (EleFXCarouselItem item : items)
            item.getStyleClass().removeAll("ele-carousel__item--active", "ele-carousel__item--card-side");
        newItem.getStyleClass().add("ele-carousel__item--active");
        oldItem.getStyleClass().add("ele-carousel__item--card-side");
        if (incomingItem != null) incomingItem.getStyleClass().add("ele-carousel__item--card-side");
        rebuildIndicators();
        previous.toFront();
        next.toFront();
        if (indicators.getParent() == stage) indicators.toFront();
        updateArrowVisibility();
        updateNavigationDisabled();
    }

    private void finishMotionTransition() {
        transitionRunning = false;
        if (pendingIndex < 0) return;
        int nextIndex = pendingIndex;
        boolean nextNotify = pendingNotify;
        int nextDirection = pendingDirection;
        pendingIndex = -1;
        pendingNotify = false;
        pendingDirection = 0;
        activate(nextIndex, nextNotify, nextDirection);
    }

    private void rebuildIndicators() {
        indicators.getChildren().clear();
        boolean visible = getIndicatorPosition() != EleFXCarouselIndicatorPosition.NONE;
        indicators.setVisible(visible);
        indicators.setManaged(visible);
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            Button button = new Button(items.get(i).getLabel().isBlank() ? "" : items.get(i).getLabel());
            button.getStyleClass().add("ele-carousel__indicator");
            button.setFocusTraversable(false);
            if (i == getActiveIndex()) button.getStyleClass().add("ele-carousel__indicator--active");
            button.setOnAction(e -> setActiveItem(index));
            if (getTrigger() == EleFXCarouselTrigger.HOVER) button.setOnMouseEntered(e -> setActiveItem(index));
            indicators.getChildren().add(button);
        }
        if (getIndicatorPosition() == EleFXCarouselIndicatorPosition.OUTSIDE) {
            stage.getChildren().remove(indicators);
            if (!getChildren().contains(indicators)) getChildren().add(indicators);
            indicators.getStyleClass().add("ele-carousel__indicators--outside");
        } else {
            getChildren().remove(indicators);
            if (!stage.getChildren().contains(indicators)) stage.getChildren().add(indicators);
            indicators.getStyleClass().remove("ele-carousel__indicators--outside");
        }
        updateIndicatorAlignment();
    }

    private void restartAutoplay() {
        stopAutoplay();
        if (!isAutoplay() || items.size() < 2 || isHover()) return;
        autoplayTimeline = new Timeline(new KeyFrame(Duration.millis(getInterval()), e -> next()));
        autoplayTimeline.setCycleCount(Timeline.INDEFINITE);
        autoplayTimeline.play();
    }

    private void stopAutoplay() {
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
            autoplayTimeline = null;
        }
    }

    private void updateNavigationDisabled() {
        previous.setDisable(!isLoop() && getActiveIndex() <= 0);
        next.setDisable(!isLoop() && getActiveIndex() >= items.size() - 1);
    }

    private void updateArrowVisibility() {
        boolean visible = getArrow() == EleFXCarouselArrow.ALWAYS
                || (getArrow() == EleFXCarouselArrow.HOVER && isHover());
        previous.setVisible(visible);
        previous.setManaged(visible);
        next.setVisible(visible);
        next.setManaged(visible);
    }

    private void refreshDirectionClass() {
        getStyleClass().removeAll("ele-carousel--horizontal", "ele-carousel--vertical");
        getStyleClass().add(getDirection() == EleFXCarouselDirection.HORIZONTAL
                ? "ele-carousel--horizontal"
                : "ele-carousel--vertical");
        updateIndicatorAlignment();
    }

    private void updateIndicatorAlignment() {
        if (getDirection() == EleFXCarouselDirection.VERTICAL
                && getIndicatorPosition() == EleFXCarouselIndicatorPosition.INSIDE) {
            indicators.setOrientation(Orientation.VERTICAL);
            indicators.setHgap(0);
            indicators.setVgap(8);
            indicators.setMaxWidth(Region.USE_PREF_SIZE);
            StackPane.setAlignment(indicators, Pos.CENTER_RIGHT);
            StackPane.setMargin(indicators, new Insets(0, 12, 0, 0));
        } else {
            indicators.setOrientation(Orientation.HORIZONTAL);
            indicators.setHgap(8);
            indicators.setVgap(0);
            indicators.setMaxWidth(Double.MAX_VALUE);
            StackPane.setAlignment(indicators, Pos.BOTTOM_CENTER);
            StackPane.setMargin(indicators, Insets.EMPTY);
        }
    }

    private void updateMotionBlurClass() {
        if (isMotionBlur()) {
            if (!getStyleClass().contains("ele-carousel--motion-blur"))
                getStyleClass().add("ele-carousel--motion-blur");
        } else
            getStyleClass().remove("ele-carousel--motion-blur");
    }
}
