package com.lightbend.lagom.recipes.corsscala.impl

import com.lightbend.lagom.recipes.corsscala.api.CorsscalaService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class CorsscalaLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CorsscalaApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CorsscalaApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CorsscalaService])

}

abstract class CorsscalaApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[CorsscalaService](wire[CorsscalaServiceImpl])
}
