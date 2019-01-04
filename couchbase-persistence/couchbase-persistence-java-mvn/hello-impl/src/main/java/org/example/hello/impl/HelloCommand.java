package org.example.hello.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface HelloCommand extends Jsonable {

  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  final class UseGreetingMessage implements HelloCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
    public final String message;

    @JsonCreator
    public UseGreetingMessage(String message) {
      this.message = Preconditions.checkNotNull(message, "message");
    }
  }

  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  final class Hello implements HelloCommand, PersistentEntity.ReplyType<String> {

    public final String name;

    @JsonCreator
    public Hello(String name) {
      this.name = Preconditions.checkNotNull(name, "name");
    }
  }

}
