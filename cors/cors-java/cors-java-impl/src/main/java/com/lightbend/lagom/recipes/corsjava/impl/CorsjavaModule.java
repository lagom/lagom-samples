/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.recipes.corsjava.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.recipes.corsjava.api.CorsjavaService;

/**
 * The module that binds the CorsjavaService so that it can be served.
 */
public class CorsjavaModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(CorsjavaService.class, CorsjavaServiceImpl.class);
  }
}
