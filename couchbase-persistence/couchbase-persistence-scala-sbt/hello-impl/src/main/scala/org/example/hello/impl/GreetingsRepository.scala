package org.example.hello.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.couchbase.scaladsl.CouchbaseSession
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.lightbend.lagom.scaladsl.persistence.couchbase.CouchbaseReadSide
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEventTag, EventStreamElement, ReadSideProcessor }
import org.example.hello.api.UserGreeting

import scala.concurrent.{ ExecutionContext, Future }

/**
  *
  */
//#couchbase-begin
class GreetingsRepository(couchbaseSession: CouchbaseSession)(implicit executionContext: ExecutionContext) {

  def listUserGreetings() = {
    import scala.collection.JavaConverters._
    couchbaseSession.get(HelloEventProcessor.UserGreetingsDocId).map {
      case Some(jsonDoc) =>
        val json = jsonDoc.content()
        json
          .getNames()
          .asScala
          .map(name => UserGreeting(name, json.getString(name)))
          .toList
      case None => List.empty[UserGreeting]
    }
  }
}

private object HelloEventProcessor{
  private[impl] val UserGreetingsDocId = "users-actual-greetings"
}

private class HelloEventProcessor(
                                   couchbaseReadSide: CouchbaseReadSide,
                                   actorSystem: ActorSystem
                                 )(implicit executionContext: ExecutionContext)
  extends ReadSideProcessor[HelloEvent] {

  import HelloEventProcessor._

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[HelloEvent] = {

    couchbaseReadSide
      .builder[HelloEvent]("all-greetings")
      .setGlobalPrepare(createDocument _)
      .setEventHandler[GreetingMessageChanged](processGreetingMessageChanged _)
      .build()
  }

  private def createDocument(session: CouchbaseSession): Future[Done] =
    session.get(UserGreetingsDocId).flatMap {
      case Some(doc) => Future.successful(Done)
      case None =>
        session
          .upsert(JsonDocument.create(UserGreetingsDocId, JsonObject.empty()))
          .map(_ => Done)
    }

  private def processGreetingMessageChanged(
                                             session: CouchbaseSession,
                                             ese: EventStreamElement[GreetingMessageChanged]
                                           ): Future[Done] =
    session
      .get(UserGreetingsDocId)
      .flatMap { maybeDoc =>
        val json = maybeDoc match {
          case Some(doc) => doc.content()
          case None => JsonObject.create();
        }
        val evt = ese.event
        json.put(evt.name, evt.message)
        session.upsert(JsonDocument.create(UserGreetingsDocId, json))
      }
      .map(_ => Done)

  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] =
    HelloEvent.Tag.allTags
}

//#couchbase-end
