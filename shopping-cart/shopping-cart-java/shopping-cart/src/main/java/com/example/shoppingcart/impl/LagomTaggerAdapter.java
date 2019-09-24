package com.example.shoppingcart.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class LagomTaggerAdapter {

    public static <Event extends AggregateEvent<Event>> Function<Event, Set<String>> adapt(String businessId, AggregateEventTagger<Event> lagomTagger) {
        return evt -> {
            Set<String> tags = new HashSet<>();
            if (lagomTagger instanceof AggregateEventTag) {
                tags.add(((AggregateEventTag) lagomTagger).tag());
            } else if (lagomTagger instanceof AggregateEventShards) {
                tags.add(((AggregateEventShards) lagomTagger).forEntityId(businessId).tag());
            }
            return tags;
        };

    }
}
