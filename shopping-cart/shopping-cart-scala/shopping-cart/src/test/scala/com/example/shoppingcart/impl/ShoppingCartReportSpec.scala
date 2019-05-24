package com.example.shoppingcart.impl

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ReadSideTestDriver, ServiceTest}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class ShoppingCartReportSpec extends WordSpec with BeforeAndAfterAll with Matchers with ScalaFutures with OptionValues {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withJdbc(true)) { ctx =>
    new ShoppingCartApplication(ctx) {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val reportRepository = server.application.reportRepository
  private val offset = new AtomicInteger()
  private implicit val  exeCxt = server.actorSystem.dispatcher

  "The shopping cart report processor" should {

    "create a report on first event" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      val eventTime = Instant.now()

      val updatedReport =
        for {
          _ <- feedEvent(cartId, ItemUpdated("test1", 1, eventTime))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report is created on first event") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate shouldBe eventTime
          report.checkoutDate shouldBe None
        }
      }
    }

    "NOT update a report on subsequent ItemUpdated events" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      val eventTime = Instant.now()

      val updatedReport =
        for {
          _ <- feedEvent(cartId, ItemUpdated("test2", 1, eventTime))
          _ <- feedEvent(cartId, ItemUpdated("test2", 2, eventTime.plusSeconds(5)))
          _ <- feedEvent(cartId, ItemUpdated("test2", 3, eventTime.plusSeconds(10)))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report's creationDate should not change") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate shouldBe eventTime
          report.checkoutDate shouldBe None
        }
      }
    }

    "produce a checked-out report on check-out event" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      val eventTime = Instant.now()
      val checkedOutTime = eventTime.plusSeconds(30)

      val updatedReport =
        for {
          _ <- feedEvent(cartId, ItemUpdated("test3", 1, eventTime))
          _ <- feedEvent(cartId, CheckedOut(checkedOutTime))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report is marked as checked-out") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate shouldBe eventTime
          report.checkoutDate shouldBe Some(checkedOutTime)
        }
      }
    }

  }

  private def feedEvent(cartId: String, event: ShoppingCartEvent): Future[Done] = {
    testDriver.feed(cartId, event, Sequence(offset.getAndIncrement))
  }
}
