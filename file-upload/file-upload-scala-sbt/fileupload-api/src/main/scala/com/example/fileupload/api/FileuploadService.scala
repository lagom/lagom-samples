package com.example.fileupload.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceAcl, ServiceCall}

/**
  * This Service represents your usual Lagom code. You can have many calls here.
 *
  * The particularity of this file resides on the ACLs declaration: we are manually
  * creating the list of ACLs to include `/api/files` which is not a call handled
  * by the Lagom Router but still a Call available in the FileUpload Application.
  */
trait FileUploadService extends Service {

  /**
    * Invoke using:
     <code> curl -X GET -H "Content-Type: text/plain" http://localhost:9000/api/hello/John </code>
    */
  def hello(name: String): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    named("fileupload")
      .withCalls(
        pathCall("/api/hello/:name", hello _)
      )
      .withAutoAcl(true)
      .withAcls(
        ServiceAcl(pathRegex = Some("/api/files"))
      )
  }
}
