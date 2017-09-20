/*
 * 
 */
package com.example.hellostream.impl;

import com.example.hello.api.HelloService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceInfo;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the HelloStreamService so that it can be served.
 */
public class HelloStreamModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        // Bind the service info
        bindServiceInfo(ServiceInfo.of("hello-stream"));
        // Bind the HelloService client
        bindClient(HelloService.class);
        // Bind the subscriber eagerly to ensure it starts up
        bind(HelloStreamSubscriber.class).asEagerSingleton();
    }
}
