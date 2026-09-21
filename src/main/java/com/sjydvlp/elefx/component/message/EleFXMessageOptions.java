package com.sjydvlp.elefx.component.message;

import javafx.scene.Node;
import javafx.stage.Window;

/**
 * Options used by {@link EleFXMessage#show(EleFXMessageOptions)}.
 *
 * <p>
 * The optional owner replaces Element Plus's browser-only {@code appendTo}: messages are
 * positioned within that JavaFX window. All setters return this object for concise use.
 * </p>
 */
public final class EleFXMessageOptions {

    private String message = "";

    private Node messageNode;

    private EleFXMessageType type = EleFXMessageType.INFO;

    private boolean plain;

    private Node icon;

    private boolean dangerouslyUseHTMLString;

    private String customClass = "";

    private int duration = 3000;

    private boolean showClose;

    private Runnable onClose;

    private double offset = 16;

    private EleFXMessagePlacement placement = EleFXMessagePlacement.TOP;

    private Window owner;

    private boolean grouping;

    private int repeatNum = 1;

    public String getMessage() {
        return message;
    }

    public EleFXMessageOptions message(String value) {
        message = value == null ? "" : value;
        return this;
    }

    public Node getMessageNode() {
        return messageNode;
    }

    public EleFXMessageOptions messageNode(Node value) {
        messageNode = value;
        return this;
    }

    public EleFXMessageType getType() {
        return type;
    }

    public EleFXMessageOptions type(EleFXMessageType value) {
        type = value == null ? EleFXMessageType.INFO : value;
        return this;
    }

    public boolean isPlain() {
        return plain;
    }

    public EleFXMessageOptions plain(boolean value) {
        plain = value;
        return this;
    }

    public Node getIcon() {
        return icon;
    }

    public EleFXMessageOptions icon(Node value) {
        icon = value;
        return this;
    }

    public boolean isDangerouslyUseHTMLString() {
        return dangerouslyUseHTMLString;
    }

    public EleFXMessageOptions dangerouslyUseHTMLString(boolean value) {
        dangerouslyUseHTMLString = value;
        return this;
    }

    public String getCustomClass() {
        return customClass;
    }

    public EleFXMessageOptions customClass(String value) {
        customClass = value == null ? "" : value;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public EleFXMessageOptions duration(int value) {
        duration = Math.max(0, value);
        return this;
    }

    public boolean isShowClose() {
        return showClose;
    }

    public EleFXMessageOptions showClose(boolean value) {
        showClose = value;
        return this;
    }

    public Runnable getOnClose() {
        return onClose;
    }

    public EleFXMessageOptions onClose(Runnable value) {
        onClose = value;
        return this;
    }

    public double getOffset() {
        return offset;
    }

    public EleFXMessageOptions offset(double value) {
        offset = Math.max(0, value);
        return this;
    }

    public EleFXMessagePlacement getPlacement() {
        return placement;
    }

    public EleFXMessageOptions placement(EleFXMessagePlacement value) {
        placement = value == null ? EleFXMessagePlacement.TOP : value;
        return this;
    }

    public Window getOwner() {
        return owner;
    }

    public EleFXMessageOptions owner(Window value) {
        owner = value;
        return this;
    }

    public boolean isGrouping() {
        return grouping;
    }

    public EleFXMessageOptions grouping(boolean value) {
        grouping = value;
        return this;
    }

    public int getRepeatNum() {
        return repeatNum;
    }

    public EleFXMessageOptions repeatNum(int value) {
        repeatNum = Math.max(1, value);
        return this;
    }
}
