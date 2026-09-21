package com.sjydvlp.elefx.component.message;

import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.*;

/**
 * Element Plus-inspired transient feedback message.
 *
 * <p>
 * Call {@link #show(String)} or {@link #show(EleFXMessageOptions)} to create a window-owned
 * toast. The returned object can be closed manually. Messages in a placement automatically stack,
 * and grouped string messages increment a visible repeat badge.
 * </p>
 */
public class EleFXMessage extends HBox implements Themable {

    private static final Map<StackKey, List<EleFXMessage>> INSTANCES = new HashMap<>();

    private static final double GAP = 16;

    private final StringProperty message = new SimpleStringProperty(this, "message", "");

    private final ObjectProperty<Node> messageNode = new SimpleObjectProperty<>(this, "messageNode");

    private final ObjectProperty<EleFXMessageType> type = new SimpleObjectProperty<>(this, "type",
            EleFXMessageType.INFO);

    private final BooleanProperty plain = new SimpleBooleanProperty(this, "plain", false);

    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>(this, "icon");

    private final BooleanProperty dangerouslyUseHTMLString = new SimpleBooleanProperty(this, "dangerouslyUseHTMLString",
            false);

    private final StringProperty customClass = new SimpleStringProperty(this, "customClass", "");

    private final IntegerProperty duration = new SimpleIntegerProperty(this, "duration", 3000);

