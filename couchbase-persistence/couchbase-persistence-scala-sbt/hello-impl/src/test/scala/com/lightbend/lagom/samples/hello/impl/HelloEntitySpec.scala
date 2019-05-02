package com.lightbend.lagom.sampleshello.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }

class HelloEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem(
    "HelloEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(HelloSerializerRegistry)
  )

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(
    block: PersistentEntityTestDriver[HelloCommand[_],
                                      HelloEvent,
                                      HelloState] => Unit
  ): Unit = {
    val driver =
      new PersistentEntityTestDriver(system, new HelloEntity, "hello-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "hello entity" should {

    "say hello by default" in withTestDriver { driver =>
      val outcome = driver.run(Hello("Alice"))
      outcome.replies should contain only "Hello, Alice!"
    }

    "allow updating the greeting message" in withTestDriver { driver =>
      val outcome1 = driver.run(UseGreetingMessage("Hi"))
      outcome1.events should contain only GreetingMessageChanged("hello-1", "Hi")
      val outcome2 = driver.run(Hello("Alice"))
      outcome2.replies should contain only "Hi, Alice!"
    }

  }
}
