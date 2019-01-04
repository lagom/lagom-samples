package org.example.hello.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import org.example.hello.api._

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    //#couchbase-start
    //TODO: how to override persistence to use Coucbhase instead of Cassandra?
    ServiceTest.defaultSetup
      .withCluster(true)
      .withCassandra(false)
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
