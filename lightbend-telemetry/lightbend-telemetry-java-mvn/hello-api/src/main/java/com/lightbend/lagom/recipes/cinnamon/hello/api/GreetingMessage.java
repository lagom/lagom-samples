package com.lightbend.lagom.recipes.cinnamon.hello.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

@Value
@JsonDeserialize
public final class GreetingMessage {

    public final String message;

    @JsonCreator
    public GreetingMessage(String message) {
        this.message = Preconditions.checkNotNull(message, "message");
    }
}
