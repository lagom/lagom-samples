package com.example.filedownload.impl

import com.example.filedownload.api.FileDownloadService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class FileDownloadLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new FileDownloadApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new FileDownloadApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[FileDownloadService])

}

abstract class FileDownloadApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  protected lazy val employeeRepository: EmployeeRepository = wire[EmployeeRepository]

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[FileDownloadService](wire[FileDownloadServiceImpl])

}
