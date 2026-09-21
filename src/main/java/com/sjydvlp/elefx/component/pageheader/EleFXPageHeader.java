package com.sjydvlp.elefx.component.pageheader;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * An Element Plus-inspired page header for simple page navigation.
 *
 * <p>
 * The named Element Plus slots map to node properties: {@linkplain #breadcrumbProperty()
 * breadcrumb}, {@linkplain #iconProperty() icon}, {@linkplain #titleProperty() title},
 * {@linkplain #contentProperty() content}, and {@linkplain #extraProperty() extra}. Add the
 * default-slot content through {@link #getBodyChildren()}.
 * </p>
 */
public class EleFXPageHeader extends VBox implements Themable {

    private final StringProperty titleText = new SimpleStringProperty(this, "titleText", "Back");

    private final StringProperty contentText = new SimpleStringProperty(this, "contentText", "");

    private final ObjectProperty<Node> breadcrumb = new SimpleObjectProperty<>(this, "breadcrumb");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon",
            new EleFXIcon(EleFXIconType.BACK, 20));

    private final ObjectProperty<Node> title = new SimpleObjectProperty<>(this, "title");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final ObjectProperty<Node> extra = new SimpleObjectProperty<>(this, "extra");

    private final ObjectProperty<EventHandler<EleFXPageHeaderEvent>> onBack = new SimpleObjectProperty<>(this,
            "onBack");

    private final StackPane breadcrumbBox = new StackPane();

    private final HBox header = new HBox();

    private final HBox back = new HBox();

    private final StackPane iconBox = new StackPane();

    private final StackPane titleBox = new StackPane();

    private final StackPane contentBox = new StackPane();

    private final StackPane extraBox = new StackPane();

    private final VBox body = new VBox();

    private final Label titleLabel = new Label();

    private final Label contentLabel = new Label();

    public EleFXPageHeader() {
        initialize();
    }

    public EleFXPageHeader(Node... bodyContent) {
        this();
        if (bodyContent != null) getBodyChildren().addAll(bodyContent);
    }

    /** Text used when no custom {@link #getTitle() title node} is present. Defaults to {@code Back}. */
    public String getTitleText() {
        return titleText.get();
    }

    public void setTitleText(String value) {
        titleText.set(value == null ? "" : value);
    }

    public StringProperty titleTextProperty() {
        return titleText;
    }

    /** Alias for {@link #getTitleText()} matching the Element Plus attribute name. */
    public String getTitle() {
        return titleText.get();
    }

    public void setTitle(String value) {
        setTitleText(value);
    }

    /** Equivalent to {@link #setTitleNode(Node)}; provided for the named title slot. */
    public void setTitle(Node value) {
        setTitleNode(value);
    }

    /** Text used when no custom {@link #getContent() content node} is present. */
    public String getContentText() {
        return contentText.get();
    }

    public void setContentText(String value) {
        contentText.set(value == null ? "" : value);
    }

    public StringProperty contentTextProperty() {
        return contentText;
    }

    /** Alias for {@link #getContentText()} matching the Element Plus attribute name. */
    public String getContent() {
        return contentText.get();
    }

    public void setContent(String value) {
        setContentText(value);
    }

    /** Equivalent to {@link #setContentNode(Node)}; provided for the named content slot. */
    public void setContent(Node value) {
        setContentNode(value);
    }

    public Node getBreadcrumb() {
        return breadcrumb.get();
    }

    public void setBreadcrumb(Node value) {
        breadcrumb.set(value);
    }

    public ObjectProperty<Node> breadcrumbProperty() {
        return breadcrumb;
    }

    /** The icon shown before the back title. Set {@code null} to omit it. */
    public Node getIcon() {
        return icon.get();
    }

    public void setIcon(Node value) {
        icon.set(value);
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    /** Convenience replacement for the icon slot. A {@code null} type hides the icon. */
    public void setIconType(EleFXIconType value) {
        setIcon(value == null ? null : new EleFXIcon(value, 20));
    }

    /** Custom node for the back title; it overrides {@link #getTitleText()}. */
    public Node getTitleNode() {
        return title.get();
    }

    public void setTitleNode(Node value) {
        title.set(value);
    }

    public ObjectProperty<Node> titleProperty() {
        return title;
    }

    /** Custom node for the main header content; it overrides {@link #getContentText()}. */
    public Node getContentNode() {
        return content.get();
    }

    public void setContentNode(Node value) {
        content.set(value);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getExtra() {
        return extra.get();
    }

    public void setExtra(Node value) {
        extra.set(value);
    }

    public ObjectProperty<Node> extraProperty() {
        return extra;
    }

    public EventHandler<EleFXPageHeaderEvent> getOnBack() {
        return onBack.get();
    }

    public void setOnBack(EventHandler<EleFXPageHeaderEvent> value) {
        onBack.set(value);
    }

    public ObjectProperty<EventHandler<EleFXPageHeaderEvent>> onBackProperty() {
        return onBack;
    }

    /** Container for the Element Plus default slot. */
    public VBox getBody() {
        return body;
    }

    public ObservableList<Node> getBodyChildren() {
        return body.getChildren();
    }

    public HBox getHeader() {
        return header;
    }

    public HBox getBack() {
        return back;
    }

    /** Emits the {@link EleFXPageHeaderEvent#BACK} event unless this header is disabled. */
    public void fireBackEvent() {
        if (isDisabled()) return;
        EleFXPageHeaderEvent event = new EleFXPageHeaderEvent(this, this);
        fireEvent(event);
        if (!event.isConsumed() && getOnBack() != null) getOnBack().handle(event);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.PAGE_HEADER;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add("ele-page-header");
        setAlignment(Pos.TOP_LEFT);
        setFillWidth(true);
        breadcrumbBox.getStyleClass().add("ele-page-header__breadcrumb");
        header.getStyleClass().add("ele-page-header__header");
        header.setAlignment(Pos.CENTER_LEFT);
        back.getStyleClass().add("ele-page-header__left");
        back.setAlignment(Pos.CENTER_LEFT);
        back.setFocusTraversable(true);
        iconBox.getStyleClass().add("ele-page-header__icon");
        titleBox.getStyleClass().add("ele-page-header__title");
        contentBox.getStyleClass().add("ele-page-header__content");
        extraBox.getStyleClass().add("ele-page-header__extra");
        extraBox.setAlignment(Pos.CENTER_RIGHT);
        titleLabel.getStyleClass().add("ele-page-header__title-text");
        contentLabel.getStyleClass().add("ele-page-header__content-text");
        body.getStyleClass().add("ele-page-header__body");
        body.setFillWidth(true);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        HBox.setHgrow(extraBox, Priority.NEVER);
        back.getChildren().addAll(iconBox, titleBox);
        header.getChildren().addAll(back, contentBox, extraBox);
        getChildren().addAll(breadcrumbBox, header, body);
        titleText.addListener(observable -> refreshTitle());
        contentText.addListener(observable -> refreshContent());
        breadcrumb.addListener(observable -> refreshNode(breadcrumbBox, getBreadcrumb()));
        icon.addListener(observable -> refreshNode(iconBox, getIcon()));
        title.addListener(observable -> refreshTitle());
        content.addListener(observable -> refreshContent());
        extra.addListener(observable -> refreshNode(extraBox, getExtra()));
        disabledProperty().addListener(observable -> back.setDisable(isDisabled()));
        back.setOnMouseClicked(event -> fireBackEvent());
        back.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                fireBackEvent();
                event.consume();
            }
        });
        refreshNode(breadcrumbBox, getBreadcrumb());
        refreshNode(iconBox, getIcon());
        refreshTitle();
        refreshContent();
        refreshNode(extraBox, getExtra());
        sceneBuilderIntegration();
    }

    private void refreshTitle() {
        refreshNode(titleBox, getTitleNode() == null ? titleLabel : getTitleNode());
        titleLabel.setText(getTitleText());
    }

    private void refreshContent() {
        refreshNode(contentBox, getContentNode() == null ? contentLabel : getContentNode());
        contentLabel.setText(getContentText());
    }

    private static void refreshNode(StackPane box, Node node) {
        box.getChildren().setAll(node == null ? java.util.List.of() : java.util.List.of(node));
        boolean present = node != null && (!(node instanceof Label label) || !label.getText().isBlank());
        box.setVisible(present);
        box.setManaged(present);
    }
}
