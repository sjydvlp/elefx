package com.sjydvlp.elefx.component.tag;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

/**
 * An Element Plus-inspired tag for marking and selection.
 *
 * <p>
 * Set {@link #setClosable(boolean)} to display a close affordance. Activating it fires
 * {@link #CLOSE}; applications may remove the tag from their parent in the close handler.
 * </p>
 */
public class EleFXTag extends StackPane implements Themable {

    public static final EventType<Event> CLOSE = new EventType<>(Event.ANY, "ELEFX_TAG_CLOSE");

    private static final String STYLE_CLASS = "ele-tag";

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final ObjectProperty<EleFXTagType> type = new SimpleObjectProperty<>(this, "type", EleFXTagType.PRIMARY);

    private final ObjectProperty<EleFXTagEffect> effect = new SimpleObjectProperty<>(this, "effect",
            EleFXTagEffect.LIGHT);

    private final ObjectProperty<EleFXTagSize> size = new SimpleObjectProperty<>(this, "size", EleFXTagSize.DEFAULT);

    private final ObjectProperty<Paint> color = new SimpleObjectProperty<>(this, "color");

    private final BooleanProperty closable = new SimpleBooleanProperty(this, "closable", false);

    private final BooleanProperty disableTransitions = new SimpleBooleanProperty(this, "disableTransitions", false);

    private final BooleanProperty hit = new SimpleBooleanProperty(this, "hit", false);

    private final BooleanProperty round = new SimpleBooleanProperty(this, "round", false);

    private final ObjectProperty<EventHandler<Event>> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final HBox contentBox = new HBox();

    private final Button closeButton = new Button();

    private final EleFXIcon closeIcon = new EleFXIcon(EleFXIconType.CLOSE, 14);

    private final Label textNode = new Label();

    public EleFXTag() {
        initialize();
    }

    public EleFXTag(String text) {
        setText(text);
        initialize();
    }

    public EleFXTag(String text, EleFXTagType type) {
        setText(text);
        setType(type);
        initialize();
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text == null ? "" : text);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    public EleFXTagType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXTagType> typeProperty() {
        return type;
    }

    public void setType(EleFXTagType type) {
        this.type.set(type == null ? EleFXTagType.PRIMARY : type);
    }

    /**
     * Returns the Element Plus visual effect. The {@code tagEffect} name avoids the final
     * {@link Node#effectProperty()} inherited from JavaFX.
     */
    public EleFXTagEffect getTagEffect() {
        return effect.get();
    }

    public ObjectProperty<EleFXTagEffect> tagEffectProperty() {
        return effect;
    }

    public void setTagEffect(EleFXTagEffect effect) {
        this.effect.set(effect == null ? EleFXTagEffect.LIGHT : effect);
    }

