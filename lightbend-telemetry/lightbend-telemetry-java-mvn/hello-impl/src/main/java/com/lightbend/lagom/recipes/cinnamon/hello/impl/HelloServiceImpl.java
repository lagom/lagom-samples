package com.lightbend.lagom.recipes.cinnamon.hello.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.recipes.cinnamon.hello.api.HelloService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class HelloServiceImpl implements HelloService {

    private final HelloService helloService;

    @Inject
    public HelloServiceImpl(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> {
            return completedFuture("Hello " + id);
        };
    }

    @Override
    public ServiceCall<NotUsed, String> helloProxy(String id) {
        return msg -> {
            CompletionStage<String> response = helloService.hello(id).invoke(NotUsed.getInstance());
            return response.thenApply(answer -> "Hello service said: " + answer);
        };
    }

}
