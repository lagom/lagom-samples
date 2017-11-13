package com.lightbend.lagom.recipes.hello.impl.readside

import java.sql.{ Connection, ResultSet }

import com.lightbend.lagom.recipes.hello.impl.{ GreetingMessageChanged, HelloEvent }
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith
import com.lightbend.lagom.scaladsl.persistence.jdbc.{ JdbcReadSide, JdbcSession }
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEventTag, EventStreamElement, ReadSide, ReadSideProcessor }

import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

case class ReadSideGreeting(name: String, message: String)

class GreetingsRepository(
                           session: JdbcSession,
                           readSide: ReadSide,
                           eventProcessor: GreetingsEventProcessor
                         )(
                           implicit ec: ExecutionContext
                         ) {

  readSide.register(eventProcessor)

  def getAll(): Future[Seq[ReadSideGreeting]] = {
    session.withConnection { conn: Connection =>
      tryWith(conn.prepareStatement("SELECT * from greetings;").executeQuery) {
        parse
      }
    }
  }

  private def parse(rs: ResultSet): Seq[ReadSideGreeting] = {
    val greetings = mutable.Buffer.empty[ReadSideGreeting]
    while (rs.next()) {
      greetings += ReadSideGreeting(
        rs.getString("name"),
        rs.getString("message")
      )
    }
    greetings.toIndexedSeq
  }

}


class GreetingsEventProcessor(readSide: JdbcReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[HelloEvent] {

  val createTableSql =
    "CREATE TABLE IF NOT EXISTS PUBLIC.greetings (name NVARCHAR2(64), message NVARCHAR2(512), PRIMARY KEY(name))"

  // This is a convenience for creating the read-side table in development mode.
  val buildTables: Connection => Unit = { connection =>
    JdbcSession.tryWith(connection.createStatement()) {
      _.executeUpdate(createTableSql)
    }
  }

  val processGreetingMessageChanged: (Connection, EventStreamElement[GreetingMessageChanged]) => Unit = {
    (connection, eventElement) =>
      JdbcSession.tryWith(
        // "MERGE" is H2's equivalent to 'INSERT OR UPDATE'.
        // See http://www.h2database.com/html/grammar.html#merge
        // We use "MERGE" here because we want this read-side to keep only the lastest message per each name
        // Since 'name' is the table Primary Key then merging is trivial.
        connection.prepareStatement("MERGE INTO greetings (name, message) VALUES (?, ?)")
      ) { statement =>
        statement.setString(1, eventElement.entityId)
        statement.setString(2, eventElement.event.message)
        statement.executeUpdate()
      }
  }

  override def buildHandler() =
    readSide
      .builder[HelloEvent]("GreetingsReadSide")
      .setGlobalPrepare(buildTables)
      .setEventHandler(processGreetingMessageChanged)
      .build()

  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] = Set(HelloEvent.Tag)

}
