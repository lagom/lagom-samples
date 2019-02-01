package com.example.shoppingcart.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json._

import scala.collection.immutable.Seq

/**
  * This is an event sourced entity. It has a state, [[ShoppingCartState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * entity supports three commands, an [[UpdateItem]] crommand, which is used to
  * update the quantity of an item in the cart, a [[Checkout]] command which is
  * used to set checkout the shopping cart, and a [[Get]] command, which is a read
  * only command which returns the current shopping cart state.
  *
  * Commands get translated to events, and it's the events that get persisted by
  * the entity. Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the entity is
  * loaded from the database - each event will be replayed to recreate the state
  * of the entity.
  *
  * This entity defines two events, the [[ItemUpdated]] event, which is emitted
  * when a [[UpdateItem]] command is received, and a [[CheckedOut]] event, which
  * is emitted when a [[Checkout]] command is received.
  */
class ShoppingCartEntity extends PersistentEntity {

  override type Command = ShoppingCartCommand[_]
  override type Event = ShoppingCartEvent
  override type State = ShoppingCartState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: ShoppingCartState = ShoppingCartState(Map.empty, false)

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case ShoppingCartState(items, false) => openShoppingCart
    case ShoppingCartState(items, true) => checkedOut
  }

  def openShoppingCart = {
    Actions().onCommand[UpdateItem, Done] {

      // Command handler for the UpdateItem command
      case (UpdateItem(productId, quantity), ctx, state) =>
        if (quantity < 0) {
          ctx.commandFailed(BadRequest("Quantity must be greater than zero"))
          ctx.done
        } else if (quantity == 0 && !state.items.contains(productId)) {
          ctx.commandFailed(BadRequest("Cannot delete item that is not already in cart"))
          ctx.done
        } else {
          // In response to this command, we want to first persist it as a
          // ItemUpdated event
          ctx.thenPersist(
            ItemUpdated(productId, quantity)
          ) { _ =>
            // Then once the event is successfully persisted, we respond with done.
            ctx.reply(Done)
          }
        }

    }.onCommand[Checkout.type, Done] {

      // Command handler for the Checkout command
      case (Checkout, ctx, state) =>
        // In response to this command, we want to first persist it as a
        // CheckedOut event
        ctx.thenPersist(
          CheckedOut
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }

    }.onReadOnlyCommand[Get.type, ShoppingCartState] {

      // Command handler for the Hello command
      case (Get, ctx, state) =>
        // Reply with the current state.
        ctx.reply(state)

    }.onEvent(eventHandlers)
  }

  def checkedOut = {
    Actions().onReadOnlyCommand[Get.type, ShoppingCartState] {

      // Command handler for the Hello command
      case (Get, ctx, state) =>
        // Reply with the current state.
        ctx.reply(state)

    }.onCommand[UpdateItem, Done] {

      // Not valid when checked out
      case (_, ctx, _) =>
        ctx.commandFailed(BadRequest("Can't update item on already checked out shopping cart"))
        ctx.done

    }.onCommand[Checkout.type, Done] {

      // Not valid when checked out
      case (_, ctx, _) =>
        ctx.commandFailed(BadRequest("Can't checkout on already checked out shopping cart"))
        ctx.done

    }.onEvent(eventHandlers)
  }

  def eventHandlers: EventHandler = {
    // Event handler for the ItemUpdated event
    case (ItemUpdated(productId: String, quantity: Int), state) => state.updateItem(productId, quantity)

    // Event handler for the checkout event
    case (CheckedOut, state) => state.checkout
  }
}

/**
  * The current state held by the persistent entity.
  */
case class ShoppingCartState(items: Map[String, Int], checkedOut: Boolean) {

  def updateItem(productId: String, quantity: Int) = {
    quantity match {
      case 0 => copy(items = items - productId)
      case _ => copy(items = items + (productId -> quantity))
    }
  }

  def checkout = copy(checkedOut = true)
}

object ShoppingCartState {
  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the entity gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[ShoppingCartState] = Json.format
}

/**
  * This interface defines all the events that the ShoppingCartEntity supports.
  */
sealed trait ShoppingCartEvent extends AggregateEvent[ShoppingCartEvent] {
  def aggregateTag = ShoppingCartEvent.Tag
}

object ShoppingCartEvent {
  val Tag = AggregateEventTag[ShoppingCartEvent]
}

/**
  * An event that represents a item updated event.
  */
case class ItemUpdated(productId: String, quantity: Int) extends ShoppingCartEvent

object ItemUpdated {

  /**
    * Format for the item updated event.
    *
    * Events get stored and loaded from the database, hence a JSON format
    * needs to be declared so that they can be serialized and deserialized.
    */
  implicit val format: Format[ItemUpdated] = Json.format
}

/**
  * An event that represents a checked out event.
  */
case object CheckedOut extends ShoppingCartEvent {

  /**
    * Format for the checked out event.
    *
    * Events get stored and loaded from the database, hence a JSON format
    * needs to be declared so that they can be serialized and deserialized.
    */
  implicit val format: Format[CheckedOut.type] = Format(
    Reads(_ => JsSuccess(CheckedOut)),
    Writes(_ => Json.obj())
  )
}

/**
  * This interface defines all the commands that the ShoppingCartEntity supports.
  */
sealed trait ShoppingCartCommand[R] extends ReplyType[R]

/**
  * A command to update an item.
  *
  * It has a reply type of [[Done]], which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
case class UpdateItem(productId: String, quantity: Int) extends ShoppingCartCommand[Done]

object UpdateItem {

  /**
    * Format for the update item command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[UpdateItem] = Json.format
}

/**
  * A command to get the current state of the shopping cart.
  *
  * The reply type is the ShoppingCart, and will contain the message to say to that
  * person.
  */
case object Get extends ShoppingCartCommand[ShoppingCartState] {

  /**
    * Format for the Get command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[Get.type] = Format(
    Reads(_ => JsSuccess(Get)),
    Writes(_ => Json.obj())
  )
}

/**
  * A command to checkout the shopping cart.
  *
  * The reply type is the Done, which will be returned when the events have been
  * emitted.
  */
case object Checkout extends ShoppingCartCommand[Done] {

  /**
    * Format for the Checkout command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[Checkout.type] = Format(
    Reads(_ => JsSuccess(Checkout)),
    Writes(_ => Json.obj())
  )
}

/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object ShoppingCartSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[ItemUpdated],
    JsonSerializer[CheckedOut.type],
    JsonSerializer[UpdateItem],
    JsonSerializer[Checkout.type],
    JsonSerializer[Get.type],
    JsonSerializer[ShoppingCartState]
  )
}
