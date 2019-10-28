package com.example.shoppingcart.impl

import java.time.Instant

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger, AkkaTaggerAdapter}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, _}

import scala.collection.immutable.Seq

object ShoppingCart {

  // SHOPPING CART COMMANDS

  // This is a marker trait for shopping cart commands.
  // We will serialize them using Akka's Jackson support that is able to deal with the replyTo field.
  // (see application.conf).
  // Keep in mind that when configuring it on application.conf you need to use the FQCN which is:
  // com.example.shoppingcart.impl.ShoppingCart$CommandSerializable
  // Note the "$".
  trait CommandSerializable

  sealed trait Command extends CommandSerializable

  final case class AddItem(itemId: String, quantity: Int, replyTo: ActorRef[Confirmation]) extends Command

  final case class RemoveItem(itemId: String, replyTo: ActorRef[Confirmation]) extends Command

  final case class AdjustItemQuantity(itemId: String, quantity: Int, replyTo: ActorRef[Confirmation]) extends Command

  final case class Checkout(replyTo: ActorRef[Confirmation]) extends Command

  final case class Get(replyTo: ActorRef[Summary]) extends Command

  // SHOPPING CART EVENTS
  sealed trait Event extends AggregateEvent[Event] {
    override def aggregateTag: AggregateEventTagger[Event] = Event.Tag
  }

  object Event {
    val Tag: AggregateEventShards[Event] = AggregateEventTag.sharded[Event](numShards = 10)
  }

  final case class ItemAdded(itemId: String, quantity: Int) extends Event

  final case class ItemRemoved(itemId: String) extends Event

  final case class ItemQuantityAdjusted(itemId: String, newQuantity: Int) extends Event

  final case class CartCheckedOut(eventTime: Instant) extends Event

  // Events get stored and loaded from the database, hence a JSON format
  //  needs to be declared so that they can be serialized and deserialized.
  implicit val itemAddedFormat: Format[ItemAdded] = Json.format
  implicit val itemRemovedFormat: Format[ItemRemoved] = Json.format
  implicit val itemQuantityAdjustedFormat: Format[ItemQuantityAdjusted] = Json.format
  implicit val cartCheckedOutFormat: Format[CartCheckedOut] = Json.format

  // SHOPPING CART REPLIES
  final case class Summary(items: Map[String, Int], checkedOut: Boolean)

  sealed trait Confirmation

  final case class Accepted(summary: Summary) extends Confirmation

  final case class Rejected(reason: String) extends Confirmation

  implicit val summaryFormat: Format[Summary] = Json.format
  implicit val confirmationAcceptedFormat: Format[Accepted] = Json.format
  implicit val confirmationRejectedFormat: Format[Rejected] = Json.format
  implicit val confirmationFormat: Format[Confirmation] = new Format[Confirmation] {
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

  val empty = ShoppingCart(Map.empty, checkedOut = false)

  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ShoppingCart")

  // We can then access the entity behavior in our test tests, without the need to tag
  // or retain events.
  def behavior(persistenceId: PersistenceId): EventSourcedBehavior[Command, Event, ShoppingCart] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, ShoppingCart](
        persistenceId = persistenceId,
        emptyState = ShoppingCart.empty,
        commandHandler = (cart, cmd) => cart.applyCommand(cmd),
        eventHandler = (cart, evt) => cart.applyEvent(evt)
      )
  }

  def behavior(entityContext: EntityContext[Command]): Behavior[Command] =
    behavior(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
      .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, Event.Tag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))

  /**
   * The aggregate get snapshoted every configured number of events. This
   * means the state gets stored to the database, so that when the entity gets
   * loaded, you don't need to replay all the events, just the ones since the
   * snapshot. Hence, a JSON format needs to be declared so that it can be
   * serialized and deserialized when storing to and from the database.
   */
  implicit val format: Format[ShoppingCart] = Json.format
}

