package com.example.hello.impl

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.example.hello.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import example.myapp.helloworld.grpc.{GreeterServiceClient, HelloRequest}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

/**
 * Illustrate deliberate use of the gRPC client without TLS.
 */
class HelloServiceNoSslSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup//.withSsl(true)
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }
  
  implicit val sys: ActorSystem = server.actorSystem

  val client: HelloService = server.serviceClient.implement[HelloService]
  val grpcClient: GreeterServiceClient = {
    val httpPort = server.playServer.httpPort.get

    val settings = GrpcClientSettings
      .connectToServiceAt("127.0.0.1", httpPort)(server.actorSystem)
      .withTls(false)

    GreeterServiceClient(settings)
  }


  override protected def afterAll(): Unit = {
    grpcClient.close()
    server.stop()
  }

  "Hello service" should {

    "say hello over HTTP" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hi Alice!")
      }
    }

    "say hello over gRPC" in {
      grpcClient
        .sayHello(HelloRequest("Alice"))
        .map{
          _.message should be ("Hi Alice! (gRPC)")
        }
    }

  }
}
