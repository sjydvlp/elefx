package com.sjydvlp.elefx.component.upload;

import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.button.EleFXButtonType;
import com.sjydvlp.elefx.component.icon.EleFXIcon;
import com.sjydvlp.elefx.component.icon.EleFXIconType;
import com.sjydvlp.elefx.theme.EleFXThemes;
import com.sjydvlp.elefx.theme.Theme;
import com.sjydvlp.elefx.theme.Themable;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

/**
 * Element Plus inspired file picker with text, picture, and picture-card lists.
 * <p>
 * It owns selection and presentation; actual transport is supplied through
 * {@link #setUploadRequest(EleFXUploadRequest)}. Its default request succeeds
 * immediately, making local-only selection useful without network configuration.
 * </p>
 */
public class EleFXUpload extends VBox implements Themable {

    private final ObservableList<EleFXUploadFile> files = FXCollections.observableArrayList(file -> new Observable[] {
            file.nameProperty(), file.urlProperty(), file.statusProperty(), file.progressProperty()});

    private final BooleanProperty multiple = new SimpleBooleanProperty(this, "multiple", false);

    private final BooleanProperty showFileList = new SimpleBooleanProperty(this, "showFileList", true);

    private final BooleanProperty drag = new SimpleBooleanProperty(this, "drag", false);

    private final BooleanProperty autoUpload = new SimpleBooleanProperty(this, "autoUpload", true);

    private final IntegerProperty limit = new SimpleIntegerProperty(this, "limit", -1);

    private final StringProperty accept = new SimpleStringProperty(this, "accept", "");

    private final StringProperty action = new SimpleStringProperty(this, "action", "#");

    private final StringProperty name = new SimpleStringProperty(this, "name", "file");

    private final StringProperty triggerText = new SimpleStringProperty(this, "triggerText", "Click to upload");

    private final StringProperty tipText = new SimpleStringProperty(this, "tipText", "");

    private final ObjectProperty<EleFXUploadListType> listType = new SimpleObjectProperty<>(this, "listType",
            EleFXUploadListType.TEXT);

    private final ObjectProperty<Node> trigger = new SimpleObjectProperty<>(this, "trigger");

    private final ObjectProperty<Predicate<EleFXUploadFile>> beforeUpload = new SimpleObjectProperty<>(this,
            "beforeUpload");

    private final ObjectProperty<Predicate<EleFXUploadFile>> beforeRemove = new SimpleObjectProperty<>(this,
            "beforeRemove");

    private final ObjectProperty<EleFXUploadRequest> uploadRequest = new SimpleObjectProperty<>(this, "uploadRequest");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onChange = new SimpleObjectProperty<>(this,
            "onChange");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onRemove = new SimpleObjectProperty<>(this,
            "onRemove");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onPreview = new SimpleObjectProperty<>(this,
            "onPreview");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onSuccess = new SimpleObjectProperty<>(this,
            "onSuccess");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onError = new SimpleObjectProperty<>(this, "onError");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onProgress = new SimpleObjectProperty<>(this,
            "onProgress");

    private final ObjectProperty<EventHandler<EleFXUploadEvent>> onExceed = new SimpleObjectProperty<>(this,
            "onExceed");

    private final StackPane triggerPane = new StackPane();

    private final FlowPane cards = new FlowPane(8, 8);

    private final VBox rows = new VBox(4);

    private final Label tip = new Label();

    public EleFXUpload() {
        initialize();
    }

    public ObservableList<EleFXUploadFile> getFiles() {
        return files;
    }

    public void setFiles(Collection<? extends EleFXUploadFile> value) {
        files.setAll(value == null ? List.of() : value);
    }

    public boolean isMultiple() {
        return multiple.get();
    }

    public BooleanProperty multipleProperty() {
        return multiple;
    }

    public void setMultiple(boolean value) {
        multiple.set(value);
    }

    public boolean isShowFileList() {
        return showFileList.get();
    }

    public BooleanProperty showFileListProperty() {
        return showFileList;
    }

    public void setShowFileList(boolean value) {
        showFileList.set(value);
    }

    public boolean isDrag() {
        return drag.get();
    }

    public BooleanProperty dragProperty() {
        return drag;
    }

    public void setDrag(boolean value) {
        drag.set(value);
    }

    public boolean isAutoUpload() {
        return autoUpload.get();
    }

