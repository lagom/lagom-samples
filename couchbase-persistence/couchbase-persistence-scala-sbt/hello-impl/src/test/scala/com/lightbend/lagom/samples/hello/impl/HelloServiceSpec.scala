package com.lightbend.lagom.sampleshello.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.lightbend.lagom.sampleshello.api._

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    //#couchbase-begin
    // Since the `HelloApplication` already mixes in CouchbaseComponents the TestServer
    // only needs to enable the cluster. Note that Lagom doesn't provide a managed Couchbase
    // so there's no `withCouchbase(true)` on the Lagom testkit like there is a `withCassandra`.
    // You must have a Couchbase server running manually before running the tests. Connection settings
    // will be picked up from `application.conf`
    ServiceTest.defaultSetup
      .withCluster(true)
    //#couchbase-end
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[HelloService]

  override protected def afterAll() = server.stop()

  "hello service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
