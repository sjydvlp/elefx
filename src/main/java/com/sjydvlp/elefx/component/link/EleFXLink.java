package com.sjydvlp.elefx.component.link;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;

/**
 * Element Plus inspired text hyperlink.
 *
 * <p>
 * The {@code href} and {@code target} properties describe the destination and are available
 * to action handlers. JavaFX has no document navigation context, so activating a link still
 * dispatches the standard {@link javafx.event.ActionEvent}; applications choose how to navigate.
 * </p>
 */
public class EleFXLink extends Hyperlink implements Themable {

    private static final String STYLE_CLASS = "ele-link";

    private final ObjectProperty<EleFXLinkType> type = new SimpleObjectProperty<>(this, "type", EleFXLinkType.DEFAULT);

    private final ObjectProperty<EleFXLinkUnderline> underline = new SimpleObjectProperty<>(this, "underline",
            EleFXLinkUnderline.HOVER);

    private final StringProperty href = new SimpleStringProperty(this, "href", "");

    private final ObjectProperty<EleFXLinkTarget> target = new SimpleObjectProperty<>(this, "target",
            EleFXLinkTarget.SELF);

    public EleFXLink() {
        initialize();
    }

    public EleFXLink(String text) {
        super(text);
        initialize();
    }

    public EleFXLink(String text, EleFXLinkType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFXLink(String text, Node icon) {
        super(text, icon);
        initialize();
    }

    public EleFXLink(String text, Node icon, EleFXLinkType type) {
        super(text, icon);
        setType(type);
        initialize();
    }

    public EleFXLinkType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXLinkType> typeProperty() {
        return type;
    }

    public void setType(EleFXLinkType type) {
        this.type.set(type == null ? EleFXLinkType.DEFAULT : type);
    }

    /**
     * Returns the Element Plus underline mode. This is named {@code underlineMode} because
     * {@link Hyperlink#underlineProperty()} is JavaFX's final boolean property.
     */
    public EleFXLinkUnderline getUnderlineMode() {
        return underline.get();
    }

    public ObjectProperty<EleFXLinkUnderline> underlineModeProperty() {
        return underline;
    }

    public void setUnderlineMode(EleFXLinkUnderline underline) {
        this.underline.set(underline == null ? EleFXLinkUnderline.HOVER : underline);
    }

    public String getHref() {
        return href.get();
    }

    public StringProperty hrefProperty() {
        return href;
    }

    public void setHref(String href) {
        this.href.set(href == null ? "" : href);
    }

    public EleFXLinkTarget getTarget() {
        return target.get();
    }

    public ObjectProperty<EleFXLinkTarget> targetProperty() {
        return target;
    }

    public void setTarget(EleFXLinkTarget target) {
        this.target.set(target == null ? EleFXLinkTarget.SELF : target);
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
        return EleFXThemes.LINK;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateStyleClass(null, getType());
        type.addListener((observable, oldValue, newValue) -> updateStyleClass(oldValue, newValue));
        updateUnderlineStyleClass(null, getUnderlineMode());
        underline.addListener((observable, oldValue, newValue) -> updateUnderlineStyleClass(oldValue, newValue));
        sceneBuilderIntegration();
    }

    private void updateStyleClass(EleFXLinkType oldValue, EleFXLinkType newValue) {
        if (oldValue != null) {
            getStyleClass().remove(oldValue.styleClass());
        }
        addStyleClass((newValue == null ? EleFXLinkType.DEFAULT : newValue).styleClass());
    }

    private void updateUnderlineStyleClass(EleFXLinkUnderline oldValue, EleFXLinkUnderline newValue) {
        if (oldValue != null) {
            getStyleClass().remove(oldValue.styleClass());
        }
        addStyleClass((newValue == null ? EleFXLinkUnderline.HOVER : newValue).styleClass());
    }

    private void addStyleClass(String styleClass) {
        if (!getStyleClass().contains(styleClass)) {
            getStyleClass().add(styleClass);
        }
    }
}
