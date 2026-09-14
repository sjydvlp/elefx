package com.sjydvlp.elefx.component.upload;

import javafx.beans.property.*;
import java.io.File;
import java.util.Objects;

/** A mutable file entry in an {@link EleFXUpload} file list. */
public final class EleFXUploadFile {

    private final ObjectProperty<File> file = new SimpleObjectProperty<>(this, "file");

    private final StringProperty name = new SimpleStringProperty(this, "name", "");

    private final StringProperty url = new SimpleStringProperty(this, "url", "");

    private final ObjectProperty<EleFXUploadStatus> status = new SimpleObjectProperty<>(this, "status",
            EleFXUploadStatus.READY);

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);

    private final ObjectProperty<Throwable> error = new SimpleObjectProperty<>(this, "error");

    public EleFXUploadFile(File file) {
        setFile(file);
        setName(file == null ? "" : file.getName());
        if (file != null) setUrl(file.toURI().toString());
    }

    public EleFXUploadFile(String name, String url) {
        setName(name);
        setUrl(url);
    }

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public void setFile(File value) {
        file.set(value);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String value) {
        name.set(Objects.requireNonNullElse(value, ""));
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String value) {
        url.set(Objects.requireNonNullElse(value, ""));
    }

    public EleFXUploadStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<EleFXUploadStatus> statusProperty() {
        return status;
    }

    public void setStatus(EleFXUploadStatus value) {
        status.set(value == null ? EleFXUploadStatus.READY : value);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double value) {
        progress.set(Math.max(0, Math.min(1, value)));
    }

    public Throwable getError() {
        return error.get();
    }

    public ObjectProperty<Throwable> errorProperty() {
        return error;
    }

    public void setError(Throwable value) {
        error.set(value);
    }
}
