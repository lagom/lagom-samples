package com.lightbend.lagom.samples.mixedpersistence.hello.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.Value;

@Value
@JsonDeserialize
public final class GreetingMessage {
    @NonNull String message;

    @JsonCreator
    public GreetingMessage(@NonNull String message) {
        this.message = message;
    }
}
