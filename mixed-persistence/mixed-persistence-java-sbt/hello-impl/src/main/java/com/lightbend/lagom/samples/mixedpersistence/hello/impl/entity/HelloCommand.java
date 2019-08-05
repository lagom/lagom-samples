package com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.samples.mixedpersistence.hello.api.UserGreeting;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.NonNull;
import lombok.Value;

import com.google.common.base.Preconditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This interface defines all the commands that the HelloEntity supports.
 * <p>
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface HelloCommand extends Jsonable {

    /**
     * A command to switch the greeting message.
     * <p>
     * It has a reply type of {@link akka.Done}, which is sent back to the caller
     * when all the events emitted by this command are successfully persisted.
     */
    @Value
    @JsonDeserialize
    final class UseGreetingMessage implements HelloCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        @NonNull String message;

        @JsonCreator
        public UseGreetingMessage(@NonNull String message) {
            this.message = Preconditions.checkNotNull(message, "message must NOT be null");
        }
    }

    /**
     * A command to say hello to someone using the current greeting message.
     * <p>
     * The reply type is String, and will contain the message to say to that
     * person.
     */
    @Value
    @JsonDeserialize
    final class Hello implements HelloCommand, PersistentEntity.ReplyType<String> {
        @NonNull String name;

        @JsonCreator
        public Hello(@NonNull String name) {
            this.name = Preconditions.checkNotNull(name, "name must NOT be null");
        }
    }

}
