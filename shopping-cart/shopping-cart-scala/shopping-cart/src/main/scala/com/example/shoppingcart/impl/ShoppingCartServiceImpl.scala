package com.example.shoppingcart.impl

import akka.{Done, NotUsed}
import com.example.shoppingcart.api.{ShoppingCart, ShoppingCartItem, ShoppingCartService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.ExecutionContext

/**
  * Implementation of the `ShoppingCartService`.
  */
class ShoppingCartServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends ShoppingCartService {

  /**
    * Looks up the shopping cart entity for the given ID.
    */
  private def entityRef(id: String) =
    persistentEntityRegistry.refFor[ShoppingCartEntity](id)

  override def get(id: String): ServiceCall[NotUsed, ShoppingCart] = ServiceCall { _ =>
    entityRef(id)
      .ask(Get)
      .map(cart => convertShoppingCart(id, cart))
  }

  override def updateItem(id: String): ServiceCall[ShoppingCartItem, Done] = ServiceCall { update =>
    entityRef(id)
      .ask(UpdateItem(update.productId, update.quantity))
      .recover {
        case ShoppingCartException(message) => throw BadRequest(message)
      }
  }

  override def checkout(id: String): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    entityRef(id)
      .ask(Checkout)
      .recover {
        case ShoppingCartException(message) => throw BadRequest(message)
      }
  }

  override def shoppingCartTopic: Topic[ShoppingCart] = TopicProducer.singleStreamWithOffset { fromOffset =>
    persistentEntityRegistry.eventStream(ShoppingCartEvent.Tag, fromOffset)
      .filter(_.event.isInstanceOf[CheckedOut.type])
      .mapAsync(4) {
        case EventStreamElement(id, _, offset) =>
          entityRef(id)
            .ask(Get)
            .map(cart => convertShoppingCart(id, cart) -> offset)
      }
    }

  private def convertShoppingCart(id: String, cart: ShoppingCartState) = {
    ShoppingCart(id, cart.items.map((ShoppingCartItem.apply _).tupled).toSeq, cart.checkedOut)
  }
}
