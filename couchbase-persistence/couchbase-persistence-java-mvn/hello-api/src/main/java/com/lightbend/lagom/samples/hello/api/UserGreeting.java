package com.lightbend.lagom.sampleshello.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public final class UserGreeting {
  String user;
  String message;

  @JsonCreator
  public UserGreeting(String user, String message) {
    this.user = user;
    this.message = message;
  }
}
