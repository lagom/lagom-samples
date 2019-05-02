package com.lightbend.lagom.samples.mixedpersistence.hello.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class UserGreeting {
    @NonNull String id;
    @NonNull String message;
}
