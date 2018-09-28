package com.example.helloproxy.impl;

import akka.NotUsed;
import com.example.hello.api.HelloService;
import com.example.helloproxy.api.HelloProxyService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HelloProxyServiceImpl implements HelloProxyService {

    private HelloService helloService;
    private GreeterServiceClient greeterClient;

    @Inject
    public HelloProxyServiceImpl(HelloService helloService, GreeterServiceClient greeterClient) {
        this.helloService = helloService;
        this.greeterClient = greeterClient;
    }

    @Override
    public ServiceCall<NotUsed, String> proxyViaHttp(String id) {
        return req -> helloService.hello(id).invoke();
    }

    @Override
    public ServiceCall<NotUsed, String> proxyViaGrpc(String id) {
        return req -> greeterClient
            .sayHello(
                HelloRequest
                    .newBuilder()
                    .setName(id)
                    .build()
            ).thenApply(
                HelloReply::getMessage
            );
    }

}
