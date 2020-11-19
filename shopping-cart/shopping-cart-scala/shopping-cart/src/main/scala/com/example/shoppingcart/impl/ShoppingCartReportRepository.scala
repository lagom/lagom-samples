package com.example.shoppingcart.impl

import java.time.Instant

import akka.Done
import com.example.shoppingcart.api.ShoppingCartReport
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The report repository manage the storage of ShoppingCartReport which is a API class (view model).
 *
 * It saves data in a ready for consumption format for that specific API model.
 * If the API changes, we must regenerated the stored models.
 */
class ShoppingCartReportRepository(database: Database) {

  class ShoppingCartReportTable(tag: Tag) extends Table[ShoppingCartReport](tag, "shopping_cart_report") {
    def cartId = column[String]("cart_id", O.PrimaryKey)

    def creationDate = column[Instant]("creation_date")

    def checkoutDate = column[Option[Instant]]("checkout_date")

    def * = (cartId, creationDate, checkoutDate).mapTo[ShoppingCartReport]
  }

  val reportTable = TableQuery[ShoppingCartReportTable]

  def createTable() = reportTable.schema.createIfNotExists

  def findById(id: String): Future[Option[ShoppingCartReport]] =
    database.run(findByIdQuery(id))

  def createReport(cartId: String): DBIO[Done] = {
    findByIdQuery(cartId)
      .flatMap {
        case None => reportTable += ShoppingCartReport(cartId, Instant.now(), None)
        case _    => DBIO.successful(Done)
      }
      .map(_ => Done)
      .transactionally
  }

  def addCheckoutTime(cartId: String, checkoutDate: Instant): DBIO[Done] = {
    findByIdQuery(cartId)
      .flatMap {
        case Some(cart) => reportTable.insertOrUpdate(cart.copy(checkoutDate = Some(checkoutDate)))
        // if that happens we have a corrupted system
        // cart checkout can only happens for a existing cart
        case None => throw new RuntimeException(s"Didn't find cart for checkout. CartID: $cartId")
      }
      .map(_ => Done)
      .transactionally
  }

  private def findByIdQuery(cartId: String): DBIO[Option[ShoppingCartReport]] =
    reportTable
      .filter(_.cartId === cartId)
      .result
      .headOption
}
