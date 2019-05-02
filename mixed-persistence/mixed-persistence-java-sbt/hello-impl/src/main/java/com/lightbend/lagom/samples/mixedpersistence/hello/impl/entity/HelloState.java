package com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity;

import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.NonNull;
import lombok.Value;

/**
 * The state for the {@link HelloEntity} entity.
 */
@Value
public final class HelloState implements CompressedJsonable {
    @NonNull String message;
    @NonNull String timestamp;
}
