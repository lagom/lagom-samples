package com.example.helloproxy.impl

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.AkkaGrpcClient
import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import example.myapp.helloworld.grpc.{ GreeterService, GreeterServiceClient }
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

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
  private implicit val sys: ActorSystem = actorSystem

  private lazy val settings = GrpcClientSettings
    .usingServiceDiscovery("hello-srvc")
    .withServicePortName("http")
    .withDeadline(Duration.create(5, TimeUnit.SECONDS)) // response timeout
    .withConnectionAttempts(5) // use a small reconnectionAttempts value to cause a client reload in case of failure

  lazy val greeterServiceClient: GreeterServiceClient = GreeterServiceClient(settings)
  //  Register a shutdown task to release resources of the client
  coordinatedShutdown
    .addTask(
      CoordinatedShutdown.PhaseServiceUnbind,
      "shutdown-greeter-service-grpc-client"
    ) { () => greeterServiceClient.close() }

  lazy val helloService = serviceClient.implement[HelloService]

  override lazy val lagomServer = serverFor[HelloProxyService](wire[HelloProxyServiceImpl])

}

