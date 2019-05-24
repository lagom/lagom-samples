package com.example.shoppingcart.impl

import java.time.{Instant, OffsetDateTime}

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json._

import scala.collection.immutable.Seq

/**
  * This is an event sourced entity. It has a state,
  * [[com.example.shoppingcart.impl.ShoppingCartState]], which stores the
  * current shopping cart items and whether it's checked out.
  *
  * Event sourced entities are interacted with by sending them commands. This
  * entity supports three commands, an
  * [[com.example.shoppingcart.impl.UpdateItem]] command, which is used to
  * update the quantity of an item in the cart, a
  * [[com.example.shoppingcart.impl.Checkout]] command which is used to set
  * checkout the shopping cart, and a [[com.example.shoppingcart.impl.Get]]
  * command, which is a read only command which returns the current shopping
  * cart state.
  *
  * Commands get translated to events, and it's the events that get persisted
  * by the entity. Each event will have an event handler registered for it, and
  * an event handler simply applies an event to the current state. This will be
  * done when the event is first created, and it will also be done when the
  * entity is loaded from the database - each event will be replayed to
  * recreate the state of the entity.
  *
  * This entity defines two events, the
  * [[com.example.shoppingcart.impl.ItemUpdated]] event, which is emitted when
  * a [[com.example.shoppingcart.impl.UpdateItem]] command is received, and a
  * [[com.example.shoppingcart.impl.CheckedOut]] event, which is emitted when a
  * [[com.example.shoppingcart.impl.Checkout]] command is received.
  */
class ShoppingCartEntity extends PersistentEntity {

  import play.api.libs.functional.syntax._
  def naStringSerializer: Format[Option[String]] =
    implicitly[Format[String]].inmap(
      str => Some(str).filterNot(_ == "N/A"),
      maybeStr => maybeStr.getOrElse("N/A")
    )

  override type Command = ShoppingCartCommand[_]
  override type Event = ShoppingCartEvent
  override type State = ShoppingCartState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: ShoppingCartState = ShoppingCartState(Map.empty, checkedOut = false)

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case ShoppingCartState(_, false) => openShoppingCart
    case ShoppingCartState(_, true) => checkedOut
  }

  def openShoppingCart = {
    Actions().onCommand[UpdateItem, Done] {

      // Command handler for the UpdateItem command
      case (UpdateItem(_, quantity), ctx, _) if quantity < 0 =>
        ctx.commandFailed(ShoppingCartException("Quantity must be greater than zero"))
        ctx.done
      case (UpdateItem(productId, 0), ctx, state) if !state.items.contains(productId) =>
        ctx.commandFailed(ShoppingCartException("Cannot delete item that is not already in cart"))
        ctx.done
      case (UpdateItem(productId, quantity), ctx, _) =>
        // In response to this command, we want to first persist it as a
        // ItemUpdated event
        ctx.thenPersist(
          ItemUpdated(productId, quantity, Instant.now())
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }

    }.onCommand[Checkout.type, Done] {

      // Command handler for the Checkout command
      case (Checkout, ctx, state) if state.items.isEmpty =>
        ctx.commandFailed(ShoppingCartException("Cannot checkout empty cart"))
        ctx.done
      case (Checkout, ctx, _) =>
        // In response to this command, we want to first persist it as a
        // CheckedOut event
        ctx.thenPersist(
          CheckedOut(Instant.now())
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
        ctx.commandFailed(ShoppingCartException("Can't update item on already checked out shopping cart"))
        ctx.done

    }.onCommand[Checkout.type, Done] {

      // Not valid when checked out
      case (_, ctx, _) =>
        ctx.commandFailed(ShoppingCartException("Can't checkout on already checked out shopping cart"))
        ctx.done

    }.onEvent(eventHandlers)
  }

  def eventHandlers: EventHandler = {
    // Event handler for the ItemUpdated event
    case (ItemUpdated(productId: String, quantity: Int, _), state) => state.updateItem(productId, quantity)

    // Event handler for the checkout event
    case (_: CheckedOut, state) => state.checkout
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
case class ItemUpdated(productId: String, quantity: Int, eventTime: Instant) extends ShoppingCartEvent

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
case class CheckedOut(eventTime: Instant) extends ShoppingCartEvent

object CheckedOut {
  /**
   * Format for the checked out event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[CheckedOut] = Json.format
}

/**
  * This interface defines all the commands that the ShoppingCartEntity supports.
  */
sealed trait ShoppingCartCommand[R] extends ReplyType[R]

/**
  * A command to update an item.
  *
  * It has a reply type of `Done`, which is sent back to the caller when
  * all the events emitted by this command are successfully persisted.
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
  * An exception thrown by the shopping cart validation
  *
  * @param message The message
  */
case class ShoppingCartException(message: String) extends RuntimeException(message)

object ShoppingCartException {

  /**
    * Format for the ShoppingCartException.
    *
    * When a command fails, the error needs to be serialized and sent back to
    * the node that requested it, this is used to do that.
    */
  implicit val format: Format[ShoppingCartException] = Json.format[ShoppingCartException]
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
    JsonSerializer[CheckedOut],
    JsonSerializer[UpdateItem],
    JsonSerializer[Checkout.type],
    JsonSerializer[Get.type],
    JsonSerializer[ShoppingCartState],
    JsonSerializer[ShoppingCartException]
  )
}
