package com.example.inventory.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.stream.scaladsl.Flow
import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.inventory.api.InventoryService
import com.example.shoppingcart.api.ShoppingCartView
import com.example.shoppingcart.api.ShoppingCartService

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/**
 * Implementation of the inventory service.
 *
 * This just stores the inventory in memory, so it will be lost on restart, and also won't work
 * with more than one replicas, but it's enough to demonstrate things working.
 */
class InventoryServiceImpl(shoppingCartService: ShoppingCartService) extends InventoryService {

  private val inventory = TrieMap.empty[String, AtomicInteger]

  private def getInventory(itemId: String) = inventory.getOrElseUpdate(itemId, new AtomicInteger)

  shoppingCartService.shoppingCartTopic.subscribe.atLeastOnce(Flow[ShoppingCartView].map { cart =>
    // Since this is at least once event handling, we really should track by shopping cart, and
    // not update inventory if we've already seen this shopping cart. But this is an in memory
    // inventory tracker anyway, so no need to be that careful.
    cart.items.foreach { item =>
      getInventory(item.itemId).addAndGet(-item.quantity)
    }
    Done
  })

  override def get(itemId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(inventory.get(itemId).fold(0)(_.get()))
  }

  override def add(itemId: String): ServiceCall[Int, Done] = ServiceCall { quantity =>
    getInventory(itemId).addAndGet(quantity)
    Future.successful(Done)
  }
}
