package com.example.shoppingcart.impl

import java.time.Instant

import akka.Done
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag }
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, _}

import scala.collection.immutable.Seq
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter

final case class ShoppingCart(items: Map[String, Int], checkedOut: Boolean) {

  //The shopping cart behavior changes if it's checked out or not. The command handles are different for each case.
  def applyCommand(cmd: ShoppingCartCommand): ReplyEffect[ShoppingCartEvent, ShoppingCart] = 
    if (checkedOut) applyCommandForCheckedOut(cmd)
    else applyCommandForOpen(cmd)

  // command handlers for open shopping cart
  private def applyCommandForOpen(cmd: ShoppingCartCommand): ReplyEffect[ShoppingCartEvent, ShoppingCart] = 
    cmd match {
      case AddItem(itemId, quantity, replyTo) =>
        if (items.contains(itemId))
          Effect.reply(replyTo)(Rejected(s"Item '$itemId' was already added to this shopping cart"))
        else if (quantity <= 0)
          Effect.reply(replyTo)(Rejected("Quantity must be greater than zero"))
        else
          Effect
            .persist(ItemAdded(itemId, quantity))
            .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))

      case RemoveItem(itemId, replyTo) =>
        if (items.contains(itemId))
          Effect
            .persist(ItemRemoved(itemId))
            .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
        else
          Effect.reply(replyTo)(Accepted(toSummary(this))) // removing an item is idempotent

      case AdjustItemQuantity(itemId, quantity, replyTo) =>
        if(items.contains(itemId))
          Effect
            .persist(ItemQuantityAdjusted(itemId, quantity))
            .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
        else
          Effect.reply(replyTo)(Rejected(s"Cannot adjust quantity for item '$itemId'. Item not present on cart"))

      // check it out
      case Checkout(replyTo) =>
        Effect
          .persist(CartCheckedOut(Instant.now()))
          .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))

      case Get(replyTo) =>
        Effect.reply(replyTo)(toSummary(this))
    }

  private def toSummary(shoppingCart: ShoppingCart): ShoppingCartSummary = 
    ShoppingCartSummary(shoppingCart.items, shoppingCart.checkedOut)

  // command handlers for checked-out shopping cart
  private def applyCommandForCheckedOut(cmd: ShoppingCartCommand): ReplyEffect[ShoppingCartEvent, ShoppingCart] = 
    cmd match {
      // it is allowed to read it's state of a checked-out cart
      case Get(replyTo) =>
        Effect.reply(replyTo)(toSummary(this))

      // CheckedOut is a final state, no mutations allowed
      case AddItem(_, _, replyTo) =>
        Effect.reply(replyTo)(Rejected("Cannot add an item to a checked-out cart"))
      case RemoveItem(_, replyTo) =>
        Effect.reply(replyTo)(Rejected("Cannot remove an item from a checked-out cart"))
      case AdjustItemQuantity(_, _, replyTo) =>
        Effect.reply(replyTo)(Rejected("Cannot adjust an item quantity on a checked-out cart"))
      case Checkout(replyTo) =>
        Effect.reply(replyTo)(Rejected("Cannot checkout a checked-out cart"))

    }

  // we don't make a distinction of checked or open for the event handler
  // because a checked-out cart will never persist any new event
  def applyEvent(evt: ShoppingCartEvent): ShoppingCart =
    evt match {
      case ItemAdded(itemId, quantity) => addOrUpdateItem(itemId, quantity)
      case ItemRemoved(itemId) => removeItem(itemId)
      case ItemQuantityAdjusted(itemId, quantity) => addOrUpdateItem(itemId, quantity)
      case CartCheckedOut(checkedOutTime) => copy(checkedOut = true)
    }

    private def removeItem(itemId: String) = copy(items = items - itemId)
    private def addOrUpdateItem(itemId: String, quantity: Int) =
      copy(items = items + (itemId -> quantity))

}


object ShoppingCart {
  
  val empty = ShoppingCart(Map.empty, checkedOut = false)
  
  val typeKey = EntityTypeKey[ShoppingCartCommand]("ShoppingCart")

  def behavior(entityContext: EntityContext[ShoppingCartCommand]): Behavior[ShoppingCartCommand] = 
    EventSourcedBehavior
      .withEnforcedReplies[ShoppingCartCommand, ShoppingCartEvent, ShoppingCart](
        persistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId),
        emptyState = ShoppingCart.empty,
        commandHandler = (cart, cmd) => cart.applyCommand(cmd),
        eventHandler = (cart, evt) => cart.applyEvent(evt)
      )
      .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, ShoppingCartEvent.Tag))    
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))

  /**
   *
   * The aggregate get snapshotted every configured number of events. This
   * means the state gets stored to the database, so that when the entity gets
   * loaded, you don't need to replay all the events, just the ones since the
   * snapshot. Hence, a JSON format needs to be declared so that it can be
   * serialized and deserialized when storing to and from the database.
   */
  implicit val format: Format[ShoppingCart] = Json.format
}

