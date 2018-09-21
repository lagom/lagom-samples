package com.example.play.controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * https://www.playframework.com/documentation/2.6.x/JavaFileUpload
 */
public class FileUploadController extends Controller {

    public Result upload() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        List<Http.MultipartFormData.FilePart<File>> files = body.getFiles();
        return ok(
                files.stream().map(f -> f.getFile().getAbsolutePath()).collect(joining(",", "Uploaded[", "]"))
        );
    }
}
