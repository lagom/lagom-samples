package com.example.fileupload.impl;

import com.example.fileupload.api.FileUploadService;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public ServiceCall<String, String> uppercaseEcho() {
        return input -> completedFuture(input.toUpperCase());
    }

}
