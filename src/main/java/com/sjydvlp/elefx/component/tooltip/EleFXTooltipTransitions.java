package com.sjydvlp.elefx.component.tooltip;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Named tooltip animations. Registration is shared; each factory creates a fresh animation. */
public final class EleFXTooltipTransitions {

    private static final Map<String, Definition> DEFINITIONS = new ConcurrentHashMap<>();

    private static final Definition NONE = new Definition(context -> null, context -> null);

    static {
        register("el-fade-in-linear", context -> {
            if (!context.isInterrupted()) context.getContent().setOpacity(0);
            return animate(context, 150, 0, 1, Interpolator.LINEAR);
        }, context -> animate(context, 150, 0, 0, Interpolator.LINEAR));

        register("slide-fade", context -> {
            if (!context.isInterrupted()) {
                context.offsetXProperty().set(120);
                context.getContent().setOpacity(0);
            }
            return animate(context, 300, 0, 1, Interpolator.SPLINE(0, 0, 0.58, 1));
        }, context -> animate(context, 800, 120, 0, Interpolator.SPLINE(1, 0.5, 0.8, 1)));
    }

    private EleFXTooltipTransitions() {
    }

    /**
     * Registers or replaces a name. Factories run on the JavaFX thread and must
     * return fresh, finite animations, or null for an immediate operation.
     * Register before assigning the name to a tooltip's transition property.
     */
    public static void register(String name,
                                Function<EleFXTooltipTransitionContext, Animation> enter,
                                Function<EleFXTooltipTransitionContext, Animation> leave) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Transition name must not be blank");
        DEFINITIONS.put(name, new Definition(Objects.requireNonNull(enter), Objects.requireNonNull(leave)));
    }

    static Definition resolve(String name) {
        if (name == null || name.isBlank()) return NONE;
        Definition definition = DEFINITIONS.get(name);
        if (definition == null) throw new IllegalArgumentException("Unknown tooltip transition: " + name);
        return definition;
    }

    private static Animation animate(EleFXTooltipTransitionContext context, double millis,
                                     double offsetX, double opacity, Interpolator interpolator) {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(context.offsetXProperty(), context.offsetXProperty().get()),
                        new KeyValue(context.offsetYProperty(), context.offsetYProperty().get()),
                        new KeyValue(context.getContent().opacityProperty(), context.getContent().getOpacity())),
                new KeyFrame(Duration.millis(millis),
                        new KeyValue(context.offsetXProperty(), offsetX, interpolator),
                        new KeyValue(context.offsetYProperty(), 0, interpolator),
                        new KeyValue(context.getContent().opacityProperty(), opacity, interpolator)));
    }

    record Definition(Function<EleFXTooltipTransitionContext, Animation> enter,
            Function<EleFXTooltipTransitionContext, Animation> leave) {
    }
}
