package com.sjydvlp.elefx.component.scrollbar;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;

/**
 * Element Plus inspired scroll container backed by JavaFX {@link ScrollPane}.
 *
 * <p>
 * Use JavaFX's {@link #setPrefHeight(double)} and {@link #setMaxHeight(double)} for the
 * Element Plus {@code height} and {@code max-height} behaviours. Scroll coordinates exposed by
 * this class are measured in pixels, unlike ScrollPane's normalized value properties.
 * </p>
 */
public class EleFXScrollbar extends ScrollPane implements Themable {

    private static final String STYLE_CLASS = "ele-scrollbar";

    private static final String CUSTOM_STYLE_CLASS = "ele-scrollbar--custom";

    private static final String NATIVE_STYLE_CLASS = "ele-scrollbar--native";

    private final BooleanProperty always = new SimpleBooleanProperty(this, "always", false);

    private final BooleanProperty verticalDisabled = new SimpleBooleanProperty(this, "verticalDisabled", false);

    private final BooleanProperty horizontalDisabled = new SimpleBooleanProperty(this, "horizontalDisabled", false);

    private final BooleanProperty nativeScrollbar = new SimpleBooleanProperty(this, "nativeScrollbar", false);

    private final BooleanProperty noresize = new SimpleBooleanProperty(this, "noresize", false);

    private final DoubleProperty minSize = new SimpleDoubleProperty(this, "minSize", 20);

    public EleFXScrollbar() {
        initialize();
    }

    public EleFXScrollbar(Node content) {
        super(content);
        initialize();
    }

    public boolean isAlways() {
        return always.get();
    }

    public BooleanProperty alwaysProperty() {
        return always;
    }

    public void setAlways(boolean always) {
        this.always.set(always);
    }

    public boolean isVerticalDisabled() {
        return verticalDisabled.get();
    }

    public BooleanProperty verticalDisabledProperty() {
        return verticalDisabled;
    }

    public void setVerticalDisabled(boolean verticalDisabled) {
        this.verticalDisabled.set(verticalDisabled);
    }

    public boolean isHorizontalDisabled() {
        return horizontalDisabled.get();
    }

    public BooleanProperty horizontalDisabledProperty() {
        return horizontalDisabled;
    }

    public void setHorizontalDisabled(boolean horizontalDisabled) {
        this.horizontalDisabled.set(horizontalDisabled);
    }

    public boolean isNativeScrollbar() {
        return nativeScrollbar.get();
    }

    public BooleanProperty nativeScrollbarProperty() {
        return nativeScrollbar;
    }

    public void setNativeScrollbar(boolean nativeScrollbar) {
        this.nativeScrollbar.set(nativeScrollbar);
    }

    /** Equivalent to Element Plus {@code noresize}; retained as a layout-update hint for callers. */
    public boolean isNoresize() {
        return noresize.get();
    }

    public BooleanProperty noresizeProperty() {
        return noresize;
    }

    public void setNoresize(boolean noresize) {
        this.noresize.set(noresize);
    }

    /**
     * Requested minimum thumb size. JavaFX does not expose a per-scrollbar thumb-size API, so this
     * property communicates the desired value to CSS/custom skins without changing native policy.
     */
    public double getMinSize() {
        return minSize.get();
    }

    public DoubleProperty minSizeProperty() {
        return minSize;
    }

    public void setMinSize(double minSize) {
        if (!Double.isFinite(minSize) || minSize <= 0) {
            throw new IllegalArgumentException("minSize must be a positive finite number");
        }
        this.minSize.set(minSize);
    }

    /** Returns the horizontal scroll offset in pixels. */
    public double getScrollLeft() {
        return getHvalue() * horizontalRange();
    }

    /** Returns the vertical scroll offset in pixels. */
    public double getScrollTop() {
        return getVvalue() * verticalRange();
    }

    /** Scrolls horizontally to a pixel offset. */
    public void setScrollLeft(double scrollLeft) {
        setHvalue(toNormalized(scrollLeft, horizontalRange()));
    }

    /** Scrolls vertically to a pixel offset. */
    public void setScrollTop(double scrollTop) {
        setVvalue(toNormalized(scrollTop, verticalRange()));
    }

    /** Scrolls to the supplied horizontal and vertical pixel offsets. */
    public void scrollTo(double scrollLeft, double scrollTop) {
        setScrollLeft(scrollLeft);
        setScrollTop(scrollTop);
    }

    /** Forces CSS and layout recalculation after dynamic content changes. */
    public void update() {
        applyCss();
        requestLayout();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.SCROLLBAR;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updatePolicies();
        updateNativeStyleClass(isNativeScrollbar());
        always.addListener(observable -> updatePolicies());
        verticalDisabled.addListener(observable -> updatePolicies());
        horizontalDisabled.addListener(observable -> updatePolicies());
        nativeScrollbar.addListener((observable, oldValue, newValue) -> updateNativeStyleClass(newValue));
        sceneBuilderIntegration();
    }

    private void updatePolicies() {
        setVbarPolicy(isVerticalDisabled()
                ? ScrollBarPolicy.NEVER
                : isAlways() ? ScrollBarPolicy.ALWAYS : ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(isHorizontalDisabled()
                ? ScrollBarPolicy.NEVER
                : isAlways() ? ScrollBarPolicy.ALWAYS : ScrollBarPolicy.AS_NEEDED);
    }

    private void updateNativeStyleClass(boolean nativeScrollbar) {
        getStyleClass().removeAll(CUSTOM_STYLE_CLASS, NATIVE_STYLE_CLASS);
        getStyleClass().add(nativeScrollbar ? NATIVE_STYLE_CLASS : CUSTOM_STYLE_CLASS);
    }

    private double horizontalRange() {
        return Math.max(0, getContent() == null
                ? 0
                : getContent().getLayoutBounds().getWidth() - getViewportBounds().getWidth());
    }

    private double verticalRange() {
        return Math.max(0, getContent() == null
                ? 0
                : getContent().getLayoutBounds().getHeight() - getViewportBounds().getHeight());
    }

    private static double toNormalized(double offset, double range) {
        if (!Double.isFinite(offset) || range <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(1, offset / range));
    }
}
