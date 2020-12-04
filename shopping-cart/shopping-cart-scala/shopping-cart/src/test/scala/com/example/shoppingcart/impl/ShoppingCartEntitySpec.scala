package com.example.shoppingcart.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.actor.BootstrapSetup
import akka.actor.setup.ActorSystemSetup
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.typed.PersistenceId
import com.example.shoppingcart.impl.ShoppingCart.AddItem
import com.example.shoppingcart.impl.ShoppingCart.Confirmation
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike

object  ShoppingCartEntitySpec {
  val testConfig =   ConfigFactory.parseString(
    """
      |
      |akka.actor {
      |  serialization-bindings {
      |    # Commands won't use play-json but Akka's jackson support.
      |    # See https://doc.akka.io/docs/akka/2.6/serialization-jackson.html
      |    "com.example.shoppingcart.impl.ShoppingCart$CommandSerializable" = jackson-json
      |  }
      |}
      |
      |akka.actor {
      |  serialization-identifiers {
      |    "com.lightbend.lagom.scaladsl.playjson.PlayJsonSerializer" = 1000004
      |  }
      |}
      |
      |""".stripMargin)

  private def typedActorSystem(name: String, config: Config): typed.ActorSystem[Nothing] = {

    val setup: ActorSystemSetup =
      ActorSystemSetup(
        BootstrapSetup(classLoader = Some(classOf[ShoppingCartEntitySpec].getClassLoader), config = Some(config), None),
        JsonSerializerRegistry.serializationSetupFor(ShoppingCartSerializerRegistry)
      )
    import akka.actor.typed.scaladsl.adapter._
    ActorSystem(name, setup).toTyped
  }

}

class ShoppingCartEntitySpec
  extends ScalaTestWithActorTestKit(
    ShoppingCartEntitySpec.typedActorSystem("ShoppingCartEntitySpec",
      EventSourcedBehaviorTestKit.config.withFallback(ShoppingCartEntitySpec.testConfig)
    )
  )
    with AnyWordSpecLike {

  private def randomId(): String = UUID.randomUUID().toString

  "ShoppingCart" must {
    "add an item" in {
      val entity = EventSourcedBehaviorTestKit[ShoppingCart.Command, ShoppingCart.Event, ShoppingCart](
        system,
        ShoppingCart(PersistenceId("ShoppingCart", randomId()))
      )

      val result = entity.runCommand(AddItem("1", 1, _))
      result.reply shouldBe a[Confirmation]
    }
  }
}
