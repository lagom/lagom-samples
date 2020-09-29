package com.example.helloproxy.impl

import akka.actor.ActorSystem
import akka.discovery.ServiceDiscovery.{ Resolved, ResolvedTarget }
import akka.discovery.{ Lookup, ServiceDiscovery }
import akka.grpc.scaladsl.AkkaGrpcClient
import akka.stream.Materializer
import akka.{ Done, NotUsed }
import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.{ ProvidesAdditionalConfiguration, ServiceCall }
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.typesafe.config.ConfigFactory
import example.myapp.helloworld.grpc.{ GreeterServiceClient, HelloReply, HelloRequest }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future, Promise }

class HelloProxyServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  "HelloProxy service" should {

    "roundtrip over HTTP" in {
      client.proxyViaHttp("Alice").invoke().map { answer =>
        answer should ===("Stubbed - Hi Alice!")
      }
    }

    "roundtrip over gRPC" in {
      client.proxyViaGrpc("Alice").invoke().map { answer =>
        answer should ===("Stubbed - Hi Alice! (gRPC)")
      }
    }

  }

  // ------------------------------------------------------------------------

  private val server: ServiceTest.TestServer[HelloProxyApplication] =
    ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
      new HelloProxyApplication(ctx)
        with LocalServiceLocator
        with ProvidesAdditionalConfiguration {

        // uses Lagom's `ProvidesAdditionalConfiguration` cake layer to setup
        // a test-friendly "akka.discovery.method" (see below)
        override def additionalConfiguration =
          super.additionalConfiguration ++ ConfigFactory.parseString(
            s"akka.discovery.method = ${classOf[PlaceholderServiceDiscovery].getName}"
          )

        // overrides the HTTP client to inject a stub
        override lazy val helloService = new HelloServiceClientStub
        override lazy val greeterServiceClient: GreeterServiceClient = new GreeterServiceClientStub
      }
    }

  val client: HelloProxyService = server.serviceClient.implement[HelloProxyService]

  override protected def afterAll(): Unit = {
    server.stop()
  }


  implicit val mat: Materializer = server.materializer

}

class HelloServiceClientStub extends HelloService {
  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ => Future.successful(s"Stubbed - Hi $id!")
  }
}

class GreeterServiceClientStub extends GreeterServiceClient with StubbedAkkaGrpcClient {
  def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Stubbed - Hi ${in.name}! (gRPC)"))
}


// ------------------------------------------------------------------------
// ------------------------------------------------------------------------

// At the moment, gRPC client obtains a `SimpleServiceDiscovery` from the ActorSystem default settings
// but this test doesn't exercise that `SimpleServiceDiscovery` instance so we use a noop placeholder.
class PlaceholderServiceDiscovery(system: ActorSystem) extends ServiceDiscovery {
  implicit val exCtx: ExecutionContext = system.dispatcher

  override def lookup(lookup: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = Future {
    Resolved(lookup.serviceName, immutable.Seq(ResolvedTarget("localhost", None, None)))
  }
}

protected trait StubbedAkkaGrpcClient extends AkkaGrpcClient {
  private val _closed = Promise[Done]()

  override def close(): Future[Done] = {
    _closed.trySuccess(Done)
    _closed.future
  }

  override def closed(): Future[Done] = _closed.future
}
