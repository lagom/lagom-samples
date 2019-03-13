package com.example.fileupload.impl

import com.example.fileupload.api.FileUploadService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class FileUploadServiceImpl extends FileUploadService {

  /**
    * This service call is implemented to have a basic Lagom ServiceImpl that we
    * can merge with the Play Router in FileUploadLoader.
    */
  override def hello(name: String) = ServiceCall { _ =>
    Future.successful(s"Hello, $name")
  }

}
