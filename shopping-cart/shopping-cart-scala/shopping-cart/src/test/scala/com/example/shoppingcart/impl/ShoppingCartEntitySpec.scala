package com.example.shoppingcart.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class ShoppingCartEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("ShoppingcartEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(ShoppingCartSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[ShoppingCartCommand[_], ShoppingCartEvent, ShoppingCartState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ShoppingCartEntity, "shoppingcart-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "ShoppingCart entity" should {

    "add an item" in withTestDriver { driver =>
      val outcome = driver.run(UpdateItem("123", 2))
      outcome.replies should contain only Done
      outcome.events should contain only ItemUpdated("123", 2)
      outcome.state should === (ShoppingCartState(Map("123" -> 2), false))
    }

    "remove an item" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2))
      val outcome = driver.run(UpdateItem("123", 0))
      outcome.replies should contain only Done
      outcome.events should contain only ItemUpdated("123", 0)
      outcome.state should === (ShoppingCartState(Map.empty, false))
    }

    "update multiple items" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2)).state should === (ShoppingCartState(
        Map("123" -> 2), false))
      driver.run(UpdateItem("456", 3)).state should === (ShoppingCartState(
        Map("123" -> 2, "456" -> 3), false))
      driver.run(UpdateItem("123", 1)).state should === (ShoppingCartState(
        Map("123" -> 1, "456" -> 3), false))
      driver.run(UpdateItem("456", 0)).state should === (ShoppingCartState(
        Map("123" -> 1), false))
    }

    "allow checking out" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2))
      val outcome = driver.run(Checkout)
      outcome.replies should contain only Done
      outcome.events should contain only CheckedOut
      outcome.state should === (ShoppingCartState(Map("123" -> 2), true))
    }

    "allow getting the state" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2))
      val outcome = driver.run(Get)
      outcome.replies should contain only ShoppingCartState(Map("123" -> 2), false)
      outcome.events should have size 0
    }

    "fail when removing an item that isn't added" in withTestDriver { driver =>
      val outcome = driver.run(UpdateItem("123", 0))
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[ShoppingCartException]
      outcome.events should have size 0
    }

    "fail when adding a negative number of items" in withTestDriver { driver =>
      val outcome = driver.run(UpdateItem("123", -1))
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[ShoppingCartException]
      outcome.events should have size 0
    }

    "fail when adding an item to a checked out cart" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2), Checkout)
      val outcome = driver.run(UpdateItem("456", 3))
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[ShoppingCartException]
      outcome.events should have size 0
    }

    "fail when checking out twice" in withTestDriver { driver =>
      driver.run(UpdateItem("123", 2), Checkout)
      val outcome = driver.run(Checkout)
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[ShoppingCartException]
      outcome.events should have size 0
    }

    "fail when checking out an empty cart" in withTestDriver { driver =>
      val outcome = driver.run(Checkout)
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[ShoppingCartException]
      outcome.events should have size 0
    }

  }
}
