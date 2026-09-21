package com.sjydvlp.elefx.component.alert;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * An Element Plus-inspired, non-overlay alert message.
 *
 * <p>
 * Text properties provide the normal title and description content. {@link #setTitleNode(Node)},
 * {@link #setDescriptionNode(Node)}, and {@link #setIconNode(Node)} offer JavaFX equivalents of
 * the corresponding Element Plus slots. Calling {@link #close()} hides the alert and fires
 * {@link #CLOSE}; use {@link #show()} to make it visible again.
 * </p>
 */
public class EleFXAlert extends StackPane implements Themable {

    public static final EventType<Event> CLOSE = new EventType<>(Event.ANY, "ELEFX_ALERT_CLOSE");

    private static final String STYLE_CLASS = "ele-alert";

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty description = new SimpleStringProperty(this, "description", "");

    private final StringProperty closeText = new SimpleStringProperty(this, "closeText", "");

    private final ObjectProperty<EleFXAlertType> type = new SimpleObjectProperty<>(this, "type", EleFXAlertType.INFO);

    private final ObjectProperty<EleFXAlertEffect> alertEffect = new SimpleObjectProperty<>(this, "alertEffect",
            EleFXAlertEffect.LIGHT);

    private final BooleanProperty closable = new SimpleBooleanProperty(this, "closable", true);

    private final BooleanProperty center = new SimpleBooleanProperty(this, "center", false);

    private final BooleanProperty showIcon = new SimpleBooleanProperty(this, "showIcon", false);

    private final ObjectProperty<Node> titleNode = new SimpleObjectProperty<>(this, "titleNode");

    private final ObjectProperty<Node> descriptionNode = new SimpleObjectProperty<>(this, "descriptionNode");

    private final ObjectProperty<Node> iconNode = new SimpleObjectProperty<>(this, "iconNode");

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final HBox body = new HBox();

    private final StackPane iconBox = new StackPane();

    private final VBox messageBox = new VBox();

    private final StackPane titleBox = new StackPane();

    private final StackPane descriptionBox = new StackPane();

    private final Label titleLabel = new Label();

    private final Label descriptionLabel = new Label();

    private final Button closeButton = new Button();

    public EleFXAlert() {
        initialize();
    }

    public EleFXAlert(String title) {
        setTitle(title);
        initialize();
    }

    public EleFXAlert(String title, EleFXAlertType type) {
        setTitle(title);
        setType(type);
        initialize();
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String value) {
        title.set(value == null ? "" : value);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String value) {
        description.set(value == null ? "" : value);
    }

    public String getCloseText() {
        return closeText.get();
    }

    public StringProperty closeTextProperty() {
        return closeText;
    }

    public void setCloseText(String value) {
        closeText.set(value == null ? "" : value);
    }

    public EleFXAlertType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXAlertType> typeProperty() {
        return type;
    }

    public void setType(EleFXAlertType value) {
        type.set(value == null ? EleFXAlertType.INFO : value);
    }

    /** The name avoids the final {@link Node#effectProperty()} inherited from JavaFX. */
    public EleFXAlertEffect getAlertEffect() {
        return alertEffect.get();
    }

    public ObjectProperty<EleFXAlertEffect> alertEffectProperty() {
        return alertEffect;
    }

    public void setAlertEffect(EleFXAlertEffect value) {
        alertEffect.set(value == null ? EleFXAlertEffect.LIGHT : value);
    }

    public boolean isClosable() {
        return closable.get();
    }

    public BooleanProperty closableProperty() {
        return closable;
    }

    public void setClosable(boolean value) {
        closable.set(value);
    }

    public boolean isCenter() {
        return center.get();
    }

    public BooleanProperty centerProperty() {
        return center;
    }

    public void setCenter(boolean value) {
        center.set(value);
    }

    public boolean isShowIcon() {
        return showIcon.get();
    }

    public BooleanProperty showIconProperty() {
        return showIcon;
    }

    public void setShowIcon(boolean value) {
        showIcon.set(value);
    }

    public Node getTitleNode() {
        return titleNode.get();
    }

    public ObjectProperty<Node> titleNodeProperty() {
        return titleNode;
    }

    public void setTitleNode(Node value) {
        titleNode.set(value);
    }

    public Node getDescriptionNode() {
        return descriptionNode.get();
    }

    public ObjectProperty<Node> descriptionNodeProperty() {
        return descriptionNode;
    }

    public void setDescriptionNode(Node value) {
        descriptionNode.set(value);
    }

    public Node getIconNode() {
        return iconNode.get();
    }

    public ObjectProperty<Node> iconNodeProperty() {
        return iconNode;
    }

    public void setIconNode(Node value) {
        iconNode.set(value);
    }

    public EventHandler<Event> getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<EventHandler<Event>> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(EventHandler<Event> value) {
        onClose.set(value);
    }

    public HBox getBody() {
        return body;
    }

    public StackPane getIconBox() {
        return iconBox;
    }

    public StackPane getTitleBox() {
        return titleBox;
    }

    public StackPane getDescriptionBox() {
        return descriptionBox;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    /** Dismisses the alert, removes its layout space, and fires {@link #CLOSE}. */
    public void close() {
        if (!isVisible()) return;
        setVisible(false);
        setManaged(false);
        fireEvent(new Event(this, this, CLOSE));
    }

    /** Restores an alert dismissed with {@link #close()}. */
    public void show() {
        setManaged(true);
        setVisible(true);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.ALERT;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        body.getStyleClass().add("ele-alert__content");
        body.setAlignment(Pos.CENTER_LEFT);
        body.setMaxWidth(Double.MAX_VALUE);
        iconBox.getStyleClass().add("ele-alert__icon");
        messageBox.getStyleClass().add("ele-alert__message");
        messageBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(messageBox, Priority.ALWAYS);
        titleBox.getStyleClass().add("ele-alert__title");
        descriptionBox.getStyleClass().add("ele-alert__description");
        titleLabel.getStyleClass().add("ele-alert__title-text");
        titleLabel.setWrapText(true);
        titleLabel.setTextOverrun(OverrunStyle.CLIP);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        descriptionLabel.getStyleClass().add("ele-alert__description-text");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextOverrun(OverrunStyle.CLIP);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);
        closeButton.getStyleClass().add("ele-alert__close");
        closeButton.setAccessibleText("Close alert");
        closeButton.setFocusTraversable(true);
        closeButton.setOnAction(this::handleClose);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        messageBox.getChildren().addAll(titleBox, descriptionBox);
        body.getChildren().addAll(iconBox, messageBox);
        getChildren().addAll(body, closeButton);

        title.addListener(o -> refreshTitle());
        description.addListener(o -> refreshDescription());
        titleNode.addListener(o -> refreshTitle());
        descriptionNode.addListener(o -> refreshDescription());
        iconNode.addListener(o -> refreshIcon());
        type.addListener((o, oldValue, value) -> {
            updateType(oldValue, value);
            refreshIcon();
        });
        showIcon.addListener(o -> refreshIcon());
        closable.addListener(o -> refreshClose());
        closeText.addListener(o -> refreshClose());
        center.addListener((o, oldValue, value) -> updateCenter(value));
        alertEffect.addListener((o, oldValue, value) -> updateEffect(oldValue, value));
        onClose.addListener((o, oldValue, value) -> setEventHandler(CLOSE, value));
        updateType(null, getType());
        updateEffect(null, getAlertEffect());
        updateCenter(isCenter());
        refreshTitle();
        refreshDescription();
        refreshIcon();
        refreshClose();
        sceneBuilderIntegration();
    }

    private void handleClose(ActionEvent event) {
        event.consume();
        close();
    }

    private void refreshTitle() {
        refreshTextBox(titleBox, titleLabel, getTitleNode(), getTitle());
    }

    private void refreshDescription() {
        refreshTextBox(descriptionBox, descriptionLabel, getDescriptionNode(), getDescription());
    }

    private static void refreshTextBox(StackPane box, Label label, Node custom, String text) {
        Node node = custom == null ? label : custom;
        label.setText(text);
        boolean present = custom != null || !text.isBlank();
        box.getChildren().setAll(node);
        box.setVisible(present);
        box.setManaged(present);
    }

    private void refreshIcon() {
        boolean present = isShowIcon();
        iconBox.getChildren().setAll(getIconNode() == null ? defaultIcon() : getIconNode());
        iconBox.setVisible(present);
        iconBox.setManaged(present);
    }

    private Node defaultIcon() {
        EleFXIcon icon = new EleFXIcon(switch (getType()) {
            case PRIMARY, INFO -> EleFXIconType.INFO_FILLED;
            case SUCCESS -> EleFXIconType.CIRCLE_CHECK_FILLED;
            case WARNING -> EleFXIconType.WARNING_FILLED;
            case ERROR -> EleFXIconType.CIRCLE_CLOSE_FILLED;
        }, 16);
        icon.getStyleClass().add("ele-alert__default-icon");
        return icon;
    }

    private void refreshClose() {
        boolean textClose = !getCloseText().isBlank();
        closeButton.setText(textClose ? getCloseText() : "");
        closeButton.setGraphic(textClose ? null : new EleFXIcon(EleFXIconType.CLOSE, 16));
        closeButton.setContentDisplay(textClose
                ? javafx.scene.control.ContentDisplay.TEXT_ONLY
                : javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        closeButton.setVisible(isClosable());
        closeButton.setManaged(isClosable());
        updateStyleClass(closeButton, textClose, "ele-alert__close--text");
    }

    private void updateType(EleFXAlertType oldValue, EleFXAlertType newValue) {
        if (oldValue != null) getStyleClass().remove(oldValue.styleClass());
        String style = (newValue == null ? EleFXAlertType.INFO : newValue).styleClass();
        if (!getStyleClass().contains(style)) getStyleClass().add(style);
    }

    private void updateEffect(EleFXAlertEffect oldValue, EleFXAlertEffect newValue) {
        if (oldValue != null) getStyleClass().remove(oldValue.styleClass());
        String style = (newValue == null ? EleFXAlertEffect.LIGHT : newValue).styleClass();
        if (!getStyleClass().contains(style)) getStyleClass().add(style);
    }

    private void updateCenter(boolean value) {
        updateBooleanStyle(value, "ele-alert--center");
        Pos alignment = value ? Pos.CENTER : Pos.CENTER_LEFT;
        setAlignment(alignment);
        body.setAlignment(alignment);
        messageBox.setAlignment(value ? Pos.CENTER : Pos.CENTER_LEFT);
        titleBox.setAlignment(alignment);
        descriptionBox.setAlignment(alignment);
        titleLabel.setAlignment(alignment);
        descriptionLabel.setAlignment(alignment);
    }

    private void updateBooleanStyle(boolean active, String style) {
        updateStyleClass(this, active, style);
    }

    private static void updateStyleClass(Node node, boolean active, String style) {
        if (active && !node.getStyleClass().contains(style)) node.getStyleClass().add(style);
        if (!active) node.getStyleClass().remove(style);
    }
}
