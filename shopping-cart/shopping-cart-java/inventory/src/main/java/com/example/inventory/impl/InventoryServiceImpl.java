package com.example.inventory.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.example.shoppingcart.api.ShoppingCartView;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import com.example.shoppingcart.api.ShoppingCartService;
import com.example.inventory.api.InventoryService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the InventoryService.
 */
@Singleton
public class InventoryServiceImpl implements InventoryService {
    private final ConcurrentMap<String, AtomicInteger> inventory = new ConcurrentHashMap<>();

    @Inject
    public InventoryServiceImpl(ShoppingCartService shoppingCartService) {

        // Subscribe to the shopping cart topic
        shoppingCartService.shoppingCartTopic().subscribe()
            // Since this is at least once event handling, we really should track by shopping cart, and
            // not update inventory if we've already seen this shopping cart. But this is an in memory
            // inventory tracker anyway, so no need to be that careful.
            .atLeastOnce(
                // Create a flow that emits a Done for each message it processes
                Flow.<ShoppingCartView>create().map(cart -> {
                    cart.getItems().forEach(item ->
                        getInventory(item.getItemId()).addAndGet(-item.getQuantity())
                    );
                    return Done.getInstance();
                })
            );

    }

    private AtomicInteger getInventory(String productId) {
        return inventory.computeIfAbsent(productId, k -> new AtomicInteger());
    }

    @Override
    public ServiceCall<NotUsed, Integer> get(String productId) {
        return notUsed -> CompletableFuture.completedFuture(
            getInventory(productId).get()
        );
    }

    @Override
    public ServiceCall<Integer, Done> add(String productId) {
        return quantity -> {
            getInventory(productId).addAndGet(quantity);
            return CompletableFuture.completedFuture(Done.getInstance());
        };
    }
}
