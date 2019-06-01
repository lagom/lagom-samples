package com.lightbend.lagom.samples.mixedpersistence.hello.api;

import lombok.NonNull;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

@Value
@JsonDeserialize
public class UserGreeting {
    @NonNull String id;
    @NonNull String message;

    @JsonCreator
    public UserGreeting(String id, String message) {
        this.id = Preconditions.checkNotNull(id, "id field MUST not be null");
        this.message = Preconditions.checkNotNull(message, "message field MUST not be null");
    }
}
