package org.example.hello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.example.hello.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {

  @Override
  protected void configure() {
    bindService(HelloService.class, HelloServiceImpl.class);
  }
}
