package com.sjydvlp.elefx.component.messagebox;

import com.sjydvlp.elefx.component.button.EleFXButtonSize;
import com.sjydvlp.elefx.component.button.EleFXButtonType;
import javafx.scene.Node;
import javafx.stage.Window;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** Fluent options for {@link EleFXMessageBox}. */
public final class EleFXMessageBoxOptions {

    private String title = "";

    private String message = "";

    private Node messageNode;

    private boolean dangerouslyUseHTMLString;

    private EleFXMessageBoxType type;

    private Node icon;

    private Node closeIcon;

    private String customClass = "";

    private String customStyle = "";

    private boolean modal = true;

    private String modalClass = "";

    private boolean showClose = true;

    private boolean closeOnClickModal = true;

    private boolean closeOnPressEscape = true;

    private boolean distinguishCancelAndClose;

    private boolean showCancelButton;

    private boolean showConfirmButton = true;

    private String cancelButtonText = "Cancel";

    private String confirmButtonText = "OK";

    private EleFXButtonType cancelButtonType = EleFXButtonType.DEFAULT;

    private EleFXButtonType confirmButtonType = EleFXButtonType.PRIMARY;

    private String cancelButtonClass = "";

    private String confirmButtonClass = "";

    private boolean cancelButtonLoading;

    private boolean confirmButtonLoading;

    private Node cancelButtonLoadingIcon;

    private Node confirmButtonLoadingIcon;

    private boolean showInput;

    private String inputPlaceholder = "";

    private String inputValue = "";

    private Pattern inputPattern;

    private EleFXMessageBoxInputValidator inputValidator;

    private String inputErrorMessage = "Illegal input";

    private boolean center;

    private boolean draggable;

    private boolean overflow;

    private boolean roundButton;

    private EleFXButtonSize buttonSize = EleFXButtonSize.DEFAULT;

    private boolean autofocus = true;

    private Window owner;

    private Consumer<EleFXMessageBoxResult> callback;

    private EleFXMessageBox.BeforeCloseHandler beforeClose;

    public String getTitle() {
        return title;
    }

