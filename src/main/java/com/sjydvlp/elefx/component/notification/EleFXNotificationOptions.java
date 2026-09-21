package com.sjydvlp.elefx.component.notification;

import javafx.scene.Node;
import javafx.stage.Window;

/** Fluent options for {@link EleFXNotification#show(EleFXNotificationOptions)}. */
public final class EleFXNotificationOptions {

    private String title = "";

    private String message = "";

    private Node messageNode;

    private EleFXNotificationType type;

    private Node icon;

    private String customClass = "";

    private int duration = 4500;

    private EleFXNotificationPosition position = EleFXNotificationPosition.TOP_RIGHT;

    private boolean showClose = true;

    private Runnable onClose;

    private Runnable onClick;

    private double offset;

    private Window owner;

    private boolean progress;

    private String progressColor = "";

    private boolean pauseOnHover = true;

    public String getTitle() {
        return title;
    }

    public EleFXNotificationOptions title(String value) {
        title = value == null ? "" : value;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public EleFXNotificationOptions message(String value) {
        message = value == null ? "" : value;
        return this;
    }

    public Node getMessageNode() {
        return messageNode;
    }

    public EleFXNotificationOptions messageNode(Node value) {
        messageNode = value;
        return this;
    }

    public EleFXNotificationType getType() {
        return type;
    }

    public EleFXNotificationOptions type(EleFXNotificationType value) {
        type = value;
        return this;
    }

    public Node getIcon() {
        return icon;
    }

    public EleFXNotificationOptions icon(Node value) {
        icon = value;
        return this;
    }

    public String getCustomClass() {
        return customClass;
    }

    public EleFXNotificationOptions customClass(String value) {
        customClass = value == null ? "" : value;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public EleFXNotificationOptions duration(int value) {
        duration = Math.max(0, value);
        return this;
    }

    public EleFXNotificationPosition getPosition() {
        return position;
    }

    public EleFXNotificationOptions position(EleFXNotificationPosition value) {
        position = value == null ? EleFXNotificationPosition.TOP_RIGHT : value;
        return this;
    }

    public boolean isShowClose() {
        return showClose;
    }

    public EleFXNotificationOptions showClose(boolean value) {
        showClose = value;
        return this;
    }

    public Runnable getOnClose() {
        return onClose;
    }

    public EleFXNotificationOptions onClose(Runnable value) {
        onClose = value;
        return this;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    public EleFXNotificationOptions onClick(Runnable value) {
        onClick = value;
        return this;
    }

    public double getOffset() {
        return offset;
    }

    public EleFXNotificationOptions offset(double value) {
        offset = Math.max(0, value);
        return this;
    }

    public Window getOwner() {
        return owner;
    }

    public EleFXNotificationOptions owner(Window value) {
        owner = value;
        return this;
    }

    public boolean isProgress() {
        return progress;
    }

    public EleFXNotificationOptions progress(boolean value) {
        progress = value;
        return this;
    }

    public String getProgressColor() {
        return progressColor;
    }

    public EleFXNotificationOptions progressColor(String value) {
        progressColor = value == null ? "" : value;
        return this;
    }

    public boolean isPauseOnHover() {
        return pauseOnHover;
    }

    public EleFXNotificationOptions pauseOnHover(boolean value) {
        pauseOnHover = value;
        return this;
    }
}
