package com.lightbend.lagom.samples.hello.impl

import akka.Done
import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import com.lightbend.lagom.samples.hello.api.{ Greeting, HelloService }
import com.lightbend.lagom.samples.hello.impl.readside.{ GreetingsRepository, ReadSideGreeting }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.lightbend.lagom.samples.hello.api


class HelloServiceImpl(
  greetingsRepository: GreetingsRepository,
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
    extends HelloService {

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[HelloCommand] =
    clusterSharding.entityRefFor(HelloState.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given ID.
      val ref = entityRef(id)

      // Ask the aggregate instance the Hello command.
      ref
        .ask[Greeting](replyTo => Hello(id, replyTo))
        .map(greeting => greeting.message)
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the sharded entity (aka the aggregate instance) for the given ID.
    val ref = entityRef(id)

    // Tell the aggregate to use the greeting message specified.
    ref
      .ask[Confirmation](
        replyTo => UseGreetingMessage(request.message, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't upgrade the greeting message.")
      }
  }

  override def allGreetings(): ServiceCall[NotUsed, Seq[api.Greeting]] =
    ServiceCall { _ =>
      greetingsRepository
        .getAll()
        .map(_.map(toApi).toSeq)
    }

  private def toApi: ReadSideGreeting => api.Greeting = { readSideModel =>
    api.Greeting(readSideModel.name, readSideModel.message)
  }
}
