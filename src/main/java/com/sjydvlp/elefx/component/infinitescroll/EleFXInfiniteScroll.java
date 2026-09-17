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
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

/** A scroll pane that calls a loading action as its content approaches the bottom. */
public class EleFXInfiniteScroll extends ScrollPane implements Themable {

    private static final String STYLE_CLASS = "ele-infinite-scroll";

    private final ObjectProperty<Runnable> onLoad = new SimpleObjectProperty<>(this, "onLoad");

    private final IntegerProperty delay = new SimpleIntegerProperty(this, "delay", 200);

    private final IntegerProperty distance = new SimpleIntegerProperty(this, "distance", 0);

    private final BooleanProperty immediate = new SimpleBooleanProperty(this, "immediate", true);

    private final PauseTransition throttle = new PauseTransition();

    private final ChangeListener<Bounds> contentBoundsListener = (observable, oldBounds, newBounds) -> evaluate(true);

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

    public void setOnLoad(Runnable value) {
        onLoad.set(value);
    }

    /** Throttle interval in milliseconds. */
    public int getDelay() {
        return delay.get();
    }

    public IntegerProperty delayProperty() {
        return delay;
    }

    public void setDelay(int value) {
        if (value < 0) throw new IllegalArgumentException("delay must not be negative");
        delay.set(value);
    }

    /** Distance in pixels from the bottom at which loading is triggered. */
    public int getDistance() {
        return distance.get();
    }

    public IntegerProperty distanceProperty() {
        return distance;
    }

    public void setDistance(int value) {
        if (value < 0) throw new IllegalArgumentException("distance must not be negative");
        distance.set(value);
    }

    public boolean isImmediate() {
        return immediate.get();
    }

    public BooleanProperty immediateProperty() {
        return immediate;
    }

    public void setImmediate(boolean value) {
        immediate.set(value);
    }

    /** Re-evaluates the current scroll position after asynchronous content changes. */
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
        contentProperty().addListener((observable, oldContent, newContent) -> observeContent(oldContent, newContent));
        observeContent(null, getContent());
        vvalueProperty().addListener((observable, oldValue, newValue) -> evaluate(false));
        viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> evaluate(true));
        disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                throttle.stop();
            else
                evaluate(true);
        });
        immediate.addListener((observable, oldValue, newValue) -> {
            if (newValue) evaluate(true);
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) Platform.runLater(() -> evaluate(true));
        });
        Platform.runLater(() -> evaluate(true));
        sceneBuilderIntegration();
    }

    private void observeContent(Node oldContent, Node newContent) {
        if (oldContent != null) oldContent.layoutBoundsProperty().removeListener(contentBoundsListener);
        lastLoadedContentHeight = Double.NaN;
        wasWithinDistance = false;
        if (newContent != null) newContent.layoutBoundsProperty().addListener(contentBoundsListener);
        evaluate(true);
    }

    private void evaluate(boolean layoutChanged) {
        if (isDisabled() || getOnLoad() == null || getContent() == null || getViewportBounds().getHeight() <= 0) return;
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
        if (isDisabled() || getOnLoad() == null) return;
        lastLoadedContentHeight = getContent() == null ? Double.NaN : getContent().getLayoutBounds().getHeight();
        getOnLoad().run();
    }
}
