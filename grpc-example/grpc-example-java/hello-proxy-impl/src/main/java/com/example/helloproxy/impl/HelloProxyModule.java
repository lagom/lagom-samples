package com.example.helloproxy.impl;


import com.example.hello.api.HelloService;
import com.example.helloproxy.api.HelloProxyService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class HelloProxyModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindClient(HelloService.class);
        bindService(HelloProxyService.class, HelloProxyServiceImpl.class);
    }

}
