package com.example.filedownload.api

import java.net.InetAddress

import akka.NotUsed
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{ NegotiatedDeserializer, NegotiatedSerializer }
import com.lightbend.lagom.scaladsl.api.deser.StrictMessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.{ MessageProtocol, NotAcceptable, UnsupportedMediaType }
import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service, ServiceCall }

import scala.collection.immutable.Seq

trait FileDownloadService extends Service {
  import FileDownloadService._

  def downloadEmployees(): ServiceCall[NotUsed, Employees]

  override final def descriptor: Descriptor = {
    import Service._

    named("file-download")
      .withCalls(
        call(downloadEmployees())
      )
      .withAutoAcl(true)
  }

}

object FileDownloadService {
  type Employees = List[Employee]

  implicit final val EmployeesMessageSerializer: StrictMessageSerializer[Employees] = new StrictMessageSerializer[Employees] {

    override def serializerForRequest: NegotiatedSerializer[Employees, ByteString] = CSVSerializer

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Employees, ByteString] = {
      protocol.contentType match {
        case CSVProtocol.contentType | None => CSVDeserializer
        case _ => throw UnsupportedMediaType(protocol, CSVProtocol)
      }
    }

    override def serializerForResponse(accepted: Seq[MessageProtocol]): NegotiatedSerializer[Employees, ByteString] = {
      accepted match {
        case Nil => CSVSerializer
        case protocols =>
          protocols.collectFirst {
            case MessageProtocol(Some("text/csv" | "text/*" | "*/*" | "*"), _ , _) => CSVSerializer
          }.getOrElse {
            throw NotAcceptable(accepted, CSVProtocol)
          }
      }
    }

    override def acceptResponseProtocols: Seq[MessageProtocol] = List(CSVProtocol)

  }

  // This is a naive implementation of a CSV formatter and parser that doesn't
  // handle quoting, empty data, etc. In a real application, it would be better
  // to use a mature and well-tested library.

  private final val Header = "id,first_name,last_name,email,company,ip_address"
  private final val CSVProtocol = MessageProtocol().withContentType("text/csv")

  object CSVSerializer extends NegotiatedSerializer[Employees, ByteString] {

    override def serialize(employees: Employees): ByteString = {
      val csvRows = Header :: employees.map { employee =>
        List(
          employee.id.toString,
          employee.firstName,
          employee.lastName,
          employee.email,
          employee.company,
          employee.ipAddress.getHostAddress
        ).mkString(",")
      }
      ByteString(csvRows.map(_ + "\r\n").mkString)
    }

    override def protocol: MessageProtocol = CSVProtocol

  }

  object CSVDeserializer extends NegotiatedDeserializer[Employees, ByteString] {

    override def deserialize(employeesCSV: ByteString): Employees = {
      val csvString = employeesCSV.utf8String
      val csvLines = csvString.split("[\r\n]+").toList
      val header = csvLines.head
      if (header != Header) throw new IllegalArgumentException(s"Invalid header format: [$header], expected [$Header]")
      val csvRows = csvLines.tail // drop header row
      csvRows.map { row =>
        row.split("\\s*,\\s*").toList match {
          case id :: firstName :: lastName :: email :: company :: ipAddress :: Nil =>
            Employee(id.toInt, firstName, lastName, email, company, InetAddress.getByName(ipAddress))
          case _ => throw new IllegalArgumentException(s"Invalid row format: [$row], expected [$header]")
        }
      }
    }

  }
}

case class Employee(
  id: Int,
  firstName: String,
  lastName: String,
  email: String,
  company: String,
  ipAddress: InetAddress
)