    public EleFXTagSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXTagSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXTagSize size) {
        this.size.set(size == null ? EleFXTagSize.DEFAULT : size);
    }

    public Paint getColor() {
        return color.get();
    }

    public ObjectProperty<Paint> colorProperty() {
        return color;
    }

    public void setColor(Paint color) {
        this.color.set(color);
    }

    public boolean isClosable() {
        return closable.get();
    }

    public BooleanProperty closableProperty() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable.set(closable);
    }

    public boolean isDisableTransitions() {
        return disableTransitions.get();
    }

    public BooleanProperty disableTransitionsProperty() {
        return disableTransitions;
    }

    public void setDisableTransitions(boolean disableTransitions) {
        this.disableTransitions.set(disableTransitions);
    }

    public boolean isHit() {
        return hit.get();
    }

    public BooleanProperty hitProperty() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit.set(hit);
    }

    public boolean isRound() {
        return round.get();
    }

    public BooleanProperty roundProperty() {
        return round;
    }

    public void setRound(boolean round) {
        this.round.set(round);
    }

    public EventHandler<Event> getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<EventHandler<Event>> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(EventHandler<Event> handler) {
        onClose.set(handler);
    }

    public Button getCloseButton() {
        return closeButton;
    }

    /**
     * Fires the close event without assuming how the application owns or removes this node.
     * JavaFX parent removal is application-owned, so {@code disableTransitions} is retained as
     * compatible state for callers that perform their own removal animation.
     */
    public void close() {
        fireEvent(new Event(this, this, CLOSE));
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.TAG;
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        getChildren().add(contentBox);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(false);
        contentBox.getStyleClass().add("ele-tag__content");
        contentBox.setAlignment(Pos.CENTER_LEFT);
        textNode.getStyleClass().add("ele-tag__text");
        closeButton.getStyleClass().add("ele-tag__close");
        closeButton.setGraphic(closeIcon);
        closeButton.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        closeButton.setAccessibleText("Close tag");
        closeButton.setFocusTraversable(true);
        closeButton.setOnAction(this::handleClose);
        text.addListener((o, oldValue, newValue) -> updateContent());
        content.addListener((o, oldValue, newValue) -> updateContent());
        closable.addListener((o, oldValue, newValue) -> updateContent());
        closable.addListener((o, oldValue, newValue) -> updateBooleanStyle(newValue, "ele-tag--closable"));
        onClose.addListener((o, oldValue, newValue) -> setEventHandler(CLOSE, newValue));
        bindStyle(type, EleFXTagType.PRIMARY);
        bindStyle(effect, EleFXTagEffect.LIGHT);
        bindStyle(size, EleFXTagSize.DEFAULT);
        hit.addListener((o, oldValue, newValue) -> updateBooleanStyle(newValue, "ele-tag--hit"));
        round.addListener((o, oldValue, newValue) -> updateBooleanStyle(newValue, "ele-tag--round"));
        color.addListener((o, oldValue, newValue) -> updateCustomColor(newValue));
        size.addListener((o, oldValue, newValue) -> updateCloseIconSize(newValue));
        updateBooleanStyle(isClosable(), "ele-tag--closable");
        updateBooleanStyle(isHit(), "ele-tag--hit");
        updateBooleanStyle(isRound(), "ele-tag--round");
        updateCustomColor(getColor());
        updateContent();
        sceneBuilderIntegration();
    }

    private void handleClose(ActionEvent event) {
        event.consume();
        close();
    }

    private void updateContent() {
        textNode.setText(getText());
        contentBox.getChildren().setAll(getContent() == null ? textNode : getContent());
        if (isClosable()) contentBox.getChildren().add(closeButton);
    }

    private <T> void bindStyle(ObjectProperty<T> property, T fallback) {
        updateStyle(null, property.get(), fallback);
        property.addListener((o, oldValue, newValue) -> updateStyle(oldValue, newValue, fallback));
    }

    private void updateStyle(Object oldValue, Object newValue, Object fallback) {
        if (oldValue instanceof EleFXTagType value) getStyleClass().remove(value.styleClass());
        if (oldValue instanceof EleFXTagEffect value) getStyleClass().remove(value.styleClass());
        if (oldValue instanceof EleFXTagSize value) getStyleClass().remove(value.styleClass());
        Object resolved = newValue == null ? fallback : newValue;
        String styleClass = resolved instanceof EleFXTagType value
                ? value.styleClass()
                : resolved instanceof EleFXTagEffect value
                        ? value.styleClass()
                        : ((EleFXTagSize) resolved).styleClass();
        if (!getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
    }

    private void updateBooleanStyle(boolean active, String styleClass) {
        if (active && !getStyleClass().contains(styleClass)) getStyleClass().add(styleClass);
        if (!active) getStyleClass().remove(styleClass);
    }

    private void updateCustomColor(Paint value) {
        updateBooleanStyle(value != null, "ele-tag--custom-color");
        setStyle(value == null ? "" : "-fx-background-color: " + toCss(value) + ";");
    }

    private void updateCloseIconSize(EleFXTagSize tagSize) {
        closeIcon.setSize(switch (tagSize == null ? EleFXTagSize.DEFAULT : tagSize) {
            case LARGE -> 16;
            case DEFAULT -> 14;
            case SMALL -> 12;
        });
    }

    private String toCss(Paint paint) {
        return paint.toString().replace("0x", "#");
    }
}
