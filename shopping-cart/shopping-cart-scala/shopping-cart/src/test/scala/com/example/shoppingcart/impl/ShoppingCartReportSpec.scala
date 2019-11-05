package com.example.shoppingcart.impl

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ReadSideTestDriver
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.TestTopicComponents
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ShoppingCartReportSpec extends WordSpec with BeforeAndAfterAll with Matchers with ScalaFutures with OptionValues {

  import ShoppingCart._

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withJdbc(true)) { ctx =>
    new LagomApplication(ctx) with ShoppingCartComponents with TestTopicComponents {
      override def serviceLocator: ServiceLocator    = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver()(materializer, executionContext)
    }
  }

  override def afterAll(): Unit = server.stop()

  private val testDriver                        = server.application.readSide
  private val reportRepository                  = server.application.reportRepository
  private val offset                            = new AtomicInteger()
  private implicit val exeCxt: ExecutionContext = server.actorSystem.dispatcher

  "The shopping cart report processor" should {

    "create a report on first event" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      val updatedReport =
        for {
          _      <- feedEvent(cartId, ItemAdded("test1", 1))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report is created on first event") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate should not be null
          report.checkoutDate shouldBe None
        }
      }
    }

    "NOT update a report on subsequent ItemUpdated events" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      // Create a report to check against it later
      var reportCreatedDate: Instant = Instant.now()
      val createdReport = for {
        _      <- feedEvent(cartId, ItemAdded("test2", 1))
        report <- reportRepository.findById(cartId)
      } yield report

      withClue("Cart report created on first event") {
        whenReady(createdReport) { r =>
          reportCreatedDate = r.value.creationDate
        }
      }

      // To ensure that events have a different instant
      SECONDS.sleep(2);

      val updatedReport =
        for {
          _      <- feedEvent(cartId, ItemAdded("test2", 2))
          _      <- feedEvent(cartId, ItemAdded("test2", 3))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report's creationDate should not change") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate shouldBe reportCreatedDate
          report.checkoutDate shouldBe None
        }
      }
    }

    "produce a checked-out report on check-out event" in {
      val cartId = UUID.randomUUID().toString

      withClue("Cart report is not expected to exist") {
        reportRepository.findById(cartId).futureValue shouldBe None
      }

      // Create a report to check against it later
      var reportCreatedDate: Instant = Instant.now()
      val createdReport = for {
        _      <- feedEvent(cartId, ItemAdded("test2", 1))
        report <- reportRepository.findById(cartId)
      } yield report

      withClue("Cart report created on first event") {
        whenReady(createdReport) { r =>
          reportCreatedDate = r.value.creationDate
        }
      }

      // To ensure that events have a different instant
      SECONDS.sleep(2);

      val checkedOutTime = reportCreatedDate.plusSeconds(30)

      val updatedReport =
        for {
          _      <- feedEvent(cartId, ItemAdded("test3", 1))
          _      <- feedEvent(cartId, CartCheckedOut(checkedOutTime))
          report <- reportRepository.findById(cartId)
        } yield report

      withClue("Cart report is marked as checked-out") {
        whenReady(updatedReport) { result =>
          val report = result.value
          report.creationDate shouldBe reportCreatedDate
          report.checkoutDate shouldBe Some(checkedOutTime)
        }
      }
    }

  }

  private def feedEvent(cartId: String, event: ShoppingCart.Event): Future[Done] = {
    testDriver.feed(cartId, event, Sequence(offset.getAndIncrement))
  }
}
