package com.example.shoppingcart.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A shopping cart.
 */
@Value
@JsonDeserialize
public final class ShoppingCartView {
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

    /**
     * When this cart was checked out.
     */
    public final Optional<Instant> checkoutDate;

    @JsonCreator
    public ShoppingCartView(String id, List<ShoppingCartItem> items, Optional<Instant> checkoutDate) {
        this.id = Preconditions.checkNotNull(id, "id");
        this.items = Preconditions.checkNotNull(items, "items");
        this.checkoutDate = checkoutDate;
        this.checkedOut = checkoutDate.isPresent();
    }
}
