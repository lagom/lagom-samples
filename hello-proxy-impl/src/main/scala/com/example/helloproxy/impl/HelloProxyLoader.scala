package com.example.helloproxy.impl

import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class HelloProxyLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloProxyApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloProxyApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloProxyService])
}

abstract class HelloProxyApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[HelloProxyService](wire[HelloProxyServiceImpl])

  // Bind the HelloService client
  lazy val helloService = serviceClient.implement[HelloService]
}
