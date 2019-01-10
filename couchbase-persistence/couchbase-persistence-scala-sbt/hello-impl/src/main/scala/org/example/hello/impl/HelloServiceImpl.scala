package org.example.hello.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.example.hello.api.{ HelloService, UserGreeting }

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext

class HelloServiceImpl(
  persistentEntityRegistry: PersistentEntityRegistry,
  greetingsRepository: GreetingsRepository
)(implicit ec: ExecutionContext)
    extends HelloService {

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

  override def userGreetings(): ServiceCall[NotUsed, Seq[UserGreeting]] =
    ServiceCall { _ =>
      greetingsRepository.listUserGreetings()
    }

}
