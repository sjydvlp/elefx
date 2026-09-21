package com.sjydvlp.elefx.component.messagebox;

import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Element Plus inspired system-message-box overlay.
 *
 * <p>
 * Use {@link #alert(String, String)}, {@link #confirm(String, String)},
 * {@link #prompt(String, String)}, or {@link #show(EleFXMessageBoxOptions)}. Every factory
 * returns a {@link CompletableFuture}; the completion value includes both the action and prompt
 * input. For a callback-oriented flow configure {@link EleFXMessageBoxOptions#callback(Consumer)}.
 * </p>
 */
public final class EleFXMessageBox {

    @FunctionalInterface
    public interface BeforeCloseHandler {

        /** Call {@code done} when asynchronous work has finished and the box may close. */
        void handle(EleFXMessageBoxAction action, EleFXMessageBox instance, Runnable done);
    }

    private final EleFXMessageBoxOptions options;

    private final CompletableFuture<EleFXMessageBoxResult> future = new CompletableFuture<>();

    private final VBox box = new VBox();

    private final HBox header = new HBox();

    private final HBox content = new HBox();

    private final VBox messageArea = new VBox();

    private final HBox footer = new HBox();

    private final Label title = new Label();

    private final Label message = new Label();

    private final Label inputError = new Label();

    private final StackPane iconArea = new StackPane();

    private final Button closeButton = new Button();

    private final TextField input = new TextField();

    private final EleFXButton cancelButton = new EleFXButton();

    private final EleFXButton confirmButton = new EleFXButton();

    private final List<String> customClasses = new ArrayList<>();

    private Stage stage;

    private StackPane overlay;

    private boolean closing;

    private double dragAnchorX, dragAnchorY, boxAnchorX, boxAnchorY;

    private EleFXMessageBox(EleFXMessageBoxOptions options) {
        this.options = Objects.requireNonNull(options, "options");
        build();
    }

    public static CompletableFuture<EleFXMessageBoxResult> show(EleFXMessageBoxOptions options) {
        EleFXMessageBox box = new EleFXMessageBox(options == null ? new EleFXMessageBoxOptions() : options);
        box.open();
        return box.future();
    }

    public static CompletableFuture<EleFXMessageBoxResult> alert(String message, String title) {
        return alert(message, title, new EleFXMessageBoxOptions());
    }

    public static CompletableFuture<EleFXMessageBoxResult> alert(String message, String title,
                                                                 EleFXMessageBoxOptions options) {
        EleFXMessageBoxOptions resolved = options == null ? new EleFXMessageBoxOptions() : options;
        resolved.message(message).title(title).showCancelButton(false).closeOnClickModal(false)
                .closeOnPressEscape(false);
        return show(resolved);
    }

    public static CompletableFuture<EleFXMessageBoxResult> confirm(String message, String title) {
        return confirm(message, title, new EleFXMessageBoxOptions());
    }

    public static CompletableFuture<EleFXMessageBoxResult> confirm(String message, String title,
                                                                   EleFXMessageBoxOptions options) {
        EleFXMessageBoxOptions resolved = options == null ? new EleFXMessageBoxOptions() : options;
        resolved.message(message).title(title).showCancelButton(true);
        return show(resolved);
    }

    public static CompletableFuture<EleFXMessageBoxResult> prompt(String message, String title) {
        return prompt(message, title, new EleFXMessageBoxOptions());
    }

    public static CompletableFuture<EleFXMessageBoxResult> prompt(String message, String title,
                                                                  EleFXMessageBoxOptions options) {
        EleFXMessageBoxOptions resolved = options == null ? new EleFXMessageBoxOptions() : options;
        resolved.message(message).title(title).showCancelButton(true).showInput(true);
        return show(resolved);
    }

    public CompletableFuture<EleFXMessageBoxResult> future() {
        return future;
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }

    public String getInputValue() {
        return input.getText();
    }

    public void setInputValue(String value) {
        input.setText(Objects.requireNonNullElse(value, ""));
    }

    public TextField getInput() {
        return input;
    }

    public EleFXButton getConfirmButton() {
        return confirmButton;
    }

    public EleFXButton getCancelButton() {
        return cancelButton;
    }

    public void confirm() {
        requestClose(EleFXMessageBoxAction.CONFIRM);
    }

    public void cancel() {
        requestClose(EleFXMessageBoxAction.CANCEL);
    }

    public void close() {
        requestClose(EleFXMessageBoxAction.CLOSE);
    }

    public void setConfirmButtonLoading(boolean value) {
        confirmButton.setLoading(value);
    }

    public void setCancelButtonLoading(boolean value) {
        cancelButton.setLoading(value);
    }

    private void build() {
        box.getStyleClass().add("ele-message-box");
        box.setMaxWidth(420);
        box.setPrefWidth(420);
        box.setFillWidth(true);
        if (!options.getCustomStyle().isBlank()) box.setStyle(options.getCustomStyle());
        applyCustomClasses(box, options.getCustomClass(), customClasses);

        header.getStyleClass().add("ele-message-box__header");
        header.setAlignment(Pos.CENTER_LEFT);
        title.getStyleClass().add("ele-message-box__title");
        title.setText(options.getTitle());
        HBox.setHgrow(title, Priority.ALWAYS);
        closeButton.getStyleClass().add("ele-message-box__headerbtn");
        closeButton.setAccessibleText("Close message box");
        closeButton.setGraphic(
                options.getCloseIcon() == null ? new EleFXIcon(EleFXIconType.CLOSE, 18) : options.getCloseIcon());
        closeButton.setOnAction(e -> close());
        closeButton.setVisible(options.isShowClose());
        closeButton.setManaged(options.isShowClose());
        header.getChildren().addAll(title, closeButton);

        content.getStyleClass().add("ele-message-box__content");
        content.setAlignment(options.isCenter() ? Pos.CENTER : Pos.TOP_LEFT);
        iconArea.getStyleClass().add("ele-message-box__status");
        refreshIcon();
        iconArea.setVisible(options.getIcon() != null || options.getType() != null);
        iconArea.setManaged(iconArea.isVisible());
        messageArea.getStyleClass().add("ele-message-box__message-area");
        messageArea.setAlignment(options.isCenter() ? Pos.CENTER : Pos.CENTER_LEFT);
        message.getStyleClass().add("ele-message-box__message");
        message.setWrapText(true);
        message.setText(options.isDangerouslyUseHTMLString()
                ? options.getMessage().replaceAll("<[^>]*>", "")
                : options.getMessage());
        Node suppliedMessage = options.getMessageNode();
        messageArea.getChildren().add(suppliedMessage == null ? message : suppliedMessage);
        input.getStyleClass().add("ele-message-box__input");
        input.setPromptText(options.getInputPlaceholder());
        input.setText(options.getInputValue());
        input.setVisible(options.isShowInput());
        input.setManaged(options.isShowInput());
        input.setOnAction(e -> confirm());
        inputError.getStyleClass().add("ele-message-box__input-error");
        inputError.setManaged(false);
        inputError.setVisible(false);
        messageArea.getChildren().addAll(input, inputError);
        HBox.setHgrow(messageArea, Priority.ALWAYS);
        content.getChildren().addAll(iconArea, messageArea);

        footer.getStyleClass().add("ele-message-box__btns");
        footer.setAlignment(options.isCenter() ? Pos.CENTER : Pos.CENTER_RIGHT);
        configureButton(cancelButton, options.getCancelButtonText(), options.getCancelButtonType(),
                options.getCancelButtonClass(),
                options.isCancelButtonLoading(), options.getCancelButtonLoadingIcon());
        configureButton(confirmButton, options.getConfirmButtonText(), options.getConfirmButtonType(),
                options.getConfirmButtonClass(),
                options.isConfirmButtonLoading(), options.getConfirmButtonLoadingIcon());
        cancelButton.setOnAction(e -> cancel());
        confirmButton.setOnAction(e -> confirm());
        cancelButton.setVisible(options.isShowCancelButton());
        cancelButton.setManaged(options.isShowCancelButton());
        confirmButton.setVisible(options.isShowConfirmButton());
        confirmButton.setManaged(options.isShowConfirmButton());
        footer.getChildren().addAll(cancelButton, confirmButton);
        footer.setVisible(options.isShowCancelButton() || options.isShowConfirmButton());
        footer.setManaged(footer.isVisible());
        box.getChildren().addAll(header, content, footer);
        if (options.isCenter()) box.getStyleClass().add("ele-message-box--center");
        if (options.isDraggable()) installDragging();
    }

    private void configureButton(EleFXButton button, String text,
                                 com.sjydvlp.elefx.component.button.EleFXButtonType type,
                                 String styleClass, boolean loading, Node loadingGraphic) {
        button.setText(text);
        button.setType(type);
        button.setSize(options.getButtonSize());
        button.setRound(options.isRoundButton());
        button.setLoadingGraphic(loadingGraphic);
        button.setLoading(loading);
        applyCustomClasses(button, styleClass, new ArrayList<>());
    }

    private void open() {
        Runnable opener = () -> {
            if (isShowing()) return;
            stage = new Stage(StageStyle.TRANSPARENT);
            if (options.getOwner() != null) stage.initOwner(options.getOwner());
            if (options.isModal()) stage.initModality(Modality.WINDOW_MODAL);
            overlay = new StackPane(box);
            overlay.getStyleClass().add("ele-message-box__wrapper");
            if (options.isModal()) overlay.getStyleClass().add("ele-message-box__wrapper--modal");
            applyCustomClasses(overlay, options.getModalClass(), new ArrayList<>());
            overlay.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getTarget() == overlay && options.isCloseOnClickModal()) close();
            });
            var viewport = viewport();
            Scene scene = new Scene(overlay, viewport.getWidth(), viewport.getHeight(), Color.TRANSPARENT);
            scene.getStylesheets().add(EleFXThemes.DEFAULT.toData());
            scene.getStylesheets().add(EleFXThemes.MESSAGE_BOX.toData());
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE && options.isCloseOnPressEscape()) close();
            });
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                event.consume();
                close();
            });
            stage.setX(viewport.getMinX());
            stage.setY(viewport.getMinY());
            stage.show();
            if (options.isAutofocus())
                Platform.runLater(() -> (options.isShowInput() ? input : confirmButton).requestFocus());
        };
        if (Platform.isFxApplicationThread())
            opener.run();
        else
            Platform.runLater(opener);
    }

    private void requestClose(EleFXMessageBoxAction requested) {
        if (closing || !isShowing()) return;
        if (requested == EleFXMessageBoxAction.CONFIRM && !validateInput()) return;
        BeforeCloseHandler guard = options.getBeforeClose();
        if (guard == null)
            finishClose(requested);
        else
            guard.handle(requested, this, () -> finishClose(requested));
    }

    private boolean validateInput() {
        if (!options.isShowInput()) return true;
        String error = null, value = input.getText();
        if (options.getInputPattern() != null && !options.getInputPattern().matcher(value).matches())
            error = options.getInputErrorMessage();
        if (error == null && options.getInputValidator() != null) {
            Object result = options.getInputValidator().validate(value);
            if (result instanceof String string && !string.isBlank())
                error = string;
            else if (Boolean.FALSE.equals(result) || result == null) error = options.getInputErrorMessage();
        }
        inputError.setText(error == null ? "" : error);
        inputError.setVisible(error != null);
        inputError.setManaged(error != null);
        return error == null;
    }

    private void finishClose(EleFXMessageBoxAction requested) {
        if (closing) return;
        closing = true;
        EleFXMessageBoxAction action = requested == EleFXMessageBoxAction.CLOSE
                && !options.isDistinguishCancelAndClose()
                        ? EleFXMessageBoxAction.CANCEL
                        : requested;
        EleFXMessageBoxResult result = new EleFXMessageBoxResult(action,
                options.isShowInput() ? input.getText() : null);
        if (stage != null) stage.hide();
        Consumer<EleFXMessageBoxResult> callback = options.getCallback();
        if (callback != null) callback.accept(result);
        future.complete(result);
    }

    private void refreshIcon() {
        Node icon = options.getIcon();
        if (icon == null && options.getType() != null) icon = new EleFXIcon(switch (options.getType()) {
            case PRIMARY, INFO -> EleFXIconType.INFO_FILLED;
            case SUCCESS -> EleFXIconType.CIRCLE_CHECK_FILLED;
            case WARNING -> EleFXIconType.WARNING_FILLED;
            case ERROR -> EleFXIconType.CIRCLE_CLOSE_FILLED;
        }, 24);
        iconArea.getChildren().setAll(icon == null ? List.of() : List.of(icon));
        if (options.getType() != null)
            iconArea.getStyleClass().add("ele-message-box__status--" + options.getType().name().toLowerCase());
    }

    private javafx.geometry.Rectangle2D viewport() {
        Window owner = options.getOwner();
        if (owner != null) {
            return new javafx.geometry.Rectangle2D(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight());
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private void installDragging() {
        header.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            dragAnchorX = event.getScreenX();
            dragAnchorY = event.getScreenY();
            boxAnchorX = box.getTranslateX();
            boxAnchorY = box.getTranslateY();
        });
        header.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (stage == null) return;
            double x = boxAnchorX + event.getScreenX() - dragAnchorX,
                    y = boxAnchorY + event.getScreenY() - dragAnchorY;
            if (!options.isOverflow()) {
                double maxX = Math.max(0, (stage.getWidth() - box.getWidth()) / 2);
                double maxY = Math.max(0, (stage.getHeight() - box.getHeight()) / 2);
                x = Math.max(-maxX, Math.min(x, maxX));
                y = Math.max(-maxY, Math.min(y, maxY));
            }
            box.setTranslateX(x);
            box.setTranslateY(y);
            if (!box.getStyleClass().contains("is-dragging")) box.getStyleClass().add("is-dragging");
        });
        header.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> box.getStyleClass().remove("is-dragging"));
    }

    private static void applyCustomClasses(Node node, String classes, List<String> applied) {
        node.getStyleClass().removeAll(applied);
        applied.clear();
        for (String value : Objects.requireNonNullElse(classes, "").trim().split("\\s+"))
            if (!value.isBlank()) applied.add(value);
        node.getStyleClass().addAll(applied);
    }
}
