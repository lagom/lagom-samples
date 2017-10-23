package com.lightbend.lagom.recipes.mixedpersistence.hello.api;

import lombok.NonNull;
import lombok.Value;

@Value
public final class GreetingMessage {
    @NonNull String message;
}
