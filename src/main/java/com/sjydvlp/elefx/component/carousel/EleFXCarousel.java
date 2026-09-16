package com.sjydvlp.elefx.component.carousel;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    private final HBox indicators = new HBox(8);

    private final Button previous = navigationButton("‹", "ele-carousel__arrow--left");

    private final Button next = navigationButton("›", "ele-carousel__arrow--right");

    private Timeline autoplayTimeline;

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
        activate(getActiveIndex() + 1, true);
    }

    public void prev() {
        activate(getActiveIndex() - 1, true);
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
        setFocusTraversable(true);
        stage.getStyleClass().add("ele-carousel__container");
        stage.setMinHeight(150);
        stage.setPrefHeight(300);
        VBox.setVgrow(stage, Priority.ALWAYS);
        indicators.getStyleClass().add("ele-carousel__indicators");
        indicators.setAlignment(Pos.CENTER);
        stage.getChildren().addAll(previous, next, indicators);
        StackPane.setAlignment(previous, Pos.CENTER_LEFT);
        StackPane.setAlignment(next, Pos.CENTER_RIGHT);
        StackPane.setAlignment(indicators, Pos.BOTTOM_CENTER);
        StackPane.setMargin(previous, new Insets(0, 0, 0, 12));
        StackPane.setMargin(next, new Insets(0, 12, 0, 0));
        getChildren().add(stage);
        previous.setOnAction(e -> prev());
        next.setOnAction(e -> next());
        previous.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!previous.isDisabled()) prev();
            event.consume();
        });
        next.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!next.isDisabled()) next();
            event.consume();
        });
        previous.setGraphic(getPreviousIcon());
        next.setGraphic(getNextIcon());
        previousIcon.addListener((observable, oldValue, value) -> previous.setGraphic(value));
        nextIcon.addListener((observable, oldValue, value) -> next.setGraphic(value));
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
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
        button.setFocusTraversable(true);
        return button;
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
        if (items.isEmpty()) return;
        int index = normalize(requested);
        if (index == getActiveIndex()) return;
        int old = getActiveIndex();
        activeIndex.set(index);
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

    private void render(boolean animate) {
        stage.getChildren().removeIf(node -> node instanceof EleFXCarouselItem);
        for (EleFXCarouselItem item : items) {
            item.getStyleClass().removeAll("ele-carousel__item--active", "ele-carousel__item--card-side");
            item.setScaleX(1);
            item.setScaleY(1);
            item.setTranslateX(0);
            item.setTranslateY(0);
            item.setOpacity(1);
        }
        if (!items.isEmpty()) {
            if (getType() == EleFXCarouselType.CARD && items.size() > 1)
                renderCardItems();
            else
                stage.getChildren().add(0, items.get(getActiveIndex()));
            if (animate) {
                Node active = items.get(getActiveIndex());
                FadeTransition fade = new FadeTransition(Duration.millis(250), active);
                active.setOpacity(.4);
                fade.setFromValue(.4);
                fade.setToValue(1);
                fade.play();
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
        int previousIndex = normalize(current - 1);
        int nextIndex = normalize(current + 1);
        if (previousIndex != current) addCard(items.get(previousIndex), "ele-carousel__item--card-side", true);
        if (nextIndex != current && nextIndex != previousIndex)
            addCard(items.get(nextIndex), "ele-carousel__item--card-side", false);
        EleFXCarouselItem active = items.get(current);
        active.getStyleClass().add("ele-carousel__item--active");
        stage.getChildren().add(0, active);
    }

    private void addCard(EleFXCarouselItem item, String styleClass, boolean before) {
        item.getStyleClass().remove("ele-carousel__item--active");
        if (!item.getStyleClass().contains(styleClass)) item.getStyleClass().add(styleClass);
        item.setScaleX(getCardScale());
        item.setScaleY(getCardScale());
        double offset = getDirection() == EleFXCarouselDirection.HORIZONTAL
                ? stage.getWidth() * .34
                : stage.getHeight() * .34;
        if (getDirection() == EleFXCarouselDirection.HORIZONTAL)
            item.setTranslateX(before ? -offset : offset);
        else
            item.setTranslateY(before ? -offset : offset);
        item.setOpacity(.65);
        stage.getChildren().add(0, item);
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
            StackPane.setAlignment(indicators, Pos.BOTTOM_CENTER);
            indicators.getStyleClass().remove("ele-carousel__indicators--outside");
        }
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
    }

    private void updateMotionBlurClass() {
        if (isMotionBlur()) {
            if (!getStyleClass().contains("ele-carousel--motion-blur"))
                getStyleClass().add("ele-carousel--motion-blur");
        } else
            getStyleClass().remove("ele-carousel--motion-blur");
    }
}
