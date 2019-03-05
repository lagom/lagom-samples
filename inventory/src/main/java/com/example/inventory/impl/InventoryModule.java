package com.example.inventory.impl;

import com.example.inventory.api.InventoryService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import com.example.shoppingcart.api.ShoppingCartService;

/**
 * The module that binds the {@link InventoryService} so that it can be served.
 */
public class InventoryModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        // Bind the InventoryService service
        bindService(InventoryService.class, InventoryServiceImpl.class);
        // Bind the ShoppingCartService client
        bindClient(ShoppingCartService.class);
    }
}
