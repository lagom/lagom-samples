package com.example.filedownload.impl

import akka.NotUsed
import com.example.filedownload.api.FileDownloadService
import com.example.filedownload.api.FileDownloadService.Employees
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.http.HeaderNames

import scala.concurrent.ExecutionContext

class FileDownloadServiceImpl(employeeRepository: EmployeeRepository)(implicit executionContext: ExecutionContext)
  extends FileDownloadService {

  override def downloadEmployees(): ServiceCall[NotUsed, Employees] = ServerServiceCall { (_, _) =>
    employeeRepository.allEmployees().map { employees =>
      val responseHeader =
        ResponseHeader.Ok
          // Setting the Content-Disposition header prompts browsers to
          // download the file rather than display it inline.
          .addHeader(HeaderNames.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
      (responseHeader, employees)
    }
  }

}
