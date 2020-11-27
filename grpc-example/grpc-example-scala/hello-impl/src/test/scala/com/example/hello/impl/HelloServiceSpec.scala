package com.example.hello.impl

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.example.hello.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.grpc.AkkaGrpcClientHelpers
import example.myapp.helloworld.grpc.{GreeterServiceClient, HelloRequest}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = 
    ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
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

    "say hello over gRPC with an extra header" in {
      grpcClient
        .sayHello().addHeader("sender", "spec").invoke(HelloRequest("Alice"))
        .map{
          _.message should be ("Hi Alice! (gRPC)")
        }
    }

  }
}
