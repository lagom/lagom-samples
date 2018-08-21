package com.example.filedownload.impl

import com.example.filedownload.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.OptionValues._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }
import play.api.http.HeaderNames


class FileDownloadServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new FileDownloadApplication(ctx) with LocalServiceLocator
  }

  val client: FileDownloadService = server.serviceClient.implement[FileDownloadService]

  override protected def afterAll(): Unit = server.stop()

  "File Download service" should {
    "return the expected employee data" in {
      client.downloadEmployees().invoke().map { employees =>
        employees shouldEqual EmployeeRepository.Employees
      }
    }

    "return 'content-type: text/csv' header" in {
      client.downloadEmployees().withResponseHeader.invoke().map { case (headers, _) =>
        headers.protocol.contentType shouldEqual Some("text/csv")
      }
    }

    "return 'content-disposition: attachment' header" in {
      client.downloadEmployees().withResponseHeader.invoke().map { case (headers, _) =>
        headers.getHeader(HeaderNames.CONTENT_DISPOSITION).value should startWith("attachment")
      }
    }
  }

}
