package com.lightbend.lagom.samples.hello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.samples.hello.impl.HelloServiceImpl;
import com.lightbend.lagom.samples.hello.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(HelloService.class, HelloServiceImpl.class);
    }
}
