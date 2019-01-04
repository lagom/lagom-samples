package org.example.hello.impl

import akka.NotUsed
import akka.stream.alpakka.couchbase.scaladsl.CouchbaseSession
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.example.hello.api.{HelloService, UserGreeting}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl(
  persistentEntityRegistry: PersistentEntityRegistry,
  couchbaseSession: CouchbaseSession
)(implicit ec: ExecutionContext)
    extends HelloService {

  import org.example.hello.impl.HelloEventProcessor.UserGreetingsDocId

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the hello entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the hello entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }

  //#couchbase-begin
  override def userGreetings(): ServiceCall[NotUsed, Seq[UserGreeting]] =
    ServiceCall { _ =>
      couchbaseSession.get(UserGreetingsDocId).map {
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
  //#couchbase-end

}
