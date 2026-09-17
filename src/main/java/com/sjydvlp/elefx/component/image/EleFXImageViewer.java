package com.sjydvlp.elefx.component.image;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * A standalone image-preview overlay, equivalent to Element Plus {@code el-image-viewer}.
 *
 * <p>
 * Unlike {@link EleFXImage}, this class is not a thumbnail node. Create it once, configure
 * its URL list, and call {@link #show()} from any trigger such as a button.
 * </p>
 */
public final class EleFXImageViewer {

    private final EleFXImage previewHost = new EleFXImage();

    public EleFXImageViewer() {
    }

    public EleFXImageViewer(Collection<String> urlList) {
        setUrlList(urlList);
    }

    public ObservableList<String> getUrlList() {
        return previewHost.getPreviewSrcList();
    }

    public void setUrlList(Collection<String> values) {
        previewHost.setPreviewSrcList(values);
    }

    public void setPreviewSrcList(Collection<String> values) {
        setUrlList(values);
    }

    public int getInitialIndex() {
        return previewHost.getInitialIndex();
    }

    public void setInitialIndex(int value) {
        previewHost.setInitialIndex(value);
    }

    public boolean isInfinite() {
        return previewHost.isInfinite();
    }

    public void setInfinite(boolean value) {
        previewHost.setInfinite(value);
    }

    public boolean isShowProgress() {
        return previewHost.isShowProgress();
    }

    public void setShowProgress(boolean value) {
        previewHost.setShowProgress(value);
    }

    public boolean isCloseOnPressEscape() {
        return previewHost.isCloseOnPressEscape();
    }

    public void setCloseOnPressEscape(boolean value) {
        previewHost.setCloseOnPressEscape(value);
    }

    public EventHandler<Event> getOnShow() {
        return previewHost.getOnShow();
    }

    public void setOnShow(EventHandler<Event> value) {
        previewHost.setOnShow(value);
    }

    public EventHandler<Event> getOnClose() {
        return previewHost.getOnClose();
    }

    public void setOnClose(EventHandler<Event> value) {
        previewHost.setOnClose(value);
    }

    public EventHandler<Event> getOnSwitch() {
        return previewHost.getOnSwitch();
    }

    public void setOnSwitch(EventHandler<Event> value) {
        previewHost.setOnSwitch(value);
    }

    /** Displays the preview overlay. */
    public void show() {
        previewHost.showPreview();
    }

    /** Element Plus-compatible alias for {@link #show()}. */
    public void showPreview() {
        show();
    }

    /** Hides the preview overlay. */
    public void hide() {
        previewHost.closePreview();
    }

    public void close() {
        hide();
    }

    public boolean isShowing() {
        return previewHost.isPreviewShowing();
    }
}
