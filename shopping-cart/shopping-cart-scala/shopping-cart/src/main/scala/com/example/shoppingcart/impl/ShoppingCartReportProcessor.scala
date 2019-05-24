package com.example.shoppingcart.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide


class ShoppingCartReportProcessor(readSide: SlickReadSide,
                                  repository: ShoppingCartReportRepository) extends ReadSideProcessor[ShoppingCartEvent] {

  override def buildHandler() =
    readSide
      .builder[ShoppingCartEvent]("shopping-cart-view")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[ItemUpdated] { envelope =>
        repository.createReport(envelope.entityId, envelope.event.eventTime)
      }
      .setEventHandler[CheckedOut] { envelope =>
        repository.addCheckoutTime(envelope.entityId, envelope.event.eventTime)
      }
      .build()

  override def aggregateTags = Set(ShoppingCartEvent.Tag)
}
