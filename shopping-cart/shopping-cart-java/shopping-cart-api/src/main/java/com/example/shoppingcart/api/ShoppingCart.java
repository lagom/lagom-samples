package com.example.shoppingcart.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.List;

/**
 * A shopping cart.
 */
@Value
@JsonDeserialize
public final class ShoppingCart {
    /**
     * The ID of the shopping cart.
     */
    public final String id;

    /**
     * The list of items in the cart.
     */
    public final List<ShoppingCartItem> items;

    /**
     * Whether this cart has been checked out.
     */
    public final boolean checkedOut;

    @JsonCreator
    public ShoppingCart(String id, List<ShoppingCartItem> items, boolean checkedOut) {
        this.id = Preconditions.checkNotNull(id, "id");
        this.items = Preconditions.checkNotNull(items, "items");
        this.checkedOut = checkedOut;
    }
}
