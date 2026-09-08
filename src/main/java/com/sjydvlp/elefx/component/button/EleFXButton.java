package com.sjydvlp.elefx.component.button;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Themable;
import com.sjydvlp.elefx.theme.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

/**
 * 按钮
 *
 * @author sjydvlp@163.com
 * @date 2026/8/18 23:33
 */
public class EleFXButton extends Button implements Themable {

    private static final String STYLE_CLASS = "ele-button";

    private static final String CIRCLE_STYLE_CLASS = "ele-button--circle";

    private static final String PLAIN_STYLE_CLASS = "ele-button--plain";

    private static final String ROUND_STYLE_CLASS = "ele-button--round";

    private static final String DASHED_STYLE_CLASS = "ele-button--dashed";

    private static final String LINK_STYLE_CLASS = "ele-button--link";

    private static final String TEXT_STYLE_CLASS = "ele-button--text";

    private static final String TEXT_BACKGROUND_STYLE_CLASS = "ele-button--text-bg";

    private static final String LOADING_STYLE_CLASS = "ele-button--loading";

    private static final String LOADING_INDICATOR_STYLE_CLASS = "ele-button__loading-indicator";

    private final ObjectProperty<EleFXButtonType> type = new SimpleObjectProperty<>(this, "type",
            EleFXButtonType.DEFAULT);

    private final BooleanProperty circle = new SimpleBooleanProperty(this, "circle", false);

    private final ObjectProperty<EleFXButtonSize> size = new SimpleObjectProperty<>(this, "size",
            EleFXButtonSize.DEFAULT);

    private final BooleanProperty plain = new SimpleBooleanProperty(this, "plain", false);

    private final BooleanProperty round = new SimpleBooleanProperty(this, "round", false);

    private final BooleanProperty dashed = new SimpleBooleanProperty(this, "dashed", false);

    private final BooleanProperty link = new SimpleBooleanProperty(this, "link", false);

    private final BooleanProperty textButton = new SimpleBooleanProperty(this, "textButton", false);

