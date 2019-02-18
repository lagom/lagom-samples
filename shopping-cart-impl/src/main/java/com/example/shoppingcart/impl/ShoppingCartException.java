package com.example.shoppingcart.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

@Value
@JsonDeserialize
public class ShoppingCartException extends RuntimeException implements Jsonable {
    public final String message;

    @JsonCreator
    public ShoppingCartException(String message) {
        super(message);
        this.message = message;
    }
}