    private final BooleanProperty showClose = new SimpleBooleanProperty(this, "showClose", false);

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 16);

    private final ObjectProperty<EleFXMessagePlacement> placement = new SimpleObjectProperty<>(this, "placement",
            EleFXMessagePlacement.TOP);

    private final IntegerProperty repeatNum = new SimpleIntegerProperty(this, "repeatNum", 1);

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final StackPane iconBox = new StackPane();

    private final StackPane contentBox = new StackPane();

    private final Label text = new Label();

    private final Label repeat = new Label();

    private final Button close = new Button();

    private final Set<String> appliedClasses = new HashSet<>();

    private PauseTransition timeout;

    private Stage stage;

    private StackKey stackKey;

    private boolean closing;

    public EleFXMessage() {
        initialize();
    }

    public EleFXMessage(String value) {
        this();
        setMessage(value);
    }

    public static EleFXMessage show(String value) {
        return show(new EleFXMessageOptions().message(value));
    }

    public static EleFXMessage primary(String value) {
        return show(new EleFXMessageOptions().message(value).type(EleFXMessageType.PRIMARY));
    }

    public static EleFXMessage success(String value) {
        return show(new EleFXMessageOptions().message(value).type(EleFXMessageType.SUCCESS));
    }

    public static EleFXMessage info(String value) {
        return show(new EleFXMessageOptions().message(value).type(EleFXMessageType.INFO));
    }

    public static EleFXMessage warning(String value) {
        return show(new EleFXMessageOptions().message(value).type(EleFXMessageType.WARNING));
    }

    public static EleFXMessage error(String value) {
        return show(new EleFXMessageOptions().message(value).type(EleFXMessageType.ERROR));
    }

    public static EleFXMessage show(EleFXMessageOptions options) {
        EleFXMessageOptions o = options == null ? new EleFXMessageOptions() : options;
        StackKey key = new StackKey(o.getOwner(), o.getPlacement());
        synchronized (INSTANCES) {
            List<EleFXMessage> messages = INSTANCES.computeIfAbsent(key, ignored -> new ArrayList<>());
            if (o.isGrouping() && o.getMessageNode() == null)
                for (EleFXMessage existing : messages)
                if (existing.getMessage().equals(o.getMessage())) {
                    existing.setRepeatNum(existing.getRepeatNum() + o.getRepeatNum());
                    existing.restartTimer();
                    return existing;
                }
            EleFXMessage result = new EleFXMessage();
            result.apply(o);
            result.stackKey = key;
            messages.add(result);
            result.open();
            return result;
        }
    }

    public static void closeAll() {
        for (EleFXMessage m : snapshot())
            m.close();
    }

    public static void closeAll(EleFXMessagePlacement placement) {
        for (EleFXMessage m : snapshot())
            if (m.getPlacement() == placement) m.close();
    }

    private static List<EleFXMessage> snapshot() {
        synchronized (INSTANCES) {
            return INSTANCES.values().stream().flatMap(Collection::stream).toList();
        }
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

    public EleFXMessageType getType() {
        return type.get();
    }

    public ObjectProperty<EleFXMessageType> typeProperty() {
        return type;
    }

    public void setType(EleFXMessageType v) {
        type.set(v == null ? EleFXMessageType.INFO : v);
    }

    public boolean isPlain() {
        return plain.get();
    }

    public BooleanProperty plainProperty() {
        return plain;
    }

    public void setPlain(boolean v) {
        plain.set(v);
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

    public boolean isDangerouslyUseHTMLString() {
        return dangerouslyUseHTMLString.get();
    }

    public BooleanProperty dangerouslyUseHTMLStringProperty() {
        return dangerouslyUseHTMLString;
    }

    public void setDangerouslyUseHTMLString(boolean v) {
        dangerouslyUseHTMLString.set(v);
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

    public EleFXMessagePlacement getPlacement() {
        return placement.get();
    }

    public ObjectProperty<EleFXMessagePlacement> placementProperty() {
        return placement;
    }

    public void setPlacement(EleFXMessagePlacement v) {
        placement.set(v == null ? EleFXMessagePlacement.TOP : v);
    }

    public int getRepeatNum() {
        return repeatNum.get();
    }

    public IntegerProperty repeatNumProperty() {
        return repeatNum;
    }

    public void setRepeatNum(int v) {
        repeatNum.set(Math.max(1, v));
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

    public boolean isShowing() {
        return stage != null && stage.isShowing() && !closing;
    }

    /** Closes this instance and compacts the remaining message stack. */
    public void close() {
        if (closing) return;
        closing = true;
        if (timeout != null) timeout.stop();
        if (stage == null || !stage.isShowing()) {
            dispose();
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(180), this);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(180), this);
        slide.setByY(getPlacement().isBottom() ? 12 : -12);
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
        return EleFXThemes.MESSAGE;
    }

    private void apply(EleFXMessageOptions o) {
        setMessage(o.getMessage());
        setMessageNode(o.getMessageNode());
        setType(o.getType());
        setPlain(o.isPlain());
        setIcon(o.getIcon());
        setDangerouslyUseHTMLString(o.isDangerouslyUseHTMLString());
        setCustomClass(o.getCustomClass());
        setDuration(o.getDuration());
        setShowClose(o.isShowClose());
        setOffset(o.getOffset());
        setPlacement(o.getPlacement());
        setRepeatNum(o.getRepeatNum());
        setOnClose(o.getOnClose());
    }

    private void initialize() {
        getStyleClass().add("ele-message");
        setAlignment(Pos.CENTER_LEFT);
        setFillHeight(false);
        iconBox.getStyleClass().add("ele-message__icon");
        contentBox.getStyleClass().add("ele-message__content");
        text.getStyleClass().add("ele-message__text");
        text.setWrapText(true);
        text.setMaxWidth(360);
        repeat.getStyleClass().add("ele-message__repeat");
        close.getStyleClass().add("ele-message__close");
        close.setAccessibleText("Close message");
        close.setGraphic(new EleFXIcon(EleFXIconType.CLOSE, 16));
        close.setOnAction(e -> close());
        getChildren().addAll(iconBox, contentBox, repeat, close);
        message.addListener(o -> refreshContent());
        messageNode.addListener(o -> refreshContent());
        dangerouslyUseHTMLString.addListener(o -> refreshContent());
        icon.addListener(o -> refreshIcon());
        type.addListener((o, old, value) -> {
            updateType(old, value);
            refreshIcon();
        });
        plain.addListener((o, old, value) -> style(this, value, "ele-message--plain"));
        customClass.addListener(o -> refreshClasses());
        showClose.addListener(o -> refreshClose());
        repeatNum.addListener(o -> refreshRepeat());
        updateType(null, getType());
        refreshContent();
        refreshIcon();
        refreshClose();
        refreshRepeat();
        refreshClasses();
        sceneBuilderIntegration();
    }

    private void open() {
        stage = new Stage(StageStyle.TRANSPARENT);
        Scene scene = new Scene(new StackPane(this));
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
        scene.getStylesheets().add(EleFXThemes.MESSAGE.toData());
        stage.setScene(scene);
        Window owner = stackKey.owner;
        if (owner != null) stage.initOwner(owner);
        stage.setAlwaysOnTop(true);
        stage.show();
        positionAll(stackKey);
        restartTimer();
    }

    private void restartTimer() {
        if (timeout != null) timeout.stop();
        if (isShowing() && getDuration() > 0) {
            timeout = new PauseTransition(Duration.millis(getDuration()));
            timeout.setOnFinished(e -> close());
            timeout.play();
        }
    }

    private void dispose() {
        Runnable callback = getOnClose();
        if (callback != null) callback.run();
        StackKey key = stackKey;
        synchronized (INSTANCES) {
            List<EleFXMessage> messages = INSTANCES.get(key);
            if (messages != null) {
                messages.remove(this);
                if (messages.isEmpty()) INSTANCES.remove(key);
            }
        }
        if (key != null) positionAll(key);
    }

    private static void positionAll(StackKey key) {
        List<EleFXMessage> copy;
        synchronized (INSTANCES) {
            copy = new ArrayList<>(INSTANCES.getOrDefault(key, List.of()));
        }
        double edge = copy.isEmpty() ? 0 : copy.get(0).getOffset();
        double cursor = edge;
        for (EleFXMessage message : copy) {
            if (message.stage == null || !message.stage.isShowing()) continue;
            double width = message.stage.getWidth(), height = message.stage.getHeight();
            var area = key.owner == null
                    ? Screen.getPrimary().getVisualBounds()
                    : new javafx.geometry.Rectangle2D(key.owner.getX(), key.owner.getY(), key.owner.getWidth(),
                            key.owner.getHeight());
            double x = key.placement.isLeft()
                    ? area.getMinX() + edge
                    : key.placement.isRight()
                            ? area.getMaxX() - width - edge
                            : area.getMinX() + (area.getWidth() - width) / 2;
            double y = key.placement.isBottom() ? area.getMaxY() - cursor - height : area.getMinY() + cursor;
            message.stage.setX(x);
            message.stage.setY(y);
            cursor += height + GAP;
        }
    }

    private void refreshContent() {
        text.setText(isDangerouslyUseHTMLString() ? getMessage().replaceAll("<[^>]*>", "") : getMessage());
        contentBox.getChildren().setAll(getMessageNode() == null ? text : getMessageNode());
    }

    private void refreshIcon() {
        Node value = getIcon() == null ? new EleFXIcon(switch (getType()) {
            case PRIMARY, INFO -> EleFXIconType.INFO_FILLED;
            case SUCCESS -> EleFXIconType.CIRCLE_CHECK_FILLED;
            case WARNING -> EleFXIconType.WARNING_FILLED;
            case ERROR -> EleFXIconType.CIRCLE_CLOSE_FILLED;
        }, 16) : getIcon();
        iconBox.getChildren().setAll(value);
    }

    private void refreshClose() {
        close.setVisible(isShowClose());
        close.setManaged(isShowClose());
    }

    private void refreshRepeat() {
        boolean show = getRepeatNum() > 1;
        repeat.setText(String.valueOf(getRepeatNum()));
        repeat.setVisible(show);
        repeat.setManaged(show);
    }

    private void refreshClasses() {
        getStyleClass().removeAll(appliedClasses);
        appliedClasses.clear();
        for (String c : getCustomClass().trim().split("\\s+"))
            if (!c.isBlank()) appliedClasses.add(c);
        getStyleClass().addAll(appliedClasses);
    }

    private void updateType(EleFXMessageType old, EleFXMessageType value) {
        if (old != null) getStyleClass().remove(old.styleClass());
        String c = (value == null ? EleFXMessageType.INFO : value).styleClass();
        if (!getStyleClass().contains(c)) getStyleClass().add(c);
    }

    private static void style(Node node, boolean active, String value) {
        if (active && !node.getStyleClass().contains(value)) node.getStyleClass().add(value);
        if (!active) node.getStyleClass().remove(value);
    }

    private record StackKey(Window owner, EleFXMessagePlacement placement) {
    }
}
