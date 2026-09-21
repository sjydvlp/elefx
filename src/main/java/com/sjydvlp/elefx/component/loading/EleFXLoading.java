package com.sjydvlp.elefx.component.loading;

import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * An Element Plus inspired loading mask.
 *
 * <p>
 * Use this class directly as the top child of a {@link StackPane} for an in-container
 * loading directive equivalent. {@link #service(Options)} creates a window-owned overlay;
 * it is full screen by default and full-screen calls share one instance until closed.
 * </p>
 */
public class EleFXLoading extends StackPane implements Themable {

    private static EleFXLoading fullscreenService;

    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", true);

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    private final StringProperty maskBackground = new SimpleStringProperty(this, "maskBackground", "");

    private final StringProperty customClass = new SimpleStringProperty(this, "customClass", "");

    private final BooleanProperty fullscreen = new SimpleBooleanProperty(this, "fullscreen", false);

    private final BooleanProperty lock = new SimpleBooleanProperty(this, "lock", false);

    private final BooleanProperty body = new SimpleBooleanProperty(this, "body", false);

    private final ObjectProperty<Node> target = new SimpleObjectProperty<>(this, "target");

    private final ObjectProperty<Node> spinner = new SimpleObjectProperty<>(this, "spinner");

    private final ObjectProperty<BooleanSupplier> beforeClose = new SimpleObjectProperty<>(this, "beforeClose");

    private final ObjectProperty<Runnable> closed = new SimpleObjectProperty<>(this, "closed");

    private final VBox indicator = new VBox();

    private final javafx.scene.control.Label textLabel = new javafx.scene.control.Label();

    private final Arc defaultSpinner = new Arc(18, 18, 14, 14, 35, 285);

    private final RotateTransition rotation = new RotateTransition(Duration.millis(800), defaultSpinner);

    private final Set<String> appliedCustomClasses = new HashSet<>();

    private Stage serviceStage;

    private boolean closing;

    public EleFXLoading() {
        initialize();
    }

    public EleFXLoading(String text) {
        this();
        setText(text);
    }

    /** Creates and displays a loading service. Full-screen services are singleton, as in Element Plus. */
    public static synchronized EleFXLoading service(Options options) {
        Options effective = options == null ? new Options() : options;
        if (effective.isFullscreen() && fullscreenService != null && fullscreenService.isShowing())
            return fullscreenService;
        EleFXLoading loading = new EleFXLoading();
        loading.apply(effective);
        loading.show();
        if (effective.isFullscreen()) fullscreenService = loading;
        return loading;
    }

    public static EleFXLoading service() {
        return service(new Options());
    }

    /** Displays this service instance. The target must already be attached to a visible scene. */
    public void show() {
        if (serviceStage != null && serviceStage.isShowing()) return;
        if (!isFullscreen() && getTarget() == null)
            throw new IllegalStateException("A non-fullscreen loading service requires a target");
        createServiceStage();
        positionServiceStage();
        serviceStage.show();
        positionServiceStage();
        setLoading(true);
    }

    /** Hides this mask. A before-close callback runs before the mask is dismissed. */
    public void close() {
        if (closing) return;
        closing = true;
        BooleanSupplier guard = getBeforeClose();
        if (guard != null && !guard.getAsBoolean()) {
            closing = false;
            return;
        }
        setLoading(false);
        if (serviceStage != null) serviceStage.hide();
        rotation.stop();
        synchronized (EleFXLoading.class) {
            if (fullscreenService == this) fullscreenService = null;
        }
        Runnable callback = getClosed();
        if (callback != null) callback.run();
        closing = false;
    }

    public boolean isShowing() {
        return isLoading() && (serviceStage == null || serviceStage.isShowing());
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean value) {
        loading.set(value);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    /** CSS colour accepted by JavaFX, for example {@code rgba(0, 0, 0, .7)}. */
    public String getMaskBackground() {
        return maskBackground.get();
    }

    public StringProperty maskBackgroundProperty() {
        return maskBackground;
    }

    public void setMaskBackground(String value) {
        maskBackground.set(value == null ? "" : value.trim());
    }

    public String getCustomClass() {
        return customClass.get();
    }

    public StringProperty customClassProperty() {
        return customClass;
    }

    public void setCustomClass(String value) {
        customClass.set(value == null ? "" : value);
    }

    public boolean isFullscreen() {
        return fullscreen.get();
    }

    public BooleanProperty fullscreenProperty() {
        return fullscreen;
    }

    public void setFullscreen(boolean value) {
        fullscreen.set(value);
    }

    /** Consumes input through the mask. Service windows always block their covered area. */
    public boolean isLock() {
        return lock.get();
    }

    public BooleanProperty lockProperty() {
        return lock;
    }

    public void setLock(boolean value) {
        lock.set(value);
    }

    /** Element Plus compatibility flag. A JavaFX service uses the owner window as its body. */
    public boolean isBody() {
        return body.get();
    }

    public BooleanProperty bodyProperty() {
        return body;
    }

    public void setBody(boolean value) {
        body.set(value);
    }

    public Node getTarget() {
        return target.get();
    }

    public ObjectProperty<Node> targetProperty() {
        return target;
    }

    public void setTarget(Node value) {
        target.set(value);
    }

    /** A trusted JavaFX node replacing the default spinner. */
    public Node getSpinner() {
        return spinner.get();
    }

    public ObjectProperty<Node> spinnerProperty() {
        return spinner;
    }

    public void setSpinner(Node value) {
        spinner.set(value);
    }

    public BooleanSupplier getBeforeClose() {
        return beforeClose.get();
    }

    public ObjectProperty<BooleanSupplier> beforeCloseProperty() {
        return beforeClose;
    }

    public void setBeforeClose(BooleanSupplier value) {
        beforeClose.set(value);
    }

    public Runnable getClosed() {
        return closed.get();
    }

    public ObjectProperty<Runnable> closedProperty() {
        return closed;
    }

    public void setClosed(Runnable value) {
        closed.set(value);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.LOADING;
    }

    private void initialize() {
        getStyleClass().add("ele-loading-mask");
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);
        addEventFilter(MouseEvent.ANY, event -> {
            if (isLock()) event.consume();
        });
        indicator.getStyleClass().add("ele-loading-spinner");
        indicator.setAlignment(Pos.CENTER);
        defaultSpinner.getStyleClass().add("ele-loading-spinner__arc");
        defaultSpinner.setType(ArcType.OPEN);
        defaultSpinner.setStroke(Color.web("#409eff"));
        defaultSpinner.setFill(Color.TRANSPARENT);
        textLabel.getStyleClass().add("ele-loading-text");
        textLabel.setWrapText(true);
        indicator.getChildren().addAll(defaultSpinner, textLabel);
        getChildren().add(indicator);
        rotation.setByAngle(360);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setInterpolator(javafx.animation.Interpolator.LINEAR);
        loading.addListener((o, oldValue, showing) -> updateVisible(showing));
        text.addListener(o -> refreshText());
        maskBackground.addListener(o -> refreshBackground());
        customClass.addListener(o -> refreshCustomClasses());
        spinner.addListener(o -> refreshSpinner());
        fullscreen.addListener(o -> refreshModeClass());
        refreshText();
        refreshBackground();
        refreshSpinner();
        refreshModeClass();
        updateVisible(isLoading());
        sceneBuilderIntegration();
    }

    private void updateVisible(boolean showing) {
        setVisible(showing);
        setManaged(showing);
        if (showing)
            rotation.play();
        else
            rotation.stop();
    }

    private void refreshText() {
        textLabel.setText(getText());
        textLabel.setVisible(!getText().isBlank());
        textLabel.setManaged(!getText().isBlank());
    }

    private void refreshBackground() {
        setStyle(getMaskBackground().isBlank() ? "" : "-fx-background-color: " + getMaskBackground() + ";");
    }

    private void refreshSpinner() {
        Node selected = getSpinner() == null ? defaultSpinner : getSpinner();
        if (!indicator.getChildren().contains(selected)) indicator.getChildren().setAll(selected, textLabel);
        defaultSpinner.setVisible(selected == defaultSpinner);
        if (selected != defaultSpinner)
            rotation.stop();
        else if (isLoading()) rotation.play();
    }

    private void refreshCustomClasses() {
        getStyleClass().removeAll(appliedCustomClasses);
        appliedCustomClasses.clear();
        for (String name : getCustomClass().trim().split("\\s+"))
            if (!name.isBlank()) appliedCustomClasses.add(name);
        getStyleClass().addAll(appliedCustomClasses);
    }

    private void refreshModeClass() {
        getStyleClass().remove("ele-loading-mask--fullscreen");
        if (isFullscreen()) getStyleClass().add("ele-loading-mask--fullscreen");
    }

    private void apply(Options options) {
        setTarget(options.getTarget());
        setFullscreen(options.isFullscreen());
        setBody(options.isBody());
        setLock(options.isLock());
        setText(options.getText());
        setMaskBackground(options.getBackground());
        setCustomClass(options.getCustomClass());
        setSpinner(options.getSpinner());
        setBeforeClose(options.getBeforeClose());
        setClosed(options.getClosed());
    }

    private void createServiceStage() {
        serviceStage = new Stage(StageStyle.TRANSPARENT);
        Window owner = ownerWindow();
        if (owner != null) serviceStage.initOwner(owner);
        Scene scene = new Scene(this, 1, 1, Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.LOADING.toData());
        serviceStage.setScene(scene);
        serviceStage.setAlwaysOnTop(true);
        serviceStage.setOnCloseRequest(event -> {
            event.consume();
            close();
        });
    }

    private Window ownerWindow() {
        return getTarget() != null && getTarget().getScene() != null ? getTarget().getScene().getWindow() : null;
    }

    private void positionServiceStage() {
        if (serviceStage == null) return;
        Bounds bounds;
        if (isFullscreen() || isBody()) {
            Window owner = ownerWindow();
            if (owner != null) {
                serviceStage.setX(owner.getX());
                serviceStage.setY(owner.getY());
                serviceStage.setWidth(owner.getWidth());
                serviceStage.setHeight(owner.getHeight());
                return;
            }
            javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            serviceStage.setX(screen.getMinX());
            serviceStage.setY(screen.getMinY());
            serviceStage.setWidth(screen.getWidth());
            serviceStage.setHeight(screen.getHeight());
            return;
        }
        bounds = getTarget().localToScreen(getTarget().getBoundsInLocal());
        if (bounds == null) throw new IllegalStateException("The loading target must be visible before show()");
        serviceStage.setX(bounds.getMinX());
        serviceStage.setY(bounds.getMinY());
        serviceStage.setWidth(bounds.getWidth());
        serviceStage.setHeight(bounds.getHeight());
    }

    /** Options for {@link #service(Options)}. */
    public static final class Options {

        private Node target;

        private boolean fullscreen = true;

        private boolean body;

        private boolean lock;

        private String text = "";

        private String background = "";

        private String customClass = "";

        private Node spinner;

        private BooleanSupplier beforeClose;

        private Runnable closed;

        public Node getTarget() {
            return target;
        }

        public Options setTarget(Node value) {
            target = value;
            return this;
        }

        public boolean isFullscreen() {
            return fullscreen;
        }

        public Options setFullscreen(boolean value) {
            fullscreen = value;
            return this;
        }

        public boolean isBody() {
            return body;
        }

        public Options setBody(boolean value) {
            body = value;
            return this;
        }

        public boolean isLock() {
            return lock;
        }

        public Options setLock(boolean value) {
            lock = value;
            return this;
        }

        public String getText() {
            return text;
        }

        public Options setText(String value) {
            text = Objects.requireNonNullElse(value, "");
            return this;
        }

        public String getBackground() {
            return background;
        }

        public Options setBackground(String value) {
            background = Objects.requireNonNullElse(value, "");
            return this;
        }

        public String getCustomClass() {
            return customClass;
        }

        public Options setCustomClass(String value) {
            customClass = Objects.requireNonNullElse(value, "");
            return this;
        }

        public Node getSpinner() {
            return spinner;
        }

        public Options setSpinner(Node value) {
            spinner = value;
            return this;
        }

        public BooleanSupplier getBeforeClose() {
            return beforeClose;
        }

        public Options setBeforeClose(BooleanSupplier value) {
            beforeClose = value;
            return this;
        }

        public Runnable getClosed() {
            return closed;
        }

        public Options setClosed(Runnable value) {
            closed = value;
            return this;
        }
    }
}
