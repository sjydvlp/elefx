package com.sjydvlp.elefx.component.card;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Set;

/**
 * An Element Plus inspired information container with optional header and footer areas.
 *
 * <p>
 * Add default content through {@link #getBody()} (or the node constructor). A custom
 * header or footer node takes precedence over its corresponding text property, mirroring
 * Element Plus named slots.
 * </p>
 */
public class EleFXCard extends VBox implements Themable {

    private static final String STYLE_CLASS = "ele-card";

    private final StringProperty headerText = new SimpleStringProperty(this, "headerText", "");

    private final StringProperty footerText = new SimpleStringProperty(this, "footerText", "");

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");

    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer");

    private final ObjectProperty<EleFXCardShadow> shadow = new SimpleObjectProperty<>(this, "shadow",
            EleFXCardShadow.ALWAYS);

    private final StringProperty headerClass = new SimpleStringProperty(this, "headerClass", "");

    private final StringProperty bodyClass = new SimpleStringProperty(this, "bodyClass", "");

    private final StringProperty footerClass = new SimpleStringProperty(this, "footerClass", "");

    private final StringProperty headerStyle = new SimpleStringProperty(this, "headerStyle", "");

    private final StringProperty bodyStyle = new SimpleStringProperty(this, "bodyStyle", "");

    private final StringProperty footerStyle = new SimpleStringProperty(this, "footerStyle", "");

    private final VBox headerBox = new VBox();

    private final VBox body = new VBox();

    private final VBox footerBox = new VBox();

    private final Label headerLabel = new Label();

    private final Label footerLabel = new Label();

    private final Set<String> appliedHeaderClasses = new HashSet<>();

    private final Set<String> appliedBodyClasses = new HashSet<>();

    private final Set<String> appliedFooterClasses = new HashSet<>();

    private String appliedShadowClass;

    public EleFXCard() {
        initialize();
    }

    public EleFXCard(Node... content) {
        this();
        getBody().getChildren().addAll(content);
    }

    /** Text displayed in the header when no custom {@link #getHeader() header node} is set. */
    public String getHeaderText() {
        return headerText.get();
    }

    public StringProperty headerTextProperty() {
        return headerText;
    }

    public void setHeaderText(String value) {
        headerText.set(value == null ? "" : value);
    }

    /** Text displayed in the footer when no custom {@link #getFooter() footer node} is set. */
    public String getFooterText() {
        return footerText.get();
    }

    public StringProperty footerTextProperty() {
        return footerText;
    }

    public void setFooterText(String value) {
        footerText.set(value == null ? "" : value);
    }

    /** Optional node occupying the header area. It has priority over {@link #getHeaderText()}. */
    public Node getHeader() {
        return header.get();
    }

    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    public void setHeader(Node value) {
        header.set(value);
    }

    /** Optional node occupying the footer area. It has priority over {@link #getFooterText()}. */
    public Node getFooter() {
        return footer.get();
    }

    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    public void setFooter(Node value) {
        footer.set(value);
    }

    public EleFXCardShadow getShadow() {
        return shadow.get();
    }

    public ObjectProperty<EleFXCardShadow> shadowProperty() {
        return shadow;
    }

    public void setShadow(EleFXCardShadow value) {
        shadow.set(value == null ? EleFXCardShadow.ALWAYS : value);
    }

    /** The body container holding this card's default content. */
    public VBox getBody() {
        return body;
    }

    public ObservableList<Node> getBodyChildren() {
        return body.getChildren();
    }

    public VBox getHeaderBox() {
        return headerBox;
    }

    public VBox getFooterBox() {
        return footerBox;
    }

    public String getHeaderClass() {
        return headerClass.get();
    }

    public StringProperty headerClassProperty() {
        return headerClass;
    }

    public void setHeaderClass(String value) {
        headerClass.set(value == null ? "" : value.trim());
    }

    public String getBodyClass() {
        return bodyClass.get();
    }

    public StringProperty bodyClassProperty() {
        return bodyClass;
    }

    public void setBodyClass(String value) {
        bodyClass.set(value == null ? "" : value.trim());
    }

    public String getFooterClass() {
        return footerClass.get();
    }

    public StringProperty footerClassProperty() {
        return footerClass;
    }

    public void setFooterClass(String value) {
        footerClass.set(value == null ? "" : value.trim());
    }

    public String getHeaderStyle() {
        return headerStyle.get();
    }

    public StringProperty headerStyleProperty() {
        return headerStyle;
    }

    public void setHeaderStyle(String value) {
        headerStyle.set(value == null ? "" : value);
    }

    public String getBodyStyle() {
        return bodyStyle.get();
    }

    public StringProperty bodyStyleProperty() {
        return bodyStyle;
    }

    public void setBodyStyle(String value) {
        bodyStyle.set(value == null ? "" : value);
    }

    public String getFooterStyle() {
        return footerStyle.get();
    }

    public StringProperty footerStyleProperty() {
        return footerStyle;
    }

    public void setFooterStyle(String value) {
        footerStyle.set(value == null ? "" : value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.CARD;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.TOP_LEFT);
        setFillWidth(true);
        headerBox.getStyleClass().add("ele-card__header");
        body.getStyleClass().add("ele-card__body");
        footerBox.getStyleClass().add("ele-card__footer");
        headerLabel.getStyleClass().add("ele-card__header-text");
        footerLabel.getStyleClass().add("ele-card__footer-text");
        VBox.setVgrow(body, Priority.NEVER);
        getChildren().addAll(headerBox, body, footerBox);
        headerText.addListener(o -> refreshHeader());
        footerText.addListener(o -> refreshFooter());
        header.addListener(o -> refreshHeader());
        footer.addListener(o -> refreshFooter());
        shadow.addListener(o -> refreshShadow());
        headerClass.addListener(o -> applyClasses(headerBox, headerClass.get(), appliedHeaderClasses));
        bodyClass.addListener(o -> applyClasses(body, bodyClass.get(), appliedBodyClasses));
        footerClass.addListener(o -> applyClasses(footerBox, footerClass.get(), appliedFooterClasses));
        headerStyle.addListener(o -> headerBox.setStyle(headerStyle.get()));
        bodyStyle.addListener(o -> body.setStyle(bodyStyle.get()));
        footerStyle.addListener(o -> footerBox.setStyle(footerStyle.get()));
        refreshHeader();
        refreshFooter();
        refreshShadow();
        sceneBuilderIntegration();
    }

    private void refreshHeader() {
        refreshSection(headerBox, headerLabel, getHeader(), getHeaderText());
    }

    private void refreshFooter() {
        refreshSection(footerBox, footerLabel, getFooter(), getFooterText());
    }

    private static void refreshSection(VBox box, Label label, Node custom, String text) {
        box.getChildren().setAll(custom == null ? label : custom);
        if (custom == null) label.setText(text);
        boolean present = custom != null || !text.isBlank();
        box.setManaged(present);
        box.setVisible(present);
    }

    private void refreshShadow() {
        if (appliedShadowClass != null) getStyleClass().remove(appliedShadowClass);
        appliedShadowClass = "ele-card--shadow-" + getShadow().name().toLowerCase();
        getStyleClass().add(appliedShadowClass);
    }

    private static void applyClasses(Node node, String classes, Set<String> applied) {
        node.getStyleClass().removeAll(applied);
        applied.clear();
        if (!classes.isBlank()) {
            for (String styleClass : classes.split("\\s+")) {
                if (!styleClass.isBlank()) applied.add(styleClass);
            }
            node.getStyleClass().addAll(applied);
        }
    }
}