    public EleFXMessageBoxOptions title(String v) {
        title = text(v);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public EleFXMessageBoxOptions message(String v) {
        message = text(v);
        return this;
    }

    public Node getMessageNode() {
        return messageNode;
    }

    public EleFXMessageBoxOptions messageNode(Node v) {
        messageNode = v;
        return this;
    }

    public boolean isDangerouslyUseHTMLString() {
        return dangerouslyUseHTMLString;
    }

    public EleFXMessageBoxOptions dangerouslyUseHTMLString(boolean v) {
        dangerouslyUseHTMLString = v;
        return this;
    }

    public EleFXMessageBoxType getType() {
        return type;
    }

    public EleFXMessageBoxOptions type(EleFXMessageBoxType v) {
        type = v;
        return this;
    }

    public Node getIcon() {
        return icon;
    }

    public EleFXMessageBoxOptions icon(Node v) {
        icon = v;
        return this;
    }

    public Node getCloseIcon() {
        return closeIcon;
    }

    public EleFXMessageBoxOptions closeIcon(Node v) {
        closeIcon = v;
        return this;
    }

    public String getCustomClass() {
        return customClass;
    }

    public EleFXMessageBoxOptions customClass(String v) {
        customClass = text(v);
        return this;
    }

    public String getCustomStyle() {
        return customStyle;
    }

    public EleFXMessageBoxOptions customStyle(String v) {
        customStyle = text(v);
        return this;
    }

    public boolean isModal() {
        return modal;
    }

    public EleFXMessageBoxOptions modal(boolean v) {
        modal = v;
        return this;
    }

    public String getModalClass() {
        return modalClass;
    }

    public EleFXMessageBoxOptions modalClass(String v) {
        modalClass = text(v);
        return this;
    }

    public boolean isShowClose() {
        return showClose;
    }

    public EleFXMessageBoxOptions showClose(boolean v) {
        showClose = v;
        return this;
    }

    public boolean isCloseOnClickModal() {
        return closeOnClickModal;
    }

    public EleFXMessageBoxOptions closeOnClickModal(boolean v) {
        closeOnClickModal = v;
        return this;
    }

    public boolean isCloseOnPressEscape() {
        return closeOnPressEscape;
    }

    public EleFXMessageBoxOptions closeOnPressEscape(boolean v) {
        closeOnPressEscape = v;
        return this;
    }

    public boolean isDistinguishCancelAndClose() {
        return distinguishCancelAndClose;
    }

    public EleFXMessageBoxOptions distinguishCancelAndClose(boolean v) {
        distinguishCancelAndClose = v;
        return this;
    }

    public boolean isShowCancelButton() {
        return showCancelButton;
    }

    public EleFXMessageBoxOptions showCancelButton(boolean v) {
        showCancelButton = v;
        return this;
    }

    public boolean isShowConfirmButton() {
        return showConfirmButton;
    }

    public EleFXMessageBoxOptions showConfirmButton(boolean v) {
        showConfirmButton = v;
        return this;
    }

    public String getCancelButtonText() {
        return cancelButtonText;
    }

    public EleFXMessageBoxOptions cancelButtonText(String v) {
        cancelButtonText = text(v);
        return this;
    }

    public String getConfirmButtonText() {
        return confirmButtonText;
    }

    public EleFXMessageBoxOptions confirmButtonText(String v) {
        confirmButtonText = text(v);
        return this;
    }

    public EleFXButtonType getCancelButtonType() {
        return cancelButtonType;
    }

    public EleFXMessageBoxOptions cancelButtonType(EleFXButtonType v) {
        cancelButtonType = v == null ? EleFXButtonType.DEFAULT : v;
        return this;
    }

    public EleFXButtonType getConfirmButtonType() {
        return confirmButtonType;
    }

    public EleFXMessageBoxOptions confirmButtonType(EleFXButtonType v) {
        confirmButtonType = v == null ? EleFXButtonType.PRIMARY : v;
        return this;
    }

    public String getCancelButtonClass() {
        return cancelButtonClass;
    }

    public EleFXMessageBoxOptions cancelButtonClass(String v) {
        cancelButtonClass = text(v);
        return this;
    }

    public String getConfirmButtonClass() {
        return confirmButtonClass;
    }

    public EleFXMessageBoxOptions confirmButtonClass(String v) {
        confirmButtonClass = text(v);
        return this;
    }

    public boolean isCancelButtonLoading() {
        return cancelButtonLoading;
    }

    public EleFXMessageBoxOptions cancelButtonLoading(boolean v) {
        cancelButtonLoading = v;
        return this;
    }

    public boolean isConfirmButtonLoading() {
        return confirmButtonLoading;
    }

    public EleFXMessageBoxOptions confirmButtonLoading(boolean v) {
        confirmButtonLoading = v;
        return this;
    }

    public Node getCancelButtonLoadingIcon() {
        return cancelButtonLoadingIcon;
    }

    public EleFXMessageBoxOptions cancelButtonLoadingIcon(Node v) {
        cancelButtonLoadingIcon = v;
        return this;
    }

    public Node getConfirmButtonLoadingIcon() {
        return confirmButtonLoadingIcon;
    }

    public EleFXMessageBoxOptions confirmButtonLoadingIcon(Node v) {
        confirmButtonLoadingIcon = v;
        return this;
    }

    public boolean isShowInput() {
        return showInput;
    }

    public EleFXMessageBoxOptions showInput(boolean v) {
        showInput = v;
        return this;
    }

    public String getInputPlaceholder() {
        return inputPlaceholder;
    }

    public EleFXMessageBoxOptions inputPlaceholder(String v) {
        inputPlaceholder = text(v);
        return this;
    }

    public String getInputValue() {
        return inputValue;
    }

    public EleFXMessageBoxOptions inputValue(String v) {
        inputValue = text(v);
        return this;
    }

    public Pattern getInputPattern() {
        return inputPattern;
    }

    public EleFXMessageBoxOptions inputPattern(Pattern v) {
        inputPattern = v;
        return this;
    }

    public EleFXMessageBoxInputValidator getInputValidator() {
        return inputValidator;
    }

    public EleFXMessageBoxOptions inputValidator(EleFXMessageBoxInputValidator v) {
        inputValidator = v;
        return this;
    }

    public String getInputErrorMessage() {
        return inputErrorMessage;
    }

    public EleFXMessageBoxOptions inputErrorMessage(String v) {
        inputErrorMessage = text(v);
        return this;
    }

    public boolean isCenter() {
        return center;
    }

    public EleFXMessageBoxOptions center(boolean v) {
        center = v;
        return this;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public EleFXMessageBoxOptions draggable(boolean v) {
        draggable = v;
        return this;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public EleFXMessageBoxOptions overflow(boolean v) {
        overflow = v;
        return this;
    }

    public boolean isRoundButton() {
        return roundButton;
    }

    public EleFXMessageBoxOptions roundButton(boolean v) {
        roundButton = v;
        return this;
    }

    public EleFXButtonSize getButtonSize() {
        return buttonSize;
    }

    public EleFXMessageBoxOptions buttonSize(EleFXButtonSize v) {
        buttonSize = v == null ? EleFXButtonSize.DEFAULT : v;
        return this;
    }

    public boolean isAutofocus() {
        return autofocus;
    }

    public EleFXMessageBoxOptions autofocus(boolean v) {
        autofocus = v;
        return this;
    }

    public Window getOwner() {
        return owner;
    }

    public EleFXMessageBoxOptions owner(Window v) {
        owner = v;
        return this;
    }

    public Consumer<EleFXMessageBoxResult> getCallback() {
        return callback;
    }

    public EleFXMessageBoxOptions callback(Consumer<EleFXMessageBoxResult> v) {
        callback = v;
        return this;
    }

    public EleFXMessageBox.BeforeCloseHandler getBeforeClose() {
        return beforeClose;
    }

    public EleFXMessageBoxOptions beforeClose(EleFXMessageBox.BeforeCloseHandler v) {
        beforeClose = v;
        return this;
    }

    private static String text(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}
