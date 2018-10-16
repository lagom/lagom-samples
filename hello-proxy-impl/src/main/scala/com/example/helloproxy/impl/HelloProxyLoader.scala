package com.example.helloproxy.impl

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.grpc.GrpcClientSettings
import akka.grpc.scaladsl.RestartingClient
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

  // A) Create the gRPC client
  //   A.1) gRPC client settings
  private lazy val settings = GrpcClientSettings
    .usingServiceDiscovery(GreeterService.name)
    .withServicePortName("https")
    .withDeadline(Duration.create(5, TimeUnit.SECONDS)) // response timeout
    .withConnectionAttempts(5) // use a small reconnectionAttempts value to cause a client reload in case of failure
  //   A.2) create a client factory
  private lazy val clientConstructor = () => new GreeterServiceClient(settings)(materializer, dispatcher)
  //   A.3) create a restarting client with the client factory to handle reconnections
  lazy val greeterServiceClient: RestartingClient[GreeterServiceClient] = new RestartingClient[GreeterServiceClient](clientConstructor)

  //   A.4) register a shuhtdown task to release resources of the client
  coordinatedShutdown
    .addTask(
      CoordinatedShutdown.PhaseServiceUnbind,
      "shutdown-greeter-service-grpc-client"
    ) { () => greeterServiceClient.close() }

  // B) Create the lagom client. Bind the HelloService client
  lazy val helloService = serviceClient.implement[HelloService]

  // C) Bind the service that this server provides
  override lazy val lagomServer = serverFor[HelloProxyService](wire[HelloProxyServiceImpl])

}