    public BooleanProperty autoUploadProperty() {
        return autoUpload;
    }

    public void setAutoUpload(boolean value) {
        autoUpload.set(value);
    }

    /** Maximum count, or -1 for no maximum. */
    public int getLimit() {
        return limit.get();
    }

    public IntegerProperty limitProperty() {
        return limit;
    }

    public void setLimit(int value) {
        limit.set(value < 0 ? -1 : value);
    }

    public String getAccept() {
        return accept.get();
    }

    public StringProperty acceptProperty() {
        return accept;
    }

    public void setAccept(String value) {
        accept.set(value == null ? "" : value);
    }

    /** Multipart upload endpoint, mirroring Element Plus's {@code action} attribute. */
    public String getAction() {
        return action.get();
    }

    public StringProperty actionProperty() {
        return action;
    }

    public void setAction(String value) {
        action.set(value == null ? "#" : value.trim());
    }

    /** Multipart form field name used by the built-in action uploader. */
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String value) {
        name.set(value == null || value.isBlank() ? "file" : value);
    }

    public String getTriggerText() {
        return triggerText.get();
    }

    public StringProperty triggerTextProperty() {
        return triggerText;
    }

    public void setTriggerText(String value) {
        triggerText.set(value == null ? "" : value);
    }

    public String getTipText() {
        return tipText.get();
    }

    public StringProperty tipTextProperty() {
        return tipText;
    }

    public void setTipText(String value) {
        tipText.set(value == null ? "" : value);
    }

    public EleFXUploadListType getListType() {
        return listType.get();
    }

    public ObjectProperty<EleFXUploadListType> listTypeProperty() {
        return listType;
    }

    public void setListType(EleFXUploadListType value) {
        listType.set(value == null ? EleFXUploadListType.TEXT : value);
    }

    /** Optional node used instead of the built-in trigger button/drop zone. */
    public Node getTrigger() {
        return trigger.get();
    }

    public ObjectProperty<Node> triggerProperty() {
        return trigger;
    }

    public void setTrigger(Node value) {
        trigger.set(value);
    }

    public Predicate<EleFXUploadFile> getBeforeUpload() {
        return beforeUpload.get();
    }

    public ObjectProperty<Predicate<EleFXUploadFile>> beforeUploadProperty() {
        return beforeUpload;
    }

    public void setBeforeUpload(Predicate<EleFXUploadFile> value) {
        beforeUpload.set(value);
    }

    public Predicate<EleFXUploadFile> getBeforeRemove() {
        return beforeRemove.get();
    }

    public ObjectProperty<Predicate<EleFXUploadFile>> beforeRemoveProperty() {
        return beforeRemove;
    }

    public void setBeforeRemove(Predicate<EleFXUploadFile> value) {
        beforeRemove.set(value);
    }

    public EleFXUploadRequest getUploadRequest() {
        return uploadRequest.get();
    }

    public ObjectProperty<EleFXUploadRequest> uploadRequestProperty() {
        return uploadRequest;
    }

    public void setUploadRequest(EleFXUploadRequest value) {
        uploadRequest.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnChange() {
        return onChange.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onChangeProperty() {
        return onChange;
    }

    public void setOnChange(EventHandler<EleFXUploadEvent> value) {
        onChange.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnRemove() {
        return onRemove.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onRemoveProperty() {
        return onRemove;
    }

    public void setOnRemove(EventHandler<EleFXUploadEvent> value) {
        onRemove.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnPreview() {
        return onPreview.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onPreviewProperty() {
        return onPreview;
    }

    public void setOnPreview(EventHandler<EleFXUploadEvent> value) {
        onPreview.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnSuccess() {
        return onSuccess.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onSuccessProperty() {
        return onSuccess;
    }

    public void setOnSuccess(EventHandler<EleFXUploadEvent> value) {
        onSuccess.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnError() {
        return onError.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onErrorProperty() {
        return onError;
    }

    public void setOnError(EventHandler<EleFXUploadEvent> value) {
        onError.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnProgress() {
        return onProgress.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onProgressProperty() {
        return onProgress;
    }

    public void setOnProgress(EventHandler<EleFXUploadEvent> value) {
        onProgress.set(value);
    }

    public EventHandler<EleFXUploadEvent> getOnExceed() {
        return onExceed.get();
    }

    public ObjectProperty<EventHandler<EleFXUploadEvent>> onExceedProperty() {
        return onExceed;
    }

    public void setOnExceed(EventHandler<EleFXUploadEvent> value) {
        onExceed.set(value);
    }

    /** Opens the platform file chooser. Call this from an action or mouse handler. */
    public void chooseFiles() {
        if (isDisabled()) return;
        FileChooser chooser = new FileChooser();
        configureFilters(chooser);
        Window owner = getScene() == null ? null : getScene().getWindow();
        List<File> picked = isMultiple()
                ? chooser.showOpenMultipleDialog(owner)
                : optional(chooser.showOpenDialog(owner));
        selectFiles(picked);
    }

    public void selectFiles(Collection<File> selected) {
        if (isDisabled() || selected == null || selected.isEmpty()) return;
        List<File> accepted = selected.stream().filter(Objects::nonNull).filter(this::accepts).toList();
        if (!isMultiple() && !accepted.isEmpty()) accepted = accepted.subList(0, 1);
        int available = getLimit() < 0 ? Integer.MAX_VALUE : Math.max(0, getLimit() - files.size());
        if (accepted.size() > available) {
            fire(EleFXUploadEvent.EXCEED, null);
            accepted = accepted.subList(0, available);
        }
        for (File selectedFile : accepted) {
            EleFXUploadFile file = new EleFXUploadFile(selectedFile);
            if (getBeforeUpload() != null && !getBeforeUpload().test(file)) continue;
            files.add(file);
            fire(EleFXUploadEvent.CHANGE, file);
            if (isAutoUpload()) upload(file);
        }
    }

    /** Starts all files currently waiting for manual submission. */
    public void submit() {
        files.stream().filter(file -> file.getStatus() == EleFXUploadStatus.READY).toList().forEach(this::upload);
    }

    public void clearFiles() {
        files.clear();
    }

    public boolean removeFile(EleFXUploadFile file) {
        if (file == null || !files.contains(file) || isDisabled()) return false;
        if (getBeforeRemove() != null && !getBeforeRemove().test(file)) return false;
        files.remove(file);
        fire(EleFXUploadEvent.REMOVE, file);
        fire(EleFXUploadEvent.CHANGE, file);
        return true;
    }

    public void preview(EleFXUploadFile file) {
        if (file == null) return;
        fire(EleFXUploadEvent.PREVIEW, file);
        if (getOnPreview() == null) showPreview(file);
    }

    @Override
    public Parent toParent() {
        return this;
    }

    @Override
    public Theme getTheme() {
        return EleFXThemes.UPLOAD;
    }

    private void initialize() {
        getStyleClass().add("ele-upload");
        setSpacing(8);
        setAlignment(Pos.TOP_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        cards.getStyleClass().add("ele-upload-list--card");
        rows.getStyleClass().add("ele-upload-list");
        tip.getStyleClass().add("ele-upload__tip");
        triggerPane.getStyleClass().add("ele-upload__trigger");
        triggerPane.setAlignment(Pos.CENTER_LEFT);
        triggerPane.setMaxWidth(Region.USE_PREF_SIZE);
        triggerPane.setOnMouseClicked(event -> chooseFiles());
        triggerPane.setOnDragEntered(this::dragEntered);
        triggerPane.setOnDragExited(this::dragExited);
        triggerPane.setOnDragOver(this::dragOver);
        triggerPane.setOnDragDropped(this::dragDropped);
        getChildren().addAll(triggerPane, tip, rows, cards);
        files.addListener((ListChangeListener<EleFXUploadFile>) change -> rebuild());
        Observable[] properties = {multiple, showFileList, drag, disableProperty(), triggerText, tipText, listType,
                trigger};
        for (Observable property : properties)
            property.addListener(observable -> rebuild());
        rebuild();
        sceneBuilderIntegration();
    }

    private void rebuild() {
        triggerPane.getStyleClass().removeAll("ele-upload__trigger--drag", "ele-upload__trigger--dragover",
                "ele-upload__trigger--disabled");
        if (isDrag()) triggerPane.getStyleClass().add("ele-upload__trigger--drag");
        if (isDisabled()) triggerPane.getStyleClass().add("ele-upload__trigger--disabled");
        triggerPane.setMaxWidth(isDrag() ? Double.MAX_VALUE : Region.USE_PREF_SIZE);
        triggerPane.getChildren().setAll(getTrigger() == null ? defaultTrigger() : getTrigger());
        boolean cardsVisible = isShowFileList() && getListType() == EleFXUploadListType.PICTURE_CARD;
        triggerPane.setVisible(!cardsVisible);
        triggerPane.setManaged(!cardsVisible);
        cards.setVisible(cardsVisible);
        cards.setManaged(cardsVisible);
        rows.setVisible(isShowFileList() && !cardsVisible);
        rows.setManaged(isShowFileList() && !cardsVisible);
        rows.getChildren().setAll(files.stream().map(this::row).toList());
        List<Node> cardItems = new ArrayList<>();
        cardItems.addAll(files.stream().map(this::card).toList());
        if (cardsVisible) cardItems.add(cardTrigger());
        cards.getChildren().setAll(cardItems);
        tip.setText(getTipText());
        tip.setVisible(!getTipText().isBlank());
        tip.setManaged(!getTipText().isBlank());
    }

    private Node defaultTrigger() {
        if (!isDrag()) {
            EleFXButton button = new EleFXButton(getTriggerText(), EleFXButtonType.PRIMARY);
            // Buttons consume mouse clicks before they bubble to triggerPane.
            button.setOnAction(event -> chooseFiles());
            button.setDisable(isDisabled());
            return button;
        }
        VBox box = new VBox(8, new EleFXIcon(EleFXIconType.UPLOAD_FILLED, 38),
                new Label("Drop file here or click to upload"));
        box.getStyleClass().add("ele-upload-dragger");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Node cardTrigger() {
        StackPane card = new StackPane(new EleFXIcon(EleFXIconType.PLUS, 28));
        card.getStyleClass().add("ele-upload-list__card-trigger");
        applyCardClip(card);
        card.setOnMouseClicked(event -> chooseFiles());
        card.setDisable(isDisabled());
        return card;
    }

    private Node row(EleFXUploadFile file) {
        HBox box = new HBox(7);
        box.getStyleClass().add("ele-upload-list__item");
        box.setAlignment(Pos.CENTER_LEFT);
        Node image = getListType() == EleFXUploadListType.PICTURE
                ? thumbnail(file, 38)
                : new EleFXIcon(EleFXIconType.DOCUMENT, 16);
        Label name = new Label(file.getName());
        name.getStyleClass().add("ele-upload-list__name");
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setOnMouseClicked(event -> preview(file));
        Label status = new Label(file.getStatus() == EleFXUploadStatus.UPLOADING
                ? Math.round(file.getProgress() * 100) + "%"
                : "");
        status.getStyleClass().add("ele-upload-list__status");
        EleFXIcon success = new EleFXIcon(EleFXIconType.CIRCLE_CHECK, 16);
        success.getStyleClass().add("ele-upload-list__success");
        success.setVisible(file.getStatus() == EleFXUploadStatus.SUCCESS);
        success.setManaged(file.getStatus() == EleFXUploadStatus.SUCCESS);
        EleFXButton remove = new EleFXButton(new EleFXIcon(EleFXIconType.CLOSE, 14));
        remove.getStyleClass().add("ele-upload-list__remove");
        remove.setOnAction(event -> removeFile(file));
        remove.setDisable(isDisabled());
        remove.setVisible(false);
        remove.setManaged(false);
        StackPane trailing = new StackPane(status, success, remove);
        trailing.getStyleClass().add("ele-upload-list__actions");
        box.setOnMouseEntered(event -> {
            status.setVisible(false);
            success.setVisible(false);
            remove.setVisible(true);
            remove.setManaged(true);
        });
        box.setOnMouseExited(event -> {
            remove.setVisible(false);
            remove.setManaged(false);
            boolean uploaded = file.getStatus() == EleFXUploadStatus.SUCCESS;
            success.setVisible(uploaded);
            success.setManaged(uploaded);
            status.setVisible(!uploaded);
        });
        box.getChildren().addAll(image, name, trailing);
        return box;
    }

    private Node card(EleFXUploadFile file) {
        StackPane box = new StackPane(thumbnail(file, 146));
        box.getStyleClass().add("ele-upload-list__card");
        applyCardClip(box);
        box.setOnMouseClicked(event -> preview(file));
        HBox actions = new HBox(4);
        actions.setAlignment(Pos.CENTER);
        actions.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        actions.getStyleClass().add("ele-upload-list__card-actions");
        EleFXIcon viewIcon = new EleFXIcon(EleFXIconType.VIEW, 20);
        viewIcon.setFill(Color.WHITE);
        EleFXButton view = new EleFXButton(viewIcon);
        view.setOnAction(event -> preview(file));
        EleFXIcon deleteIcon = new EleFXIcon(EleFXIconType.DELETE, 20);
        deleteIcon.setFill(Color.WHITE);
        EleFXButton remove = new EleFXButton(deleteIcon);
        remove.setDisable(isDisabled());
        remove.setOnAction(event -> removeFile(file));
        actions.getChildren().addAll(view, remove);
        box.getChildren().add(actions);
        return box;
    }

    private static void applyCardClip(StackPane card) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        card.setClip(clip);
    }

    private void showPreview(EleFXUploadFile file) {
        try {
            Image image = new Image(file.getUrl(), 720, 560, true, true, false);
            if (image.isError()) return;
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(Math.min(720, image.getWidth()));
            imageView.setFitHeight(Math.min(560, image.getHeight()));
            Stage stage = new Stage(StageStyle.UNDECORATED);
            EleFXIcon closeIcon = new EleFXIcon(EleFXIconType.CLOSE, 20);
            closeIcon.setFill(Color.web("#909399"));
            EleFXButton close = new EleFXButton(closeIcon);
            close.setStyle("-fx-min-width: 28px; -fx-pref-width: 28px; -fx-min-height: 28px; "
                    + "-fx-pref-height: 28px; -fx-padding: 0; -fx-background-color: transparent; "
                    + "-fx-background-insets: 0; -fx-border-color: transparent; -fx-border-insets: 0;");
            close.setOnAction(event -> stage.close());
            close.setOnMouseEntered(event -> closeIcon.setFill(Color.web("#409eff")));
            close.setOnMouseExited(event -> closeIcon.setFill(Color.web("#909399")));
            StackPane preview = new StackPane(imageView, close);
            preview.setStyle("-fx-background-color: white; -fx-padding: 8px;");
            StackPane.setAlignment(close, Pos.TOP_RIGHT);
            StackPane.setMargin(close, new Insets(8));
            Scene scene = new Scene(preview);
            stage.setScene(scene);
            if (getScene() != null && getScene().getWindow() != null) {
                stage.initOwner(getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.show();
        } catch (RuntimeException ignored) {
            // A non-image file can still use the onPreview callback.
        }
    }

    private Node thumbnail(EleFXUploadFile file, double size) {
        try {
            Image image = new Image(file.getUrl(), size, size, true, true, false);
            if (!image.isError()) {
                ImageView view = new ImageView(image);
                view.setFitWidth(size);
                view.setFitHeight(size);
                view.setPreserveRatio(true);
                return view;
            }
        } catch (RuntimeException ignored) {
        }
        return new EleFXIcon(EleFXIconType.PICTURE, Math.min(size, 32));
    }

    private void upload(EleFXUploadFile file) {
        file.setStatus(EleFXUploadStatus.UPLOADING);
        file.setProgress(0);
        EleFXUploadCompletion completion = new EleFXUploadCompletion() {

            @Override
            public void progress(double value) {
                runOnFx(() -> {
                    file.setProgress(value);
                    fire(EleFXUploadEvent.PROGRESS, file);
                });
            }

            @Override
            public void succeed() {
                runOnFx(() -> {
                    file.setProgress(1);
                    file.setStatus(EleFXUploadStatus.SUCCESS);
                    fire(EleFXUploadEvent.SUCCESS, file);
                    fire(EleFXUploadEvent.CHANGE, file);
                });
            }

            @Override
            public void fail(Throwable error) {
                runOnFx(() -> {
                    file.setError(error);
                    file.setStatus(EleFXUploadStatus.FAILED);
                    fire(EleFXUploadEvent.ERROR, file);
                    fire(EleFXUploadEvent.CHANGE, file);
                });
            }
        };
        try {
            if (getUploadRequest() != null)
                getUploadRequest().upload(file, completion);
            else if (hasAction())
                uploadToAction(file, completion);
            else
                completion.succeed();
        } catch (Throwable error) {
            completion.fail(error);
        }
    }

    private boolean hasAction() {
        return !getAction().isBlank() && !"#".equals(getAction());
    }

    private void uploadToAction(EleFXUploadFile file, EleFXUploadCompletion completion) throws IOException {
        if (file.getFile() == null) {
            completion.fail(new IllegalArgumentException("The built-in action uploader requires a local file."));
            return;
        }
        String boundary = "EleFXUpload-" + UUID.randomUUID();
        byte[] body = multipartBody(file, boundary);
        HttpRequest request = HttpRequest.newBuilder(URI.create(getAction()))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, error) -> {
                    if (error != null)
                        completion.fail(error);
                    else if (response.statusCode() >= 200 && response.statusCode() < 300)
                        completion.succeed();
                    else
                        completion.fail(new IOException("Upload failed with HTTP " + response.statusCode()));
                });
    }

    private byte[] multipartBody(EleFXUploadFile file, String boundary) throws IOException {
        String escapedName = file.getName().replace("\\", "\\\\").replace("\"", "\\\"");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + getName() + "\"; filename=\"" + escapedName
                + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(Files.readAllBytes(file.getFile().toPath()));
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private static void runOnFx(Runnable task) {
        if (Platform.isFxApplicationThread())
            task.run();
        else {
            try {
                Platform.runLater(task);
            } catch (IllegalStateException ignored) {
                task.run();
            }
        }
    }

    private void fire(javafx.event.EventType<EleFXUploadEvent> type, EleFXUploadFile file) {
        EleFXUploadEvent event = new EleFXUploadEvent(this, this, type, file, files);
        fireEvent(event);
        EventHandler<EleFXUploadEvent> callback = callback(type);
        if (callback != null) callback.handle(event);
    }

    private EventHandler<EleFXUploadEvent> callback(javafx.event.EventType<EleFXUploadEvent> type) {
        if (type == EleFXUploadEvent.CHANGE) return getOnChange();
        if (type == EleFXUploadEvent.REMOVE) return getOnRemove();
        if (type == EleFXUploadEvent.PREVIEW) return getOnPreview();
        if (type == EleFXUploadEvent.SUCCESS) return getOnSuccess();
        if (type == EleFXUploadEvent.ERROR) return getOnError();
        if (type == EleFXUploadEvent.PROGRESS) return getOnProgress();
        return getOnExceed();
    }

    private void dragOver(DragEvent event) {
        if (isDrag() && !isDisabled() && event.getDragboard().hasFiles()) {
            addStyleClass(triggerPane, "ele-upload__trigger--dragover");
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void dragEntered(DragEvent event) {
        if (isDrag() && !isDisabled() && event.getDragboard().hasFiles())
            addStyleClass(triggerPane, "ele-upload__trigger--dragover");
        event.consume();
    }

    private void dragExited(DragEvent event) {
        triggerPane.getStyleClass().remove("ele-upload__trigger--dragover");
        event.consume();
    }

    private void dragDropped(DragEvent event) {
        triggerPane.getStyleClass().remove("ele-upload__trigger--dragover");
        if (isDrag() && !isDisabled() && event.getDragboard().hasFiles()) {
            selectFiles(event.getDragboard().getFiles());
            event.setDropCompleted(true);
        }
        event.consume();
    }

    private static void addStyleClass(Node node, String styleClass) {
        if (!node.getStyleClass().contains(styleClass)) node.getStyleClass().add(styleClass);
    }

    private List<File> optional(File file) {
        return file == null ? List.of() : List.of(file);
    }

    private boolean accepts(File file) {
        if (getAccept().isBlank()) return true;
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String entry : getAccept().split(",")) {
            String rule = entry.trim().toLowerCase(Locale.ROOT);
            if (rule.startsWith(".") && name.endsWith(rule)) return true;
            if (rule.equals("image/*") && name.matches(".*\\.(png|jpe?g|gif|bmp|webp)$")) return true;
        }
        return false;
    }

    private void configureFilters(FileChooser chooser) {
        if (getAccept().isBlank()) return;
        List<String> patterns = Arrays.stream(getAccept().split(",")).map(String::trim)
                .filter(value -> value.startsWith(".")).map(value -> "*" + value).toList();
        if (!patterns.isEmpty())
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Accepted files", patterns));
    }
}
