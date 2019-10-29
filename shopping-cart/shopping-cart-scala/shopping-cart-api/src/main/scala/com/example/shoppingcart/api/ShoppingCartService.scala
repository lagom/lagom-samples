package com.example.shoppingcart.api

import java.time.Instant

import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.KafkaProperties
import com.lightbend.lagom.scaladsl.api.broker.kafka.PartitionKeyStrategy
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall
import play.api.libs.json.Format
import play.api.libs.json.Json
import java.text.Normalizer.Form

object ShoppingCartService {
  val TOPIC_NAME = "shopping-cart"
}

/**
 * The ShoppingCart service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the ShoppingCartService.
 */
trait ShoppingCartService extends Service {

  /**
   * Get a shopping cart.
   *
   * Example: curl http://localhost:9000/shoppingcart/123
   */
  def get(id: String): ServiceCall[NotUsed, ShoppingCartView]

  /**
   * Get a shopping cart report (view model).
   *
   * Example: curl http://localhost:9000/shoppingcart/123/report
   */
  def getReport(id: String): ServiceCall[NotUsed, ShoppingCartReport]

  /**
   * Add an item in the shopping cart.
   *
   * Example: curl -H "Content-Type: application/json" -X POST -d '{"itemId": 456, "quantity": 2}' http://localhost:9000/shoppingcart/123
   */
  def addItem(id: String): ServiceCall[ShoppingCartItem, ShoppingCartView]

  /**
   * Remove an item in the shopping cart.
   *
   * Example: curl -H "Content-Type: application/json" -X DELETE -d '{"itemId": 456 }' http://localhost:9000/shoppingcart/123/item/456
   */
  def removeItem(id: String, itemId: String): ServiceCall[NotUsed, ShoppingCartView]

  /**
   * Adjust the quantity of an item in the shopping cart.
   *
   * Example: curl -H "Content-Type: application/json" -X PATCH -d '{"quantity": 2}' http://localhost:9000/shoppingcart/123/item/456
   */
  def adjustItemQuantity(id: String, itemId: String): ServiceCall[Quantity, ShoppingCartView]

  /**
   * Checkout the shopping cart.
   *
   * Example: curl -X POST http://localhost:9000/shoppingcart/123/checkout
   */
  def checkout(id: String): ServiceCall[NotUsed, ShoppingCartView]

  /**
   * This gets published to Kafka.
   */
  def shoppingCartTopic: Topic[ShoppingCartView]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("shopping-cart")
      .withCalls(
        restCall(Method.GET, "/shoppingcart/:id", get _),
        restCall(Method.GET, "/shoppingcart/:id/report", getReport _),
        restCall(Method.POST, "/shoppingcart/:id", addItem _),
        restCall(Method.DELETE, "/shoppingcart/:id/item/:itemId", removeItem _),
        restCall(Method.PATCH, "/shoppingcart/:id/item/:itemId", adjustItemQuantity _),
        restCall(Method.POST, "/shoppingcart/:id/checkout", checkout _)
      )
      .withTopics(
        topic(ShoppingCartService.TOPIC_NAME, shoppingCartTopic)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[ShoppingCartView](_.id)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

/**
 * A shopping cart item.
 *
 * @param itemId The ID of the item.
 * @param quantity The quantity of the item.
 */
final case class ShoppingCartItem(itemId: String, quantity: Int)

object ShoppingCartItem {

  /**
   * Format for converting the shopping cart item to and from JSON.
   *
   * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
   */
  implicit val format: Format[ShoppingCartItem] = Json.format
}

final case class Quantity(quantity: Int)

object Quantity {
  implicit val format: Format[Quantity] = Json.format
}

/**
 * A shopping cart.
 *
 * @param id The id of the shopping cart.
 * @param items The items in the shopping cart.
 * @param checkedOut Whether the shopping cart has been checked out (submitted).
 */
final case class ShoppingCartView(id: String, items: Seq[ShoppingCartItem], checkedOut: Boolean)

object ShoppingCartView {
  implicit val format: Format[ShoppingCartView] = Json.format
}

/**
 * A shopping cart report exposes information about a ShoppingCart.
 */
final case class ShoppingCartReport(cartId: String, creationDate: Instant, checkoutDate: Option[Instant])

object ShoppingCartReport {
  implicit val format: Format[ShoppingCartReport] = Json.format
}
