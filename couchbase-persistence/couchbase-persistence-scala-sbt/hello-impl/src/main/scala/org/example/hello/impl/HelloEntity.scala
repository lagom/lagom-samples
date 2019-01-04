package org.example.hello.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{
  AggregateEvent,
  AggregateEventTag,
  PersistentEntity
}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{
  JsonSerializer,
  JsonSerializerRegistry
}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class HelloEntity extends PersistentEntity {

  override type Command = HelloCommand[_]
  override type Event = HelloEvent
  override type State = HelloState

  override def initialState: HelloState = HelloState("Hello", LocalDateTime.now.toString)

  override def behavior: Behavior = {
    case HelloState(message, _) =>
      Actions()
        .onCommand[UseGreetingMessage, Done] {
          case (UseGreetingMessage(newMessage), ctx, state) =>
            ctx.thenPersist(GreetingMessageChanged(entityId, newMessage)) { _ =>
              ctx.reply(Done)
            }
        }
        .onReadOnlyCommand[Hello, String] {
          case (Hello(name), ctx, state) =>
            ctx.reply(s"$message, $name!")
        }
        .onEvent {
          case (GreetingMessageChanged(_, newMessage), state) =>
            HelloState(newMessage, LocalDateTime.now().toString)
        }
  }
}

case class HelloState(message: String, timestamp: String)

object HelloState {
  implicit val format: Format[HelloState] = Json.format
}

sealed trait HelloEvent extends AggregateEvent[HelloEvent] {
  def aggregateTag = HelloEvent.Tag
}

object HelloEvent {
  val Tag = AggregateEventTag.sharded[HelloEvent](4)
}

case class GreetingMessageChanged(name: String, message: String)
    extends HelloEvent

object GreetingMessageChanged {
  implicit val format: Format[GreetingMessageChanged] = Json.format
}

sealed trait HelloCommand[R] extends ReplyType[R]

case class UseGreetingMessage(message: String) extends HelloCommand[Done]

object UseGreetingMessage {
  implicit val format: Format[UseGreetingMessage] = Json.format
}

case class Hello(name: String) extends HelloCommand[String]

object Hello {
  implicit val format: Format[Hello] = Json.format
}

object HelloSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[UseGreetingMessage],
    JsonSerializer[Hello],
    JsonSerializer[GreetingMessageChanged],
    JsonSerializer[HelloState]
  )
}
