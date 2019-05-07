package com.example.shoppingcart.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * This interface defines all the events that the {@link ShoppingCartEntity} supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface ShoppingCartEvent extends Jsonable, AggregateEvent<ShoppingCartEvent> {
    /**
     * The tag for shopping cart events, used for consuming the Journal event stream later.
     */
    AggregateEventTag<ShoppingCartEvent> TAG = AggregateEventTag.of(ShoppingCartEvent.class);

    /**
     * An event that represents a item updated event.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class ItemUpdated implements ShoppingCartEvent {
        public final String shoppingCartId;
        public final String productId;
        public final int quantity;

        @JsonCreator
        ItemUpdated(String shoppingCartId, String productId, int quantity) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
            this.productId = Preconditions.checkNotNull(productId, "productId");
            this.quantity = quantity;
        }
    }

    /**
     * An event that represents a checked out event.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class CheckedOut implements ShoppingCartEvent {
        public final String shoppingCartId;

        @JsonCreator
        CheckedOut(String shoppingCartId) {
            this.shoppingCartId = Preconditions.checkNotNull(shoppingCartId, "shoppingCartId");
        }
    }

    @Override
    default AggregateEventTagger<ShoppingCartEvent> aggregateTag() {
        return TAG;
    }
}
