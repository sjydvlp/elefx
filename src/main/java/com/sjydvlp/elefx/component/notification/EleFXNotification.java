package com.sjydvlp.elefx.component.notification;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import java.util.*;

/**
 * Element Plus-inspired global notification displayed at a window corner.
 * Instances at the same corner stack automatically; use {@link #close()} or
 * {@link #closeAll()} to dismiss them programmatically.
 */
public class EleFXNotification extends BorderPane implements Themable {

    private static final Map<StackKey, List<EleFXNotification>> INSTANCES = new HashMap<>();

    private static final double EDGE_MARGIN = 16, GAP = 16;

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    private final StringProperty message = new SimpleStringProperty(this, "message", "");

    private final ObjectProperty<Node> messageNode = new SimpleObjectProperty<>(this, "messageNode");

    private final ObjectProperty<EleFXNotificationType> type = new SimpleObjectProperty<>(this, "type");

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final StringProperty customClass = new SimpleStringProperty(this, "customClass", "");

    private final IntegerProperty duration = new SimpleIntegerProperty(this, "duration", 4500);

    private final ObjectProperty<EleFXNotificationPosition> position = new SimpleObjectProperty<>(this, "position",
            EleFXNotificationPosition.TOP_RIGHT);

    private final BooleanProperty showClose = new SimpleBooleanProperty(this, "showClose", true);

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 0);

    private final BooleanProperty progress = new SimpleBooleanProperty(this, "progress", false);

    private final StringProperty progressColor = new SimpleStringProperty(this, "progressColor", "");

    private final BooleanProperty pauseOnHover = new SimpleBooleanProperty(this, "pauseOnHover", true);

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final ObjectProperty<Runnable> onClick = new SimpleObjectProperty<>(this, "onClick");

    private final StackPane iconBox = new StackPane();

    private final Label titleLabel = new Label();

    private final Label messageLabel = new Label();

    private final StackPane messageBox = new StackPane();

    private final Button closeButton = new Button();

    private final ProgressBar progressBar = new ProgressBar(1);

    private final Set<String> appliedClasses = new HashSet<>();

    private Stage stage;

    private StackKey stackKey;

    private PauseTransition timeout;

    private Timeline progressTimeline;

    private long timerStartedAt, remainingMillis;

    private boolean closing;

    public EleFXNotification() {
        initialize();
    }

    public EleFXNotification(String value) {
        this();
        setMessage(value);
    }

    public static EleFXNotification show(String title, String message) {
        return show(new EleFXNotificationOptions().title(title).message(message));
    }

    public static EleFXNotification show(EleFXNotificationOptions options) {
        EleFXNotificationOptions o = options == null ? new EleFXNotificationOptions() : options;
        EleFXNotification result = new EleFXNotification();
        result.apply(o);
        result.stackKey = new StackKey(o.getOwner(), result.getPosition());
        synchronized (INSTANCES) {
            INSTANCES.computeIfAbsent(result.stackKey, ignored -> new ArrayList<>()).add(result);
        }
        result.open();
        return result;
    }

    public static EleFXNotification primary(EleFXNotificationOptions o) {
        return show(withType(o, EleFXNotificationType.PRIMARY));
    }

    public static EleFXNotification success(EleFXNotificationOptions o) {
        return show(withType(o, EleFXNotificationType.SUCCESS));
    }

    public static EleFXNotification info(EleFXNotificationOptions o) {
        return show(withType(o, EleFXNotificationType.INFO));
    }

    public static EleFXNotification warning(EleFXNotificationOptions o) {
        return show(withType(o, EleFXNotificationType.WARNING));
    }

    public static EleFXNotification error(EleFXNotificationOptions o) {
        return show(withType(o, EleFXNotificationType.ERROR));
    }

    public static EleFXNotification success(String title, String message) {
        return success(new EleFXNotificationOptions().title(title).message(message));
    }

    public static EleFXNotification warning(String title, String message) {
        return warning(new EleFXNotificationOptions().title(title).message(message));
    }

    public static EleFXNotification info(String title, String message) {
        return info(new EleFXNotificationOptions().title(title).message(message));
    }

    public static EleFXNotification error(String title, String message) {
        return error(new EleFXNotificationOptions().title(title).message(message));
    }

    private static EleFXNotificationOptions withType(EleFXNotificationOptions o, EleFXNotificationType type) {
        return (o == null ? new EleFXNotificationOptions() : o).type(type);
    }

    public static void closeAll() {
        snapshot().forEach(EleFXNotification::close);
    }

    public static void closeAll(EleFXNotificationPosition position) {
        snapshot().stream().filter(n -> n.getPosition() == position).forEach(EleFXNotification::close);
    }

    public static void updateOffsets(EleFXNotificationPosition position) {
        synchronized (INSTANCES) {
            INSTANCES.keySet().stream().filter(k -> k.position == position).toList()
                    .forEach(EleFXNotification::positionAll);
        }
    }

    private static List<EleFXNotification> snapshot() {
        synchronized (INSTANCES) {
            return INSTANCES.values().stream().flatMap(Collection::stream).toList();
        }
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String v) {
        title.set(v == null ? "" : v);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String v) {
        message.set(v == null ? "" : v);
    }

    public Node getMessageNode() {
        return messageNode.get();
    }

    public ObjectProperty<Node> messageNodeProperty() {
        return messageNode;
    }

    public void setMessageNode(Node v) {
        messageNode.set(v);
    }

    public EleFXNotificationType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXNotificationType> typeProperty() {
        return type;
    }

    public void setType(EleFXNotificationType v) {
        type.set(v);
    }

    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Node v) {
        icon.set(v);
    }

    public String getCustomClass() {
        return customClass.get();
    }

    public StringProperty customClassProperty() {
        return customClass;
    }

    public void setCustomClass(String v) {
        customClass.set(v == null ? "" : v);
    }

    public int getDuration() {
        return duration.get();
    }

    public IntegerProperty durationProperty() {
        return duration;
    }

    public void setDuration(int v) {
        duration.set(Math.max(0, v));
        restartTimer();
    }

    public EleFXNotificationPosition getPosition() {
        return position.get();
    }

    public ObjectProperty<EleFXNotificationPosition> positionProperty() {
        return position;
    }

    public void setPosition(EleFXNotificationPosition v) {
        position.set(v == null ? EleFXNotificationPosition.TOP_RIGHT : v);
    }

    public boolean isShowClose() {
        return showClose.get();
    }

    public BooleanProperty showCloseProperty() {
        return showClose;
    }

    public void setShowClose(boolean v) {
        showClose.set(v);
    }

    public double getOffset() {
        return offset.get();
    }

    public DoubleProperty offsetProperty() {
        return offset;
    }

    public void setOffset(double v) {
        offset.set(Math.max(0, v));
    }

    public boolean isProgress() {
        return progress.get();
    }

    public BooleanProperty progressProperty() {
        return progress;
    }

    public void setProgress(boolean v) {
        progress.set(v);
    }

    public String getProgressColor() {
        return progressColor.get();
    }

    public StringProperty progressColorProperty() {
        return progressColor;
    }

    public void setProgressColor(String v) {
        progressColor.set(v == null ? "" : v);
    }

    public boolean isPauseOnHover() {
        return pauseOnHover.get();
    }

    public BooleanProperty pauseOnHoverProperty() {
        return pauseOnHover;
    }

    public void setPauseOnHover(boolean v) {
        pauseOnHover.set(v);
    }

    public Runnable getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(Runnable v) {
        onClose.set(v);
    }

    public Runnable getOnClick() {
        return onClick.get();
    }

    public ObjectProperty<Runnable> onClickProperty() {
        return onClick;
    }

    public void setOnClick(Runnable v) {
        onClick.set(v);
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing() && !closing;
    }

    public void close() {
        if (closing) return;
        closing = true;
        stopTimer();
        if (stage == null || !stage.isShowing()) {
            dispose();
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(180), this);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(180), this);
        slide.setByX(getPosition().isLeft() ? -24 : 24);
        fade.setOnFinished(e -> {
            stage.hide();
            dispose();
        });
        fade.play();
        slide.play();
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.NOTIFICATION;
    }

    private void initialize() {
        getStyleClass().add("ele-notification");
        setPrefWidth(330);
        setMinWidth(330);
        setMaxWidth(330);
        iconBox.getStyleClass().add("ele-notification__icon");
        titleLabel.getStyleClass().add("ele-notification__title");
        messageLabel.getStyleClass().add("ele-notification__content");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(258);
        messageBox.getStyleClass().add("ele-notification__message");
        VBox content = new VBox(8, titleLabel, messageBox);
        content.getStyleClass().add("ele-notification__body");
        HBox main = new HBox(12, iconBox, content);
        main.setAlignment(Pos.TOP_LEFT);
        setCenter(main);
        closeButton.getStyleClass().add("ele-notification__close");
        closeButton.setGraphic(new EleFXIcon(EleFXIconType.CLOSE, 16));
        closeButton.setAccessibleText("Close notification");
        closeButton.setOnMouseClicked(e -> e.consume());
        closeButton.setOnAction(e -> close());
        setRight(closeButton);
        progressBar.getStyleClass().add("ele-notification__progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        setBottom(progressBar);
        title.addListener(o -> titleLabel.setText(getTitle()));
        message.addListener(o -> refreshMessage());
        messageNode.addListener(o -> refreshMessage());
        icon.addListener(o -> refreshIcon());
        type.addListener((o, old, value) -> {
            updateType(old, value);
            refreshIcon();
        });
        customClass.addListener(o -> refreshClasses());
        showClose.addListener(o -> refreshClose());
        progress.addListener(o -> refreshProgress());
        progressColor.addListener(o -> refreshProgressColor());
        setOnMouseClicked(e -> {
            if (e.getTarget() != closeButton) {
                Runnable action = getOnClick();
                if (action != null) action.run();
            }
        });
        setOnMouseEntered(e -> {
            if (isPauseOnHover()) pauseTimer();
        });
        setOnMouseExited(e -> {
            if (isPauseOnHover()) resumeTimer();
        });
        titleLabel.setText(getTitle());
        refreshMessage();
        refreshIcon();
        refreshClose();
        refreshProgress();
        refreshClasses();
        sceneBuilderIntegration();
    }

    private void apply(EleFXNotificationOptions o) {
        setTitle(o.getTitle());
        setMessage(o.getMessage());
        setMessageNode(o.getMessageNode());
        setType(o.getType());
        setIcon(o.getIcon());
        setCustomClass(o.getCustomClass());
        setDuration(o.getDuration());
        setPosition(o.getPosition());
        setShowClose(o.isShowClose());
        setOffset(o.getOffset());
        setProgress(o.isProgress());
        setProgressColor(o.getProgressColor());
        setPauseOnHover(o.isPauseOnHover());
        setOnClose(o.getOnClose());
        setOnClick(o.getOnClick());
    }

    private void open() {
        stage = new Stage(StageStyle.TRANSPARENT);
        Scene scene = new Scene(new StackPane(this));
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.NOTIFICATION.toData());
        stage.setScene(scene);
        if (stackKey.owner != null) stage.initOwner(stackKey.owner);
        stage.setAlwaysOnTop(true);
        stage.show();
        positionAll(stackKey);
        setOpacity(0);
        setTranslateX(getPosition().isLeft() ? -24 : 24);
        FadeTransition fade = new FadeTransition(Duration.millis(220), this);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), this);
        slide.setToX(0);
        fade.play();
        slide.play();
        restartTimer();
    }

    private void restartTimer() {
        stopTimer();
        remainingMillis = getDuration();
        progressBar.setProgress(1);
        if (getDuration() > 0) {
            progressTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1)),
                    new KeyFrame(Duration.millis(getDuration()), new KeyValue(progressBar.progressProperty(), 0)));
        }
        resumeTimer();
    }

    private void resumeTimer() {
        if (!isShowing() || remainingMillis <= 0 || getDuration() <= 0 || closing) return;
        timerStartedAt = System.nanoTime();
        timeout = new PauseTransition(Duration.millis(remainingMillis));
        timeout.setOnFinished(e -> close());
        timeout.play();
        if (progressTimeline != null) progressTimeline.play();
    }

    private void pauseTimer() {
        if (timeout == null) return;
        long elapsed = (System.nanoTime() - timerStartedAt) / 1_000_000;
        remainingMillis = Math.max(0, remainingMillis - elapsed);
        timeout.stop();
        timeout = null;
        if (progressTimeline != null) progressTimeline.pause();
    }

    private void stopTimer() {
        if (timeout != null) timeout.stop();
        timeout = null;
        if (progressTimeline != null) progressTimeline.stop();
        progressTimeline = null;
    }

    private void dispose() {
        Runnable callback = getOnClose();
        if (callback != null) callback.run();
        synchronized (INSTANCES) {
            List<EleFXNotification> list = INSTANCES.get(stackKey);
            if (list != null) {
                list.remove(this);
                if (list.isEmpty()) INSTANCES.remove(stackKey);
            }
        }
        if (stackKey != null) positionAll(stackKey);
    }

    private static void positionAll(StackKey key) {
        List<EleFXNotification> list;
        synchronized (INSTANCES) {
            list = new ArrayList<>(INSTANCES.getOrDefault(key, List.of()));
        }
        double cursor = EDGE_MARGIN + (list.isEmpty() ? 0 : list.get(0).getOffset());
        for (EleFXNotification n : list) {
            if (n.stage == null || !n.stage.isShowing()) continue;
            Rectangle2D area = key.owner == null
                    ? Screen.getPrimary().getVisualBounds()
                    : new Rectangle2D(key.owner.getX(), key.owner.getY(), key.owner.getWidth(), key.owner.getHeight());
            double x = key.position.isLeft()
                    ? area.getMinX() + EDGE_MARGIN
                    : area.getMaxX() - n.stage.getWidth() - EDGE_MARGIN;
            double y = key.position.isBottom()
                    ? area.getMaxY() - cursor - n.stage.getHeight()
                    : area.getMinY() + cursor;
            n.stage.setX(x);
            n.stage.setY(y);
            cursor += n.stage.getHeight() + GAP;
        }
    }

    private void refreshMessage() {
        messageLabel.setText(getMessage());
        messageBox.getChildren().setAll(getMessageNode() == null ? messageLabel : getMessageNode());
    }

    private void refreshIcon() {
        Node value = getIcon();
        if (value == null && getType() != null) value = new EleFXIcon(switch (getType()) {
            case PRIMARY, INFO -> EleFXIconType.INFO_FILLED;
            case SUCCESS -> EleFXIconType.CIRCLE_CHECK_FILLED;
            case WARNING -> EleFXIconType.WARNING_FILLED;
            case ERROR -> EleFXIconType.CIRCLE_CLOSE_FILLED;
        }, 24);
        iconBox.getChildren().setAll(value == null ? List.of() : List.of(value));
        iconBox.setManaged(value != null);
        iconBox.setVisible(value != null);
    }

    private void refreshClose() {
        closeButton.setVisible(isShowClose());
        closeButton.setManaged(isShowClose());
    }

    private void refreshProgress() {
        progressBar.setVisible(isProgress() && getDuration() > 0);
        progressBar.setManaged(isProgress() && getDuration() > 0);
    }

    private void refreshProgressColor() {
        progressBar.setStyle(getProgressColor().isBlank() ? "" : "-fx-accent: " + getProgressColor() + ";");
    }

    private void refreshClasses() {
        getStyleClass().removeAll(appliedClasses);
        appliedClasses.clear();
        for (String c : getCustomClass().trim().split("\\s+"))
            if (!c.isBlank()) appliedClasses.add(c);
        getStyleClass().addAll(appliedClasses);
    }

    private void updateType(EleFXNotificationType old, EleFXNotificationType value) {
        if (old != null) getStyleClass().remove(old.styleClass());
        if (value != null && !getStyleClass().contains(value.styleClass())) getStyleClass().add(value.styleClass());
    }

    private record StackKey(Window owner, EleFXNotificationPosition position) {
    }
}
