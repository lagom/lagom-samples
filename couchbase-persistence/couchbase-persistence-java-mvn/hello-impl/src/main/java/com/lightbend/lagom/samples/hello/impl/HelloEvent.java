package com.lightbend.lagom.sampleshello.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface HelloEvent extends Jsonable, AggregateEvent<HelloEvent> {

  AggregateEventShards<HelloEvent> TAG = AggregateEventTag.sharded(HelloEvent.class, 4);

  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class GreetingMessageChanged implements HelloEvent {

    public final String name;
    public final String message;

    @JsonCreator
    public GreetingMessageChanged(String name, String message) {
      this.name = Preconditions.checkNotNull(name, "name");
      this.message = Preconditions.checkNotNull(message, "message");
    }
  }

  @Override
  default AggregateEventTagger<HelloEvent> aggregateTag() {
    return TAG;
  }

}
