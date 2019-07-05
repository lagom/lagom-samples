package com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.NonNull;
import lombok.Value;

/**
 * The state for the {@link HelloEntity} entity.
 */
@Value
@JsonDeserialize
public final class HelloState implements CompressedJsonable {
    @NonNull String message;
    @NonNull String timestamp;

    @JsonCreator
    public HelloState(@NonNull String message, @NonNull String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
