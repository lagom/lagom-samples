package org.example.hello.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.couchbase.scaladsl.CouchbaseSession
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.document.JsonDocument
import com.lightbend.lagom.scaladsl.persistence.{
  AggregateEventTag,
  EventStreamElement,
  ReadSideProcessor
}
import com.lightbend.lagom.scaladsl.persistence.couchbase.CouchbaseReadSide

import scala.concurrent.{ExecutionContext, Future}

//#couchbase-begin
object HelloEventProcessor {
  private[impl] val UserGreetingsDocId = "users-actual-greetings"
}

class HelloEventProcessor(
  couchbaseReadSide: CouchbaseReadSide,
  actorSystem: ActorSystem
)(implicit executionContext: ExecutionContext)
    extends ReadSideProcessor[HelloEvent] {

  import HelloEventProcessor.UserGreetingsDocId

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
          case None      => JsonObject.create();
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
