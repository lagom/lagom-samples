package com.example;

import com.example.fileupload.api.FileUploadService;
import com.example.fileupload.impl.FileUploadServiceImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import router.Routes;

public class FileUploadModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(
                FileUploadService.class, FileUploadServiceImpl.class,
                additionalRouter(Routes.class)
        );
    }
}