final case class ShoppingCart(items: Map[String, Int], checkedOut: Boolean) {

  import ShoppingCart._

  //The shopping cart behavior changes if it's checked out or not. The command handles are different for each case.
  def applyCommand(cmd: Command): ReplyEffect[Event, ShoppingCart] =
    if (checkedOut) {
      cmd match {
        // it is allowed to read it's state of a checked-out cart
        case Get(replyTo) => Effect.reply(replyTo)(toSummary(this))
        // CheckedOut is a final state, no mutations allowed
        case AddItem(_, _, replyTo) => Effect.reply(replyTo)(Rejected("Cannot add an item to a checked-out cart"))
        case RemoveItem(_, replyTo) => Effect.reply(replyTo)(Rejected("Cannot remove an item from a checked-out cart"))
        case AdjustItemQuantity(_, _, replyTo) => Effect.reply(replyTo)(Rejected("Cannot adjust an item quantity on a checked-out cart"))
        case Checkout(replyTo) => Effect.reply(replyTo)(Rejected("Cannot checkout a checked-out cart"))
      }
    }
    else {
      cmd match {
        case AddItem(itemId, quantity, replyTo)            => addItem(itemId, quantity, replyTo)
        case RemoveItem(itemId, replyTo)                   => removeItem(itemId, replyTo)
        case AdjustItemQuantity(itemId, quantity, replyTo) => adjustItemQuantity(itemId, quantity, replyTo)
        case Checkout(replyTo) => checkout(replyTo)
        case Get(replyTo) =>
          Effect.reply(replyTo)(toSummary(this))
      }
    }

  private def checkout(replyTo: ActorRef[Confirmation]): ReplyEffect[Event, ShoppingCart] = {
    Effect
      .persist(CartCheckedOut(Instant.now()))
      .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
  }

  private def addItem(itemId: String, quantity: Int, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, ShoppingCart] = {
    if (items.contains(itemId))
      Effect.reply(replyTo)(Rejected(s"Item '$itemId' was already added to this shopping cart"))
    else if (quantity <= 0)
      Effect.reply(replyTo)(Rejected("Quantity must be greater than zero"))
    else
      Effect
        .persist(ItemAdded(itemId, quantity))
        .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
  }

  private def removeItem(itemId: String, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, ShoppingCart] = {
    if (items.contains(itemId))
      Effect
        .persist(ItemRemoved(itemId))
        .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
    else
      Effect.reply(replyTo)(Accepted(toSummary(this))) // removing an item is idempotent
  }

  private def adjustItemQuantity(itemId: String, quantity: Int, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, ShoppingCart] = {
    if (items.contains(itemId))
      Effect
        .persist(ItemQuantityAdjusted(itemId, quantity))
        .thenReply(replyTo)(updatedCart => Accepted(toSummary(updatedCart)))
    else
      Effect.reply(replyTo)(Rejected(s"Cannot adjust quantity for item '$itemId'. Item not present on cart"))
  }

  private def toSummary(shoppingCart: ShoppingCart): Summary =
    Summary(shoppingCart.items, shoppingCart.checkedOut)

  // we don't make a distinction of checked or open for the event handler
  // because a checked-out cart will never persist any new event
  def applyEvent(evt: Event): ShoppingCart =
    evt match {
      case ItemAdded(itemId, quantity) => addOrUpdateItem(itemId, quantity)
      case ItemRemoved(itemId) => removeItem(itemId)
      case ItemQuantityAdjusted(itemId, quantity) => addOrUpdateItem(itemId, quantity)
      case CartCheckedOut(checkedOutTime) => copy(checkedOut = true)
    }

  private def removeItem(itemId: String): ShoppingCart = copy(items = items - itemId)

  private def addOrUpdateItem(itemId: String, quantity: Int): ShoppingCart =
    copy(items = items + (itemId -> quantity))
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

  import ShoppingCart._

  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
    JsonSerializer[ShoppingCart],
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemRemoved],
    JsonSerializer[ItemQuantityAdjusted],
    JsonSerializer[CartCheckedOut],
    // the replies use play-json as well
    JsonSerializer[Summary],
    JsonSerializer[Confirmation],
    JsonSerializer[Accepted],
    JsonSerializer[Rejected],
  )
}
