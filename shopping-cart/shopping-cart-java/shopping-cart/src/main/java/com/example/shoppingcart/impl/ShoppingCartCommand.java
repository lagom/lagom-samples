package com.example.shoppingcart.impl;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.persistence.typed.ExpectingReply;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
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
public interface ShoppingCartCommand<R> extends ExpectingReply<R>, Jsonable {
    /**
     * A command to update an item.
     *
     * It has a reply type of {@link akka.Done}, which is sent back to the caller
     * when all the events emitted by this command are successfully persisted.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class UpdateItem implements ShoppingCartCommand<ShoppingCartReply.Confirmation>, CompressedJsonable {
        public final String productId;
        public final int quantity;
        public final ActorRef<ShoppingCartReply.Confirmation> replyTo;

        @JsonCreator
        UpdateItem(String productId, int quantity, ActorRef<ShoppingCartReply.Confirmation> replyTo) {
            this.productId = Preconditions.checkNotNull(productId, "productId");
            this.quantity = quantity;
            this.replyTo = replyTo;
        }

        @Override
        public ActorRef<ShoppingCartReply.Confirmation> replyTo() {
            return replyTo;
        }
    }

    /**
     * A command to get the current state of the shopping cart.
     *
     * The reply type is the {@link ShoppingCartState}
     */
    final class Get implements ShoppingCartCommand<ShoppingCartReply.CurrentState> {

        private final ActorRef<ShoppingCartReply.CurrentState> replyTo;

        public Get(ActorRef<ShoppingCartReply.CurrentState> replyTo) {
            this.replyTo = replyTo;
        }

        @Override
        public ActorRef<ShoppingCartReply.CurrentState> replyTo() {
            return replyTo;
        }
    }

    /**
     * A command to checkout the shopping cart.
     *
     * The reply type is the Done, which will be returned when the events have been
     * emitted.
     */
    final class Checkout implements ShoppingCartCommand<ShoppingCartReply.Confirmation> {

        private final ActorRef<ShoppingCartReply.Confirmation> replyTo;

        public Checkout(ActorRef<ShoppingCartReply.Confirmation> replyTo) {
            this.replyTo = replyTo;
        }

        @Override
        public ActorRef<ShoppingCartReply.Confirmation> replyTo() {
            return replyTo;
        }
    }
}
