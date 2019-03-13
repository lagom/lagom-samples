package com.example;

import play.api.mvc.Handler;
import play.api.mvc.RequestHeader;
import play.api.routing.Router;
import play.api.routing.SimpleRouter;
import play.mvc.Http;
import play.routing.RoutingDsl;
import scala.PartialFunction;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static play.mvc.Results.ok;

class FileUploadRouter implements SimpleRouter {

    private final Router delegate;

    @Inject
    public FileUploadRouter(RoutingDsl routingDsl) {
        this.delegate = routingDsl
                .POST("/api/files")
                .routingTo(request -> {
                    Http.MultipartFormData<File> body = request.body().asMultipartFormData();
                    List<Http.MultipartFormData.FilePart<File>> files = body.getFiles();
                    String response = files.stream()
                            .map(f -> f.getFile().getAbsolutePath())
                            .collect(joining(",", "Uploaded[", "]"));
                    return ok(response);
                })
                .build().asScala();
    }

    @Override
    public PartialFunction<RequestHeader, Handler> routes() {
        return delegate.routes();
    }
}
