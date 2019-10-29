package com.example.shoppingcart.impl

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import org.scalatest.WordSpecLike

class ShoppingCartEntitySpec extends ScalaTestWithActorTestKit(s"""
                                                                  |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
                                                                  |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
                                                                  |akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
                                                                  |""".stripMargin) with WordSpecLike with LogCapturing {

  private def randomId(): String = UUID.randomUUID().toString

  "ShoppingCart" must {
    "add an item" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))
      shoppingCart ! ShoppingCart.AddItem(UUID.randomUUID().toString, 2, probe.ref)

      probe.expectMessageType[ShoppingCart.Accepted]
    }

    "remove an item" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Then remove the item
      shoppingCart ! ShoppingCart.RemoveItem(itemId, probe.ref)
      probe.receiveMessage() match {
        case ShoppingCart.Accepted(summary) => summary.items.contains(itemId) shouldBe false
        case ShoppingCart.Rejected(reason)  => fail(s"Message was rejected with reason: $reason")
      }
    }

    "update item quantity" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Update item quantity
      shoppingCart ! ShoppingCart.AdjustItemQuantity(itemId, 5, probe.ref)
      probe.receiveMessage() match {
        case ShoppingCart.Accepted(summary) => summary.items.get(itemId) shouldBe Some(5)
        case ShoppingCart.Rejected(reason)  => fail(s"Message was rejected with reason: $reason")
      }
    }

    "allow checking out" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item
      shoppingCart ! ShoppingCart.AddItem(randomId(), 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Checkout shopping cart
      shoppingCart ! ShoppingCart.Checkout(probe.ref)
      probe.receiveMessage() match {
        case ShoppingCart.Accepted(summary) => summary.checkedOut shouldBe true
        case ShoppingCart.Rejected(reason)  => fail(s"Message was rejected with reason: $reason")
      }
    }

    "allow getting shopping cart summary" in {
      val probeAdd     = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probeAdd.ref)

      // Get the summary
      // Use another probe since ShoppingCart.Get does not return a ShoppingCart.Confirmation
      val probeGet = createTestProbe[ShoppingCart.Summary]()
      shoppingCart ! ShoppingCart.Get(probeGet.ref)
      probeGet.receiveMessage().items.get(itemId) shouldBe Some(2)
    }

    "fail when removing an item that isn't added" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Removing is idempotent, so command will not be Rejected
      val toRemoveItemId = randomId()
      shoppingCart ! ShoppingCart.RemoveItem(toRemoveItemId, probe.ref)
      probe.receiveMessage() match {
        case ShoppingCart.Accepted(summary) => summary.items.get(itemId) shouldBe Some(2)
        case ShoppingCart.Rejected(reason)  => fail(s"Message was rejected with reason: $reason")
      }
    }

    "fail when adding a negative number of items" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      val quantity = -2
      shoppingCart ! ShoppingCart.AddItem(randomId(), quantity, probe.ref)
      probe.expectMessage(ShoppingCart.Rejected("Quantity must be greater than zero"))
    }

    "fail when adjusting item quantity to negative number" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item so it is possible to checkout
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      val quantity = -2
      shoppingCart ! ShoppingCart.AdjustItemQuantity(itemId, quantity, probe.ref)
      probe.expectMessage(ShoppingCart.Rejected("Quantity must be greater than zero"))
    }

    "fail when adjusting quantity for an item that isn't added" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      val itemId = randomId()
      shoppingCart ! ShoppingCart.AdjustItemQuantity(itemId, 2, probe.ref)
      probe.expectMessage(ShoppingCart.Rejected(s"Cannot adjust quantity for item '$itemId'. Item not present on cart"))
    }

    "fail when adding an item to a checked out cart" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item so it is possible to checkout
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Then checkout the shopping cart
      shoppingCart ! ShoppingCart.Checkout(probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Then fail when adding new items
      shoppingCart ! ShoppingCart.AddItem(randomId(), 2, probe.ref)
      probe.expectMessage(ShoppingCart.Rejected("Cannot add an item to a checked-out cart"))
    }

    "fail when checking out twice" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // First add a item so it is possible to checkout
      val itemId = randomId()
      shoppingCart ! ShoppingCart.AddItem(itemId, 2, probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Then checkout the shopping cart
      shoppingCart ! ShoppingCart.Checkout(probe.ref)
      probe.expectMessageType[ShoppingCart.Accepted]

      // Then fail to checkout again
      shoppingCart ! ShoppingCart.Checkout(probe.ref)
      probe.expectMessage(ShoppingCart.Rejected("Cannot checkout a checked-out cart"))
    }

    "fail when checking out an empty cart" in {
      val probe        = createTestProbe[ShoppingCart.Confirmation]()
      val shoppingCart = spawn(ShoppingCart(PersistenceId("ShoppingCart", randomId())))

      // Fail to checkout empty shopping cart
      shoppingCart ! ShoppingCart.Checkout(probe.ref)
      probe.expectMessage(ShoppingCart.Rejected("Cannot checkout an empty shopping cart"))
    }
  }
}
