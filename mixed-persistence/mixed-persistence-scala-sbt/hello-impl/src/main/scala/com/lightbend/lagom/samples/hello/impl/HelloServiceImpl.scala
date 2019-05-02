package com.lightbend.lagom.samples.hello.impl

import akka.NotUsed
import com.lightbend.lagom.samples.hello.api.{ Greeting, HelloService }
import com.lightbend.lagom.samples.hello.impl.readside.{ GreetingsRepository, ReadSideGreeting }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class HelloServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                       greetingsRepository: GreetingsRepository
                      )(
                        implicit ec: ExecutionContext
                      ) extends HelloService {

  override def hello(id: String) = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)
    ref.ask(UseGreetingMessage(request.message))
  }

  override def allGreetings(): ServiceCall[NotUsed, Seq[Greeting]] = ServiceCall { _ =>
    greetingsRepository
      .getAll()
      .map(_.map(toApi))
  }

  private def toApi: ReadSideGreeting => Greeting = {
    readSideModel =>
      Greeting(readSideModel.name, readSideModel.message)
  }
}
