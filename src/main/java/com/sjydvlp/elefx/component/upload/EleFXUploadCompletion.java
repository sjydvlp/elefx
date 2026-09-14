package com.sjydvlp.elefx.component.upload;

/** Callback supplied to a custom {@link EleFXUploadRequest}. */
public interface EleFXUploadCompletion {

    void progress(double progress);

    void succeed();

    void fail(Throwable error);
}
