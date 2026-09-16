package com.sjydvlp.elefx.component.infinitescroll;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

/**
 * A scroll container that calls a loading action as its content approaches the bottom.
 *
 * <p>
 * This is the JavaFX counterpart of Element Plus Infinite Scroll. The action is throttled
 * by {@link #delayProperty()}, can be disabled, and is also checked once after layout by default
 * so an initially short list can load its first page.
 * </p>
 */
public class EleFXInfiniteScroll extends ScrollPane implements Themable {

    private static final String STYLE_CLASS = "ele-infinite-scroll";

    private final ObjectProperty<Runnable> onLoad = new SimpleObjectProperty<>(this, "onLoad");

    private final IntegerProperty delay = new SimpleIntegerProperty(this, "delay", 200);

    private final IntegerProperty distance = new SimpleIntegerProperty(this, "distance", 0);

    private final BooleanProperty immediate = new SimpleBooleanProperty(this, "immediate", true);

    private final PauseTransition throttle = new PauseTransition();

    private double lastLoadedContentHeight = Double.NaN;

    private boolean wasWithinDistance;

    public EleFXInfiniteScroll() {
        initialize();
    }

    public EleFXInfiniteScroll(Node content) {
        super(content);
        initialize();
    }

    public Runnable getOnLoad() {
        return onLoad.get();
    }

    public ObjectProperty<Runnable> onLoadProperty() {
        return onLoad;
    }

    public void setOnLoad(Runnable onLoad) {
        this.onLoad.set(onLoad);
    }

    /** Throttle interval in milliseconds. The default matches Element Plus: 200 ms. */
    public int getDelay() {
        return delay.get();
    }

    public IntegerProperty delayProperty() {
        return delay;
    }

    public void setDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.delay.set(delay);
    }

    /** Distance in pixels from the bottom at which loading is triggered. */
    public int getDistance() {
        return distance.get();
    }

    public IntegerProperty distanceProperty() {
        return distance;
    }

    public void setDistance(int distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("distance must not be negative");
        }
        this.distance.set(distance);
    }

    public boolean isImmediate() {
        return immediate.get();
    }

    public BooleanProperty immediateProperty() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate.set(immediate);
    }

    /** Re-evaluates the current scroll position, useful after asynchronous content changes. */
    public void check() {
        evaluate(false);
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.INFINITE_SCROLL;
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        throttle.setOnFinished(event -> runLoad());
        contentProperty().addListener((observable, oldContent, newContent) -> observeContent(newContent));
        observeContent(getContent());
        vvalueProperty().addListener((observable, oldValue, newValue) -> evaluate(false));
        viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> evaluate(true));
        disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                throttle.stop();
            } else {
                evaluate(true);
            }
        });
        immediate.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                evaluate(true);
            }
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> evaluate(true));
            }
        });
        Platform.runLater(() -> evaluate(true));
        sceneBuilderIntegration();
    }

    private void observeContent(Node content) {
        lastLoadedContentHeight = Double.NaN;
        wasWithinDistance = false;
        if (content != null) {
            content.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> evaluate(true));
        }
        evaluate(true);
    }

    private void evaluate(boolean layoutChanged) {
        if (isDisabled() || getOnLoad() == null || getContent() == null || getViewportBounds().getHeight() <= 0) {
            return;
        }

        double contentHeight = getContent().getLayoutBounds().getHeight();
        double scrollTop = getVvalue() * Math.max(0, contentHeight - getViewportBounds().getHeight());
        double remaining = Math.max(0, contentHeight - getViewportBounds().getHeight() - scrollTop);
        boolean withinDistance = remaining <= getDistance();
        if (!withinDistance) {
            wasWithinDistance = false;
            return;
        }

        boolean contentChanged = Double.compare(contentHeight, lastLoadedContentHeight) != 0;
        boolean shouldLoad = !wasWithinDistance || (isImmediate() && layoutChanged && contentChanged);
        wasWithinDistance = true;
        if (shouldLoad && throttle.getStatus() != javafx.animation.Animation.Status.RUNNING) {
            throttle.setDuration(Duration.millis(getDelay()));
            throttle.playFromStart();
        }
    }

    private void runLoad() {
        if (isDisabled() || getOnLoad() == null) {
            return;
        }
        lastLoadedContentHeight = getContent() == null ? Double.NaN : getContent().getLayoutBounds().getHeight();
        getOnLoad().run();
    }
}
