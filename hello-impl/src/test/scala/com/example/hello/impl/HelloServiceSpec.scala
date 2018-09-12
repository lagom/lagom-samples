package com.example.hello.impl

import com.example.hello.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[HelloService]

  override protected def afterAll() = server.stop()

  "Hello service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hi Alice!")
      }
    }

  }
}
