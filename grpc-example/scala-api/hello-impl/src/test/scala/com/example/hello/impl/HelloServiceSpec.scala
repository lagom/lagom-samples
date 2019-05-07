package com.example.hello.impl

import akka.stream.Materializer
import com.example.hello.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.grpc.AkkaGrpcClientHelpers
import example.myapp.helloworld.grpc.{ GreeterServiceClient, HelloRequest }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup.withSsl(true)
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  val client: HelloService = server.serviceClient.implement[HelloService]
  val grpcClient: GreeterServiceClient = AkkaGrpcClientHelpers.grpcClient(
    server,
    GreeterServiceClient.apply,
  )

  implicit val mat: Materializer = server.materializer

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
