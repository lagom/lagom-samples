package com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.NonNull;
import lombok.Value;

/**
 * This interface defines all the events that the HelloEntity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface HelloEvent extends Jsonable, AggregateEvent<HelloEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<HelloEvent> TAG = AggregateEventTag.sharded(HelloEvent.class, 4);

    /**
     * An event that represents a change in greeting message.
     */
    @Value
    final class GreetingMessageChanged implements HelloEvent {
        @NonNull String name;
        @NonNull String message;
    }

    @Override
    default AggregateEventTagger<HelloEvent> aggregateTag() {
        return TAG;
    }
}
