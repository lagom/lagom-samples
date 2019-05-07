package com.example.hello.impl;

import akka.NotUsed;
import com.example.hello.api.HelloService;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class HelloServiceImpl implements HelloService {

    @Inject
    public HelloServiceImpl(){

    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return req -> CompletableFuture.completedFuture("Hi " + id + "!");
    }
}
