package com.example.shoppingcart.impl

import java.time.OffsetDateTime

import akka.Done
import akka.NotUsed
import com.example.shoppingcart.api.ShoppingCart
import com.example.shoppingcart.api.ShoppingCartItem
import com.example.shoppingcart.api.ShoppingCartReport
import com.example.shoppingcart.api.ShoppingCartService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import scala.concurrent.duration._
import akka.util.Timeout
import akka.cluster.sharding.typed.scaladsl.EntityRef

/**
 * Implementation of the `ShoppingCartService`.
 */
class ShoppingCartServiceImpl(
    clusterSharding: ClusterSharding,
    persistentEntityRegistry: PersistentEntityRegistry,
    reportRepository: ShoppingCartReportRepository
)(implicit ec: ExecutionContext)
    extends ShoppingCartService {

  /**
   * Looks up the shopping cart entity for the given ID.
   */
  private def entityRef(id: String): EntityRef[ShoppingCartCommand[_]] =
    clusterSharding.entityRefFor(ShoppingCartState.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def get(id: String): ServiceCall[NotUsed, ShoppingCart] = ServiceCall { _ =>
    entityRef(id)
      .ask(reply => Get(reply))
      .map(cart => convertShoppingCart(id, cart))
  }

  override def updateItem(id: String): ServiceCall[ShoppingCartItem, Done] = ServiceCall { update =>
    entityRef(id)
      .ask(reply => UpdateItem(update.productId, update.quantity, reply))
      .map {
        case Accepted         => Done
        case Rejected(reason) => throw BadRequest(reason)
      }
  }

  override def checkout(id: String): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    entityRef(id)
      .ask(Checkout)
      .map {
        case Accepted         => Done
        case Rejected(reason) => throw BadRequest(reason)
      }
  }

  override def shoppingCartTopic: Topic[ShoppingCart] = TopicProducer.taggedStreamWithOffset(ShoppingCartEvent.Tag) {
    (tag, fromOffset) =>
      persistentEntityRegistry
        .eventStream(tag, fromOffset)
        .filter(_.event.isInstanceOf[CheckedOut])
        .mapAsync(4) {
          case EventStreamElement(id, _, offset) =>
            entityRef(id)
              .ask(Get)
              .map(cart => convertShoppingCart(id, cart) -> offset)
        }
  }

  private def convertShoppingCart(id: String, cart: CurrentState) = {
    ShoppingCart(
      id,
      cart.state.items.map((ShoppingCartItem.apply _).tupled).toSeq,
      cart.state.checkedOut
    )
  }

  override def getReport(cartId: String): ServiceCall[NotUsed, ShoppingCartReport] = ServiceCall { _ =>
    reportRepository.findById(cartId).map {
      case Some(cart) => cart
      case None       => throw NotFound(s"Couldn't find a shopping cart report for $cartId")
    }
  }
}
