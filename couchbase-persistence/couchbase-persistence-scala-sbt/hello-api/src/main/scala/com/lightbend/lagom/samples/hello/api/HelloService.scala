package com.lightbend.lagom.samples.hello.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

trait HelloService extends Service {

  def hello(id: String): ServiceCall[NotUsed, String]

  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  def userGreetings(): ServiceCall[NotUsed, Seq[UserGreeting]]

  override final def descriptor = {
    import com.lightbend.lagom.scaladsl.api.Service._
    named("hello")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        pathCall("/api/hello/:id", useGreeting _),
        pathCall("/api/user-greetings", userGreetings _)
      )
      .withAutoAcl(true)
  }
}

case class GreetingMessage(message: String)

object GreetingMessage {
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}

case class UserGreeting(name: String, message: String)

object UserGreeting {
  implicit val format: Format[UserGreeting] = Json.format[UserGreeting]
}
