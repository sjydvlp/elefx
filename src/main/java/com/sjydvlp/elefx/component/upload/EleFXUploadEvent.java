package com.sjydvlp.elefx.component.upload;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import java.util.List;

/** Event emitted for upload selection, transport, preview, removal, and limit changes. */
public final class EleFXUploadEvent extends Event {

    public static final EventType<EleFXUploadEvent> ANY = new EventType<>(Event.ANY, "ELEFX_UPLOAD");

    public static final EventType<EleFXUploadEvent> CHANGE = new EventType<>(ANY, "CHANGE");

    public static final EventType<EleFXUploadEvent> REMOVE = new EventType<>(ANY, "REMOVE");

    public static final EventType<EleFXUploadEvent> PREVIEW = new EventType<>(ANY, "PREVIEW");

    public static final EventType<EleFXUploadEvent> SUCCESS = new EventType<>(ANY, "SUCCESS");

    public static final EventType<EleFXUploadEvent> ERROR = new EventType<>(ANY, "ERROR");

    public static final EventType<EleFXUploadEvent> PROGRESS = new EventType<>(ANY, "PROGRESS");

    public static final EventType<EleFXUploadEvent> EXCEED = new EventType<>(ANY, "EXCEED");

    private final EleFXUploadFile file;

    private final List<EleFXUploadFile> files;

    public EleFXUploadEvent(Object source, EventTarget target, EventType<? extends EleFXUploadEvent> type,
            EleFXUploadFile file, List<EleFXUploadFile> files) {
        super(source, target, type);
        this.file = file;
        this.files = List.copyOf(files);
    }

    public EleFXUploadFile getFile() {
        return file;
    }

    public List<EleFXUploadFile> getFiles() {
        return files;
    }
}