/**
 * This interface defines all the events that the ShoppingCartEntity supports.
 */
sealed trait ShoppingCartEvent extends AggregateEvent[ShoppingCartEvent] {
  def aggregateTag = ShoppingCartEvent.Tag
}

object ShoppingCartEvent {
  val Tag = AggregateEventTag.sharded[ShoppingCartEvent](numShards = 10)
}

/**
 * An event that represents an item added event.
 */
final case class ItemAdded(itemId: String, quantity: Int)
  extends ShoppingCartEvent

object ItemAdded {

  /**
   * Format for the ItemAdded event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[ItemAdded] = Json.format
}
  /**
 * An event that represents an item removed event.
 */
final case class ItemRemoved(itemId: String)
  extends ShoppingCartEvent

object ItemRemoved {

  /**
   * Format for the ItemRemoved event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[ItemRemoved] = Json.format
}

/**
 * An event that represents the adjustment of an item quantity event.
 */
final case class ItemQuantityAdjusted(itemId: String, newQuantity: Int)
  extends ShoppingCartEvent


object ItemQuantityAdjusted {

  /**
   * Format for the ItemQuantityAdjusted event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[ItemQuantityAdjusted] = Json.format
}

final case class CartCheckedOut(eventTime: Instant) extends ShoppingCartEvent

object CartCheckedOut {

  /**
   * Format for the checked out event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[CartCheckedOut] = Json.format
}


sealed trait Confirmation

case object Confirmation {
  implicit val format: Format[Confirmation] = new Format[Confirmation] {
    override def reads(json: JsValue): JsResult[Confirmation] = {
      if ((json \ "reason").isDefined)
        Json.fromJson[Rejected](json)
      else
        Json.fromJson[Accepted](json)
    }

    override def writes(o: Confirmation): JsValue = {
      o match {
        case acc: Accepted => Json.toJson(acc)
        case rej: Rejected => Json.toJson(rej)
      }
    }
  }
}


final case class Accepted(summay: ShoppingCartSummary) extends Confirmation

object Accepted {
  implicit val format: Format[Accepted] = Json.format
}

final case class Rejected(reason: String) extends Confirmation

object Rejected {
  implicit val format: Format[Rejected] = Json.format
}


final case class ShoppingCartSummary(items: Map[String, Int], checkedOut: Boolean)

object ShoppingCartSummary {
  implicit val format: Format[ShoppingCartSummary] = Json.format
}

/**
 * This is a marker trait for shopping cart commands.
 * We will serialize them using Akka's Jackson support that is able to deal with the replyTo field.
 * (see application.conf)
 */
trait ShoppingCartCommandSerializable

/**
 * This interface defines all the commands that the ShoppingCartEntity supports.
 */
sealed trait ShoppingCartCommand extends ShoppingCartCommandSerializable

/**
 * A command to update an item.
 *
 * It has a reply type of [[Done]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
final case class AddItem(itemId: String,
                         quantity: Int,
                         replyTo: ActorRef[Confirmation])
  extends ShoppingCartCommand

final case class RemoveItem(itemId: String,
                            replyTo: ActorRef[Confirmation]) extends ShoppingCartCommand

final case class AdjustItemQuantity(itemId: String,
                                    quantity: Int,
                                    replyTo: ActorRef[Confirmation]) extends ShoppingCartCommand
  
/**
 * A command to checkout the shopping cart.
 *
 * The reply type is the Done, which will be returned when the events have been
 * emitted.
 */
final case class Checkout(replyTo: ActorRef[Confirmation]) extends ShoppingCartCommand
  
/**
 * A command to get the current state of the shopping cart.
 *
 * The reply type is the ShoppingCart, and will contain the message to say to that
 * person.
 */
final case class Get(replyTo: ActorRef[ShoppingCartSummary]) extends ShoppingCartCommand

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
    // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
    JsonSerializer[ShoppingCart],
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemRemoved],
    JsonSerializer[ItemQuantityAdjusted],
    JsonSerializer[CartCheckedOut],
    // the replies use play-json as well
    JsonSerializer[ShoppingCartSummary],
    JsonSerializer[Confirmation],
    JsonSerializer[Accepted],
    JsonSerializer[Rejected]
  )
}
