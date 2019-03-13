package com.example.fileupload.api;


import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.ServiceAcl.path;

/**
 * This Service represents your usual Lagom code. You can have many calls here.
 *
 * The particularity of this file resides on the ACLs declaration: we are manually
 * creating the list of ACLs to include `/api/files` which is not a call handled
 * by the Lagom Router but still a Call available in the FileUpload Application.
 */
public interface FileUploadService extends Service {

    /**
     * Invoke using:
     * <code> curl -X POST -H "Content-Type: text/plain" -d  "hello world" http://localhost:9000/api/echo </code>
     */
    ServiceCall<String, String> uppercaseEcho();

    @Override
    default Descriptor descriptor() {
        return named("fileupload")
                .withCalls(
                        pathCall("/api/echo", this::uppercaseEcho)
                )
                .withAutoAcl(true)
                .withServiceAcls(path("/api/files"));
    }
}
