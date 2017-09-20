/*
 * 
 */
package com.lightbend.lagom.recipes.consumer.hello.impl;

import com.lightbend.lagom.recipes.consumer.hello.api.HelloService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(HelloService.class, HelloServiceImpl.class);
    }
}
