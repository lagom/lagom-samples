/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.recipes.corsjava.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.recipes.corsjava.api.CorsjavaService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the CorsjavaService.
 */
public class CorsjavaServiceImpl implements CorsjavaService {


  @Inject
  public CorsjavaServiceImpl() {
  }

  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return request -> CompletableFuture.completedFuture(id);
  }


}