    private final BooleanProperty textBackground = new SimpleBooleanProperty(this, "textBackground", false);

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);

    private final ObjectProperty<Node> loadingGraphic = new SimpleObjectProperty<>(this, "loadingGraphic",
            createDefaultLoadingGraphic());

    private Node contentGraphic;

    private boolean updatingGraphic;

    public EleFXButton() {
        super();
        initialize();
    }

    public EleFXButton(String text) {
        super(text);
        initialize();
    }

    public EleFXButton(String text, EleFXButtonType type) {
        super(text);
        setType(type);
        initialize();
    }

    public EleFXButton(String text, EleFXButtonType type, EleFXButtonSize size) {
        super(text);
        setType(type);
        setSize(size);
        initialize();
    }

    public EleFXButton(String text, double prefWidth, double prefHeight) {
        super(text);
        setPrefSize(prefWidth, prefHeight);
        initialize();
    }

    public EleFXButton(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    public EleFXButton(String text, Node icon, EleFXButtonType type) {
        super(text, icon);
        setType(type);
        initialize();
    }

    public EleFXButton(Node icon) {
        super(null, icon);
        initialize();
    }

    public EleFXButton(Node icon, EleFXButtonType type) {
        super(null, icon);
        setType(type);
        initialize();
    }

    public EleFXButton(Node icon, EleFXButtonType type, boolean circle) {
        super(null, icon);
        setType(type);
        setCircle(circle);
        initialize();
    }

    public EleFXButtonType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXButtonType> typeProperty() {
        return type;
    }

    public void setType(EleFXButtonType type) {
        this.type.set(type == null ? EleFXButtonType.DEFAULT : type);
    }

    public boolean isCircle() {
        return circle.get();
    }

    public BooleanProperty circleProperty() {
        return circle;
    }

    public void setCircle(boolean circle) {
        this.circle.set(circle);
    }

    public EleFXButtonSize getSize() {
        return size.get();
    }

    public ObjectProperty<EleFXButtonSize> sizeProperty() {
        return size;
    }

    public void setSize(EleFXButtonSize size) {
        this.size.set(size == null ? EleFXButtonSize.DEFAULT : size);
    }

    public boolean isPlain() {
        return plain.get();
    }

    public BooleanProperty plainProperty() {
        return plain;
    }

    public void setPlain(boolean plain) {
        this.plain.set(plain);
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

    public boolean isDashed() {
        return dashed.get();
    }

    public BooleanProperty dashedProperty() {
        return dashed;
    }

    public void setDashed(boolean dashed) {
        this.dashed.set(dashed);
    }

    public boolean isLink() {
        return link.get();
    }

    public BooleanProperty linkProperty() {
        return link;
    }

    public void setLink(boolean link) {
        this.link.set(link);
    }

    /**
     * Whether the button uses the borderless text-button treatment.
     * Named {@code textButton} to avoid conflicting with {@link #getText()}.
     */
    public boolean isTextButton() {
        return textButton.get();
    }

    public BooleanProperty textButtonProperty() {
        return textButton;
    }

    public void setTextButton(boolean textButton) {
        this.textButton.set(textButton);
    }

    /**
     * Keeps a subtle background behind a text button. It is effective only
     * while {@link #isTextButton()} is {@code true}.
     */
    public boolean isTextBackground() {
        return textBackground.get();
    }

    public BooleanProperty textBackgroundProperty() {
        return textBackground;
    }

    public void setTextBackground(boolean textBackground) {
        this.textBackground.set(textBackground);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    /**
     * Shows the loading graphic and suppresses {@link #fire()} while loading.
     * This does not overwrite the caller's disabled state.
     */
    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public Node getLoadingGraphic() {
        return loadingGraphic.get();
    }

    public ObjectProperty<Node> loadingGraphicProperty() {
        return loadingGraphic;
    }

    public void setLoadingGraphic(Node loadingGraphic) {
        this.loadingGraphic.set(loadingGraphic == null ? createDefaultLoadingGraphic() : loadingGraphic);
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
    public void fire() {
        if (!isLoading()) {
            super.fire();
        }
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.BUTTON;
    }

    @Override
    public boolean sceneBuilderIntegration() {
        return Themable.super.sceneBuilderIntegration();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        updateTypeStyleClass(null, getType());
        type.addListener((observable, oldType, newType) -> updateTypeStyleClass(oldType, newType));
        updateSizeStyleClass(null, getSize());
        size.addListener((observable, oldSize, newSize) -> updateSizeStyleClass(oldSize, newSize));
        updateCircleStyleClass(isCircle());
        circle.addListener((observable, oldCircle, newCircle) -> updateCircleStyleClass(newCircle));
        bindStyleClass(plain, PLAIN_STYLE_CLASS);
        bindStyleClass(round, ROUND_STYLE_CLASS);
        bindStyleClass(dashed, DASHED_STYLE_CLASS);
        bindStyleClass(link, LINK_STYLE_CLASS);
        bindStyleClass(textButton, TEXT_STYLE_CLASS);
        updateTextBackgroundStyleClass();
        textBackground.addListener(observable -> updateTextBackgroundStyleClass());
        textButton.addListener(observable -> updateTextBackgroundStyleClass());
        contentGraphic = getGraphic();
        graphicProperty().addListener((observable, oldGraphic, newGraphic) -> {
            if (!updatingGraphic) {
                contentGraphic = newGraphic;
                if (isLoading()) {
                    showLoadingGraphic();
                }
            }
        });
        updateLoading(isLoading());
        loading.addListener((observable, oldLoading, newLoading) -> updateLoading(newLoading));
        loadingGraphic.addListener((observable, oldGraphic, newGraphic) -> {
            if (isLoading()) {
                showLoadingGraphic();
            }
        });
        setAlignment(Pos.CENTER);
        sceneBuilderIntegration();
    }

    private void updateTypeStyleClass(EleFXButtonType oldType, EleFXButtonType newType) {
        if (oldType != null) {
            getStyleClass().remove(oldType.styleClass());
        }
        getStyleClass().add((newType == null ? EleFXButtonType.DEFAULT : newType).styleClass());
    }

    private void updateSizeStyleClass(EleFXButtonSize oldSize, EleFXButtonSize newSize) {
        if (oldSize != null) {
            getStyleClass().remove(oldSize.styleClass());
        }
        String styleClass = (newSize == null ? EleFXButtonSize.DEFAULT : newSize).styleClass();
        if (!getStyleClass().contains(styleClass)) {
            getStyleClass().add(styleClass);
        }
    }

    private void updateCircleStyleClass(boolean circle) {
        if (circle) {
            if (!getStyleClass().contains(CIRCLE_STYLE_CLASS)) {
                getStyleClass().add(CIRCLE_STYLE_CLASS);
            }
        } else {
            getStyleClass().remove(CIRCLE_STYLE_CLASS);
        }
    }

    private void bindStyleClass(BooleanProperty property, String styleClass) {
        updateBooleanStyleClass(property.get(), styleClass);
        property.addListener((observable, oldValue, newValue) -> updateBooleanStyleClass(newValue, styleClass));
    }

    private void updateBooleanStyleClass(boolean enabled, String styleClass) {
        if (enabled) {
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        } else {
            getStyleClass().remove(styleClass);
        }
    }

    private void updateTextBackgroundStyleClass() {
        updateBooleanStyleClass(isTextButton() && isTextBackground(), TEXT_BACKGROUND_STYLE_CLASS);
    }

    private void updateLoading(boolean loading) {
        updateBooleanStyleClass(loading, LOADING_STYLE_CLASS);
        if (loading) {
            showLoadingGraphic();
        } else {
            showContentGraphic();
        }
    }

    private void showLoadingGraphic() {
        setDisplayedGraphic(getLoadingGraphic());
    }

    private void showContentGraphic() {
        setDisplayedGraphic(contentGraphic);
    }

    private void setDisplayedGraphic(Node graphic) {
        updatingGraphic = true;
        try {
            setGraphic(graphic);
        } finally {
            updatingGraphic = false;
        }
    }

    private static ProgressIndicator createDefaultLoadingGraphic() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.getStyleClass().add(LOADING_INDICATOR_STYLE_CLASS);
        indicator.setMouseTransparent(true);
        return indicator;
    }
}
