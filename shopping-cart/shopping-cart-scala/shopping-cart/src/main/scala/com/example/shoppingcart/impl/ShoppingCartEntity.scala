package com.example.shoppingcart.impl

import akka.Done
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.journal.Tagged
import akka.persistence.typed.ExpectingReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.ReplyEffect
import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import com.lightbend.lagom.scaladsl.persistence.AggregateEventShards
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import play.api.libs.json.Format
import play.api.libs.json._

import scala.collection.immutable.Seq
import java.time.Instant

/**
 * The current state held by the persistent entity.
 */
case class ShoppingCartState(items: Map[String, Int], checkedOut: Boolean) {

  def applyCommand(cmd: ShoppingCartCommand[_]): ReplyEffect[ShoppingCartEvent, ShoppingCartState] =
    cmd match {
      case x: UpdateItem => onUpdateItem(x)
      case x: Checkout   => onCheckout(x)
      case x: Get        => onReadState(x)
    }

  def onUpdateItem(cmd: UpdateItem): ReplyEffect[ShoppingCartEvent, ShoppingCartState] =
    cmd match {
      case UpdateItem(_, qty, _) if qty < 0 =>
        Effect.reply(cmd)(Rejected("Quantity must be greater than zero"))

      case UpdateItem(productId, 0, _) if !items.contains(productId) =>
        Effect.reply(cmd)(Rejected("Cannot delete item that is not already in cart"))

      case UpdateItem(productId, quantity, _) =>
        Effect
          .persist(ItemUpdated(productId, quantity, Instant.now()))
          .thenReply(cmd) { _ =>
            Accepted
          }
    }

  def onCheckout(cmd: Checkout): ReplyEffect[ShoppingCartEvent, ShoppingCartState] =
    if (items.isEmpty)
      Effect.reply(cmd)(Rejected("Cannot checkout empty cart"))
    else
      Effect
        .persist(CheckedOut(Instant.now()))
        .thenReply(cmd) { _ =>
          Accepted
        }

  def onReadState(cmd: Get): ReplyEffect[ShoppingCartEvent, ShoppingCartState] =
    Effect.reply(cmd)(CurrentState(this))

  def applyEvent(evt: ShoppingCartEvent): ShoppingCartState = {
    evt match {
      case ItemUpdated(productId, quantity, _) => updateItem(productId, quantity)
      case CheckedOut(_)                       => checkout
    }
  }

  private def updateItem(productId: String, quantity: Int) = {
    quantity match {
      case 0 => copy(items = items - productId)
      case _ => copy(items = items + (productId -> quantity))
    }
  }

  private def checkout = copy(checkedOut = true)
}

object ShoppingCartState {

  def empty: ShoppingCartState = ShoppingCartState(Map.empty, checkedOut = false)

  val typeKey = EntityTypeKey[ShoppingCartCommand[_]]("ShoppingCartEntity")

  def behavior(entityContext: EntityContext): Behavior[ShoppingCartCommand[_]] = {

    val persistenceId = typeKey.persistenceIdFrom(entityContext.entityId)

    EventSourcedBehavior
      .withEnforcedReplies[ShoppingCartCommand[_], ShoppingCartEvent, ShoppingCartState](
        persistenceId = persistenceId,
        emptyState = ShoppingCartState.empty,
        commandHandler = (cart, cmd) => cart.applyCommand(cmd),
        eventHandler = (cart, evt) => cart.applyEvent(evt)
      )
      .withTagger(LagomTaggerAdapter(entityContext, ShoppingCartEvent.Tag))

  }

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
  val Tag = AggregateEventTag.sharded[ShoppingCartEvent](numShards = 10)
}

/**
 * An event that represents a item updated event.
 */
final case class ItemUpdated(productId: String, quantity: Int, eventTime: Instant) extends ShoppingCartEvent

object ItemUpdated {

  /**
   * Format for the item updated event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[ItemUpdated] = Json.format
}

final case class CheckedOut(eventTime: Instant) extends ShoppingCartEvent

object CheckedOut {

  /**
   * Format for the checked out event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[CheckedOut] = Json.format
}

sealed trait ShoppingCartReply

object ShoppingCartReply {
  implicit val format: Format[ShoppingCartReply] =
    new Format[ShoppingCartReply] {

      override def reads(json: JsValue): JsResult[ShoppingCartReply] = {
        if ((json \ "state").isDefined)
          Json.fromJson[CurrentState](json)
        else
          Json.fromJson[Confirmation](json)
      }

      override def writes(o: ShoppingCartReply): JsValue = {
        o match {
          case conf: Confirmation  => Json.toJson(conf)
          case state: CurrentState => Json.toJson(state)
        }
      }
    }
}

sealed trait Confirmation extends ShoppingCartReply

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

sealed trait Accepted extends Confirmation

case object Accepted extends Accepted {
  implicit val format: Format[Accepted] = Format(
    Reads(_ => JsSuccess(Accepted)),
    Writes(_ => Json.obj())
  )
}

case class Rejected(reason: String) extends Confirmation

object Rejected {
  implicit val format: Format[Rejected] = Json.format
}

final case class CurrentState(state: ShoppingCartState) extends ShoppingCartReply

object CurrentState {
  implicit val format: Format[CurrentState] = Json.format
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
sealed trait ShoppingCartCommand[R <: ShoppingCartReply] extends ExpectingReply[R] with ShoppingCartCommandSerializable

/**
 * A command to update an item.
 *
 * It has a reply type of [[Done]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
case class UpdateItem(productId: String, quantity: Int, replyTo: ActorRef[Confirmation])
    extends ShoppingCartCommand[Confirmation]

/**
 * A command to get the current state of the shopping cart.
 *
 * The reply type is the ShoppingCart, and will contain the message to say to that
 * person.
 */
case class Get(replyTo: ActorRef[CurrentState]) extends ShoppingCartCommand[CurrentState]

/**
 * A command to checkout the shopping cart.
 *
 * The reply type is the Done, which will be returned when the events have been
 * emitted.
 */
case class Checkout(replyTo: ActorRef[Confirmation]) extends ShoppingCartCommand[Confirmation]

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
    JsonSerializer[ShoppingCartState],
    JsonSerializer[ItemUpdated],
    JsonSerializer[CheckedOut],
    // the replies use play-json as well
    JsonSerializer[ShoppingCartReply],
    JsonSerializer[CurrentState],
    JsonSerializer[Confirmation],
    JsonSerializer[Accepted],
    JsonSerializer[Rejected]
  )
}
