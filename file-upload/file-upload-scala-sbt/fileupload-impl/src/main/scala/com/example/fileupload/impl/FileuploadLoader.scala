package com.example.fileupload.impl

import com.example.fileupload.api.FileUploadService
import com.example.play.controllers.FileUploadController
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes

class FileUploadLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new FileUploadApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new FileUploadApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[FileUploadService])
}

abstract class FileUploadApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[FileUploadService](wire[FileUploadServiceImpl])


  // We override the default `router` Lagom builds from the user-provided
  // `lagomServer`. The new Router is actually an instance of the result
  // of compiling the `routes` file in `src/main/resources`.
  //
  // The router is built using an httpErrorHandler and a list of Routes.
  // the list of Routes depends on the contents of the `src/main/resources/routes`
  // file. In our case, the `src/main/resources/routes` declares:
  //
  //  POST    /api/files      com.example.play.controllers.FileUploadController.uploadFile()
  //  ->      /               com.lightbend.lagom.scaladsl.server.LagomServiceRouter
  //
  // which translates into `new Routes(...)` needing an instance of a
  // FileUploadController and an instance of the default lagom router.
  //
  // This is a Play provided feature. To know more, see:
  //     https://www.playframework.com/documentation/2.6.x/ScalaCompileTimeDependencyInjection#Providing-a-router
  override lazy val router = new Routes(
    httpErrorHandler,
    new FileUploadController(controllerComponents),
    lagomServer.router
  )

}
