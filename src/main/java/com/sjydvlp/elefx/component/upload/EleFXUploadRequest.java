package com.sjydvlp.elefx.component.upload;

/** Performs upload transport. The control deliberately does not prescribe an HTTP client. */
@FunctionalInterface
public interface EleFXUploadRequest {

    void upload(EleFXUploadFile file, EleFXUploadCompletion completion);
}
