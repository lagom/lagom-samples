package com.example.shoppingcart.impl;

import com.example.shoppingcart.api.ShoppingCartService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the {@link ShoppingCartService} so that it can be served.
 */
public class ShoppingCartModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(ShoppingCartService.class, ShoppingCartServiceImpl.class);
        bind(ReportRepository.class);
    }
}
