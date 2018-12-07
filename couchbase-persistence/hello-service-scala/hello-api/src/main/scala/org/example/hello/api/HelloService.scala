package org.example.hello.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

/**
  * The Hello service interface.
  * <p>
  * This describes everything that Lagom needs to know about it.
  */
trait HelloService extends Service {

  /**
    * Get Alice's greeting via write-side
    * <p>
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def hello(id: String): ServiceCall[NotUsed, String]

  /**
    * Change Alice's greeting
    * <p>
    * Example:
    * curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  /**
    * Get all user greetings via read-side
    * <p>
    * Example: curl http://localhost:9000/api/user-greetings
    */
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

/**
  * The greeting message class.
  */
case class GreetingMessage(message: String)

object GreetingMessage {

  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}

/**
  * The user greeting message class.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class UserGreeting(name: String, message: String)

object UserGreeting {

  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[UserGreeting] =
    Json.format[UserGreeting]
}
