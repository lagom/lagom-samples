package com.example.fileupload.impl

import com.example.fileupload.api.FileUploadService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

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
  override lazy val lagomServer =
    serverFor[FileUploadService](wire[FileUploadServiceImpl])
      .additionalRouter(wire[FileUploadRouter].router)

}
