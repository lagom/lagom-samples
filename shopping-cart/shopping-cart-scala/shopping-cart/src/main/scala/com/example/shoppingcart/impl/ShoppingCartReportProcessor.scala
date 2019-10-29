package com.example.shoppingcart.impl

import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import slick.dbio.DBIOAction
import akka.Done
import com.example.shoppingcart.impl.ShoppingCart._

class ShoppingCartReportProcessor(readSide: SlickReadSide, repository: ShoppingCartReportRepository)
    extends ReadSideProcessor[Event] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[Event] =
    readSide
      .builder[Event]("shopping-cart-report")
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

  override def aggregateTags: Set[AggregateEventTag[Event]] = Event.Tag.allTags
}
