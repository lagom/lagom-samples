package com.example.shoppingcart.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import java.time.Instant
import slick.dbio.DBIOAction
import akka.Done

class ShoppingCartReportProcessor(readSide: SlickReadSide, repository: ShoppingCartReportRepository)
    extends ReadSideProcessor[ShoppingCartEvent] {

  override def buildHandler() =
    readSide
      .builder[ShoppingCartEvent]("shopping-cart-report")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[ItemAdded] { envelope =>
        repository.createReport(envelope.entityId) 
      }
      .setEventHandler[ItemRemoved] { envelope =>
        DBIOAction.successful(Done) // not used in report
      }
      .setEventHandler[ItemQuantityAdjusted] { envelope =>
        DBIOAction.successful(Done) // not used in report
      }
      .setEventHandler[CartCheckedOut] { envelope =>
        repository.addCheckoutTime(envelope.entityId, envelope.event.eventTime)
      }
      .build()

  override def aggregateTags = ShoppingCartEvent.Tag.allTags
}
