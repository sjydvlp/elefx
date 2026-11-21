package com.sjydvlp.elefx.component.icon;

import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;

/** Cursor artwork shared by EleFX controls. */
public final class EleFXCursors {

    private EleFXCursors() {
    }

    public static ImageCursor createBlockedCursor() {
        var icon = new EleFXIcon(EleFXIconType.FORBIDDEN, 20);
        icon.setFill(Color.web("#606266"));
        var parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return new ImageCursor(icon.snapshot(parameters, null), 10, 10);
    }
}
