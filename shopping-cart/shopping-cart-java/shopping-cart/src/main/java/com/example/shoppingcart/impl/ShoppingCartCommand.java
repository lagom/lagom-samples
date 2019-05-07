package com.example.shoppingcart.impl;

import akka.Done;
import com.example.shoppingcart.api.ShoppingCart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * This interface defines all the commands that the {@link ShoppingCartEntity} supports.
 * <p>
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface ShoppingCartCommand extends Jsonable {
    /**
     * A command to update an item.
     *
     * It has a reply type of {@link akka.Done}, which is sent back to the caller
     * when all the events emitted by this command are successfully persisted.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class UpdateItem implements ShoppingCartCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        public final String productId;
        public final int quantity;

        @JsonCreator
        UpdateItem(String productId, int quantity) {
            this.productId = Preconditions.checkNotNull(productId, "productId");
            this.quantity = quantity;
        }
    }

    /**
     * A command to get the current state of the shopping cart.
     *
     * The reply type is the {@link ShoppingCartState}
     */
    enum Get implements ShoppingCartCommand, PersistentEntity.ReplyType<ShoppingCartState> {
        INSTANCE
    }

    /**
     * A command to checkout the shopping cart.
     *
     * The reply type is the Done, which will be returned when the events have been
     * emitted.
     */
    enum Checkout implements ShoppingCartCommand, PersistentEntity.ReplyType<Done> {
        INSTANCE
    }
}
