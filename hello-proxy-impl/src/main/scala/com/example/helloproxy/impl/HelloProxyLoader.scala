package com.example.helloproxy.impl

import akka.actor.CoordinatedShutdown
import akka.grpc.GrpcClientSettings
import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import example.myapp.helloworld.grpc.{ GreeterService, GreeterServiceClient }
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContextExecutor

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

  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  lazy val settings = GrpcClientSettings.fromConfig("my-fancy-client-name")(actorSystem)
  lazy val greeterServiceClient: GreeterServiceClient = new GreeterServiceClient(settings)(materializer, dispatcher)
  // Bind the service that this server provides
  coordinatedShutdown
    .addTask(
      CoordinatedShutdown.PhaseServiceUnbind,
      "shutdown-greeter-service-grpc-client"
    ) { () => greeterServiceClient.close() }

  override lazy val lagomServer = serverFor[HelloProxyService](wire[HelloProxyServiceImpl])


  // Bind the HelloService client
  lazy val helloService = serviceClient.implement[HelloService]
}
