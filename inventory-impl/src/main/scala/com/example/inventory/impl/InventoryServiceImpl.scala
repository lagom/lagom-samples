package com.example.inventory.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.inventory.api.InventoryService
import com.example.shoppingcart.api.{ShoppingCart, ShoppingCartService}

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

  private def getInventory(productId: String) = inventory.getOrElseUpdate(productId, new AtomicInteger)

  shoppingCartService.shoppingCartTopic.subscribe.atLeastOnce(Flow[ShoppingCart].map { cart =>
    // Since this is at least once event handling, we really should track by shopping cart, and
    // not update inventory if we've already seen this shopping cart. But this is an in memory
    // inventory tracker anyway, so no need to be that careful.
    cart.items.foreach { item =>
      getInventory(item.productId).addAndGet(-item.quantity)
    }
    Done
  })

  override def get(productId: String): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(inventory.get(productId).fold(0)(_.get()))
  }

  override def add(productId: String): ServiceCall[Int, Done] = ServiceCall { quantity =>
    getInventory(productId).addAndGet(quantity)
    Future.successful(Done)
  }
}
