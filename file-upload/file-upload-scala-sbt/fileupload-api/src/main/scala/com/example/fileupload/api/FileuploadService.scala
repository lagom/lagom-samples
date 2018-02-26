package com.example.fileupload.api

import com.lightbend.lagom.scaladsl.api.{ Service, ServiceAcl, ServiceCall }

/**
  * This Service represents your usual Lagom code. You will have many call heres.
  * The particularity of this file resides on the ACLs declaration: we are manually
  * creating the list of ACLs to include `/api/files` which is not a call handled
  * by the Lagom Router but still a Call available in the FileUpload Application.
  */
trait FileUploadService extends Service {

  /**
    * Invoke using:
     <code> curl -X POST -H "Content-Type: text/plain" -d  "hello world" http://localhost:9000/api/echo </code>
    */
  def uppercaseEcho(): ServiceCall[String, String]

  override final def descriptor = {
    import Service._
    named("fileupload")
      .withCalls(
        pathCall("/api/echo", uppercaseEcho _)
      )
      .withAcls(
        ServiceAcl(pathRegex = Some("/api/echo")),
        ServiceAcl(pathRegex = Some("/api/files"))
      )
  }
}
