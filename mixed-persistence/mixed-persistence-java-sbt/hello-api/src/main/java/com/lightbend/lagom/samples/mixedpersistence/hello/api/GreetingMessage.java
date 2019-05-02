package com.lightbend.lagom.samples.mixedpersistence.hello.api;

import lombok.NonNull;
import lombok.Value;

@Value
public final class GreetingMessage {
    @NonNull String message;
}
