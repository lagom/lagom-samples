package com.lightbend.lagom.recipes.corsscala.impl

import com.lightbend.lagom.recipes.corsscala.api.CorsscalaService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSComponents

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
    with AhcWSComponents
    // This code example uses Play's thin-cake, compile-time Dependency Injection instead of using the runtime
    // Dependency Injection based on Guice. You can find the complete list of available Play Components by searching for
    // components in Play's API docs (https://www.playframework.com/documentation/2.5.x/api/scala/index.html).
    with CORSComponents {

  override val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)

  override lazy val lagomServer = serverFor[CorsscalaService](wire[CorsscalaServiceImpl])
}
