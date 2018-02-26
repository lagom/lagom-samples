package com.example.play.controllers

import java.io.File
import java.util.UUID

import akka.stream.scaladsl.{ FileIO, Sink }
import akka.stream.{ IOResult, Materializer }
import akka.util.ByteString
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.{ FileInfo, FilePartHandler }

import scala.concurrent.{ ExecutionContext, Future }


/**
  * This Controller is a copy, almost verbatim, of the File Upload example available
  * from the Play example collection. See:
  * https://github.com/playframework/play-scala-fileupload-example/tree/2.6.x
  * and also:
  * https://www.playframework.com/download#examples
  *
  * To know more about File upload handling in Play, have a look at the docs:
  * https://www.playframework.com/documentation/2.6.x/ScalaFileUpload
  */
class FileUploadController(
                            controllerComponents: play.api.mvc.ControllerComponents
                          )(
                            implicit mat: Materializer,
                            exCtx: ExecutionContext
                          ) extends AbstractController(controllerComponents) {

  private val logger = Logger(this.getClass)


  // this is Play's Action
  def uploadFile(): Action[MultipartFormData[File]] =
    Action.async(parse.multipartFormData(fileHandler)) { request: Request[MultipartFormData[File]] =>
      val files = request.body.files
      Future.successful(Ok(files.map(_.ref.getAbsolutePath) mkString("Uploaded[", ", ", "]")))
    }

  // A Play FilePartHandler[T] creates an Accumulator (similar to Akka Stream's Sinks)
  // for each FileInfo in the multipart request.
  private def fileHandler: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) => {
      val tempFile = {
        // create a temp file in the `target` folder
        val f = new java.io.File("./target/file-upload-data/uploads", UUID.randomUUID().toString).getAbsoluteFile
        // make sure the subfolders inside `target` exist.
        f.getParentFile.mkdirs()
        f
      }
      val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(tempFile.toPath)
      val acc: Accumulator[ByteString, IOResult] = Accumulator(sink)
      acc.map {
        case akka.stream.IOResult(bytesWriten, status) =>
          FilePart(partName, filename, contentType, tempFile)
      }
    }
  }

}

