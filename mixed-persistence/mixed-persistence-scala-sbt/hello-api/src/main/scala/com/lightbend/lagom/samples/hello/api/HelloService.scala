package com.lightbend.lagom.samples.hello.api

import akka.{ Done, NotUsed }
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }
import play.api.libs.json.{ Format, Json }

trait HelloService extends Service {

  def hello(id: String): ServiceCall[NotUsed, String]

  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  def allGreetings(): ServiceCall[NotUsed, Seq[Greeting]]

  override final def descriptor = {
    import Service._
    named("hello")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        pathCall("/api/hello/:id", useGreeting _),
        pathCall("/api/greetings", allGreetings)
      )
      .withAutoAcl(true)
  }
}

/**
  * The Greeting is both the message and the person that message is meant for.
  */
case class Greeting(name: String, message: String)

object Greeting {
  implicit val format: Format[Greeting] = Json.format
}

/**
  * This class only models the message of a Greeting
  */
case class GreetingMessage(message: String)

object GreetingMessage {
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}
