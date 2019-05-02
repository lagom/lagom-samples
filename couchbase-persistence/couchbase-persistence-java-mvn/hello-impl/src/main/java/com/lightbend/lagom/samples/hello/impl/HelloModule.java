package com.lightbend.lagom.sampleshello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.sampleshello.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {

  @Override
  protected void configure() {
    bindService(HelloService.class, HelloServiceImpl.class);
  }
}
