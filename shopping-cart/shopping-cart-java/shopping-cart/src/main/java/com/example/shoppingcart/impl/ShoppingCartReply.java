package com.example.shoppingcart.impl;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

public interface ShoppingCartReply {


    @Value
    @JsonDeserialize
    final class CurrentState implements ShoppingCartReply{
        private final ShoppingCartState shoppingCartState;

        @JsonCreator
        public CurrentState(ShoppingCartState shoppingCartState) {
            this.shoppingCartState = shoppingCartState;
        }
    }

    interface Confirmation extends ShoppingCartReply {}

    @Value
    @JsonDeserialize
    final class Accepted implements Confirmation {
        @JsonCreator
        public Accepted() {
        }
    }

    @Value
    @JsonDeserialize
    final class Rejected implements Confirmation {
        private final String reason;

        @JsonCreator
        public Rejected(String reason) {
            this.reason = reason;
        }
    }

}
