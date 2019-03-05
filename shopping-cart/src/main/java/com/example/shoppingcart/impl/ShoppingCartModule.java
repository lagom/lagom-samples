package com.example.shoppingcart.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import com.example.shoppingcart.api.ShoppingCartService;

/**
 * The module that binds the {@link ShoppingCartService} so that it can be served.
 */
public class ShoppingCartModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(ShoppingCartService.class, ShoppingCartServiceImpl.class);
        bind(ClusterBootstrapStart.class).asEagerSingleton();
    }
}
