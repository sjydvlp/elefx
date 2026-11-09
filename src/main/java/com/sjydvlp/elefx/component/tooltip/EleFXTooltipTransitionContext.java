package com.sjydvlp.elefx.component.tooltip;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;

/**
 * Animation targets for one tooltip enter or leave operation. Animate opacity on
 * {@link #getContent()} and use the offset properties for movement; translating
 * the content node itself would change JavaFX Popup's measured window bounds.
 */
public final class EleFXTooltipTransitionContext {

    private final Node content;

    private final DoubleProperty offsetX;

    private final DoubleProperty offsetY;

    private final boolean interrupted;

    EleFXTooltipTransitionContext(Node content, DoubleProperty offsetX, DoubleProperty offsetY, boolean interrupted) {
        this.content = content;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.interrupted = interrupted;
    }

    public Node getContent() {
        return content;
    }

    public DoubleProperty offsetXProperty() {
        return offsetX;
    }

    public DoubleProperty offsetYProperty() {
        return offsetY;
    }

    /** True when this operation replaces an unfinished enter or leave animation. */
    public boolean isInterrupted() {
        return interrupted;
    }
}
