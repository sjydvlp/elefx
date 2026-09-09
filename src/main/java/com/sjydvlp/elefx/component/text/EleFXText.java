package com.sjydvlp.elefx.component.text;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;

/** Element Plus inspired semantic text label. */
public class EleFXText extends Label implements Themable {

    private static final String STYLE_CLASS = "ele-text";

    private final ObjectProperty<EleFXTextType> type = new SimpleObjectProperty<>(this, "type", EleFXTextType.DEFAULT);

    private final ObjectProperty<EleFXTextSize> size = new SimpleObjectProperty<>(this, "size", EleFXTextSize.DEFAULT);

    private final BooleanProperty truncated = new SimpleBooleanProperty(this, "truncated", false);

    private final IntegerProperty lineClamp = new SimpleIntegerProperty(this, "lineClamp", 0);

    private final ObjectProperty<EleFXTextTag> tag = new SimpleObjectProperty<>(this, "tag", EleFXTextTag.SPAN);

    public EleFXText() {
        initialize();
    }

    public EleFXText(String text) {
        super(text);
        initialize();
    }

    public EleFXText(String text, EleFXTextType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFXText(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    public EleFXText(String text, Node graphic, EleFXTextType type) {
        super(text, graphic);
        setType(type);
        initialize();
    }

    public EleFXTextType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXTextType> typeProperty() {
        return type;
    }

    public void setType(EleFXTextType type) {
        this.type.set(type == null ? EleFXTextType.DEFAULT : type);
    }

    public EleFXTextSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXTextSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXTextSize size) {
        this.size.set(size == null ? EleFXTextSize.DEFAULT : size);
    }

    public boolean isTruncated() {
        return truncated.get();
    }

    public BooleanProperty truncatedProperty() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated.set(truncated);
    }

    /**
     * Limits displayed text to this many wrapped lines. Zero disables the limit.
     * JavaFX clips multi-line overflow because its label control has no native multi-line ellipsis.
     */
    public int getLineClamp() {
        return lineClamp.get();
    }

    public IntegerProperty lineClampProperty() {
        return lineClamp;
    }

    public void setLineClamp(int lineClamp) {
        if (lineClamp < 0) {
            throw new IllegalArgumentException("lineClamp must not be negative");
        }
        this.lineClamp.set(lineClamp);
    }

    public EleFXTextTag getTag() {
        return tag.get();
    }

    public ObjectProperty<EleFXTextTag> tagProperty() {
        return tag;
    }

    public void setTag(EleFXTextTag tag) {
        this.tag.set(tag == null ? EleFXTextTag.SPAN : tag);
    }

    public Node getIcon() {
        return getGraphic();
    }

    public ObjectProperty<Node> iconProperty() {
        return graphicProperty();
    }

    public void setIcon(Node icon) {
        setGraphic(icon);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TEXT;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    @Override
    protected double computePrefHeight(double width) {
        double preferredHeight = super.computePrefHeight(width);
        if (getLineClamp() == 0) {
            return preferredHeight;
        }
        return Math.min(preferredHeight, getLineClamp() * getFont().getSize() * 1.2
                + snappedTopInset() + snappedBottomInset());
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setMinWidth(0);
        updateStyleClass(null, getType());
        type.addListener((observable, oldValue, newValue) -> updateStyleClass(oldValue, newValue));
        updateSizeStyleClass(null, getSize());
        size.addListener((observable, oldValue, newValue) -> updateSizeStyleClass(oldValue, newValue));
        updateTagStyleClass(null, getTag());
        tag.addListener((observable, oldValue, newValue) -> updateTagStyleClass(oldValue, newValue));
        updateOverflow();
        truncated.addListener(observable -> updateOverflow());
        lineClamp.addListener(observable -> updateOverflow());
        sceneBuilderIntegration();
    }

    private void updateOverflow() {
        boolean multiLine = getLineClamp() > 0;
        setWrapText(multiLine);
        setTextOverrun(isTruncated() || getLineClamp() == 1 ? OverrunStyle.ELLIPSIS : OverrunStyle.CLIP);
        updateBooleanStyleClass(isTruncated(), "ele-text--truncated");
        updateBooleanStyleClass(multiLine, "ele-text--line-clamp");
        requestLayout();
    }

    private void updateStyleClass(EleFXTextType oldValue, EleFXTextType newValue) {
        updateEnumStyleClass(oldValue, newValue == null ? EleFXTextType.DEFAULT : newValue);
    }

    private void updateSizeStyleClass(EleFXTextSize oldValue, EleFXTextSize newValue) {
        updateEnumStyleClass(oldValue, newValue == null ? EleFXTextSize.DEFAULT : newValue);
    }

    private void updateTagStyleClass(EleFXTextTag oldValue, EleFXTextTag newValue) {
        updateEnumStyleClass(oldValue, newValue == null ? EleFXTextTag.SPAN : newValue);
    }

    private void updateEnumStyleClass(Object oldValue, Object newValue) {
        if (oldValue instanceof EleFXTextType oldType) getStyleClass().remove(oldType.styleClass());
        if (oldValue instanceof EleFXTextSize oldSize) getStyleClass().remove(oldSize.styleClass());
        if (oldValue instanceof EleFXTextTag oldTag) getStyleClass().remove(oldTag.styleClass());
        String styleClass = newValue instanceof EleFXTextType newType
                ? newType.styleClass()
                : newValue instanceof EleFXTextSize newSize
                        ? newSize.styleClass()
                        : ((EleFXTextTag) newValue).styleClass();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }

    private void updateBooleanStyleClass(boolean enabled, String styleClass) {
        if (enabled && !getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
        if (!enabled) getStyleClass().remove(styleClass);
    }
}
