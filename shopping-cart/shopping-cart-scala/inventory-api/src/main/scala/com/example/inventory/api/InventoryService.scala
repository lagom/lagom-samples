package com.example.inventory.api

import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall

/**
 * The inventory service interface.
 *
 * This describes everything that Lagom needs to know about how to serve and
 * consume the inventory service.
 */
trait InventoryService extends Service {

  /**
   * Get the inventory level for the given item id.
   */
  def get(itemId: String): ServiceCall[NotUsed, Int]

  /**
   * Add inventory to the given item id.
   */
  def add(itemId: String): ServiceCall[Int, Done]

  final override def descriptor = {
    import Service._

    named("inventory")
      .withCalls(
        restCall(Method.GET, "/inventory/:itemId", get _),
        restCall(Method.POST, "/inventory/:itemId", add _)
      )
      .withAutoAcl(true)
  }
}
