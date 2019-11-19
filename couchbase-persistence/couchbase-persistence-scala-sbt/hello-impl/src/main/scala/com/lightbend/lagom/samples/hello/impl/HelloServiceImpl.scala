package com.lightbend.lagom.samples.hello.impl

import akka.Done
import akka.NotUsed
import akka.util.Timeout
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.samples.hello.api.HelloService
import com.lightbend.lagom.samples.hello.api.UserGreeting
import com.lightbend.lagom.scaladsl.api.transport.BadRequest

import scala.concurrent.duration._
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

class HelloServiceImpl(
  greetingsRepository: GreetingsRepository,
  clusterSharding: ClusterSharding
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
        replyTo => UseGreetingMessage(id, request.message, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't upgrade the greeting message.")
      }
  }
  override def userGreetings(): ServiceCall[NotUsed, Seq[UserGreeting]] =
    ServiceCall { _ =>
      greetingsRepository.listUserGreetings()
    }

}
