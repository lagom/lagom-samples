package com.example.helloproxy.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.Materializer;
import com.example.hello.api.HelloService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import org.junit.Test;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloProxyServiceImplTest {

    @Test
    public void helloProxyShouldPassthroughHttpRequests()  {
        withServer(
            defaultSetup().configureBuilder(builder ->
                builder
                    .disable(example.myapp.helloworld.grpc.AkkaGrpcClientModule.class)
                    .overrides(bind(HelloService.class).to(StubHelloService.class))
                    .overrides(bind(GreeterServiceClient.class).toProvider(GreeterServiceClientStubProvider.class))
            ),
            server -> {
                HelloService proxyServiceClient = server.client(HelloService.class);
                String msg = proxyServiceClient.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
                assertEquals("Hello Alice", msg);
            });
    }

    public static class StubHelloService implements HelloService {
        @Override
        public ServiceCall<NotUsed, String> hello(String id) {
            return notUsed -> CompletableFuture.completedFuture("Hello " + id);
        }
    }

    public static class GreeterServiceClientStubProvider implements Provider<GreeterServiceClient> {

        @Inject
        private Materializer mat;
        @Inject
        private ExecutionContext ec;
        @Inject
        private ActorSystem actorSys;

        @Override
        public GreeterServiceClient get() {
            GrpcClientSettings settings =
                GrpcClientSettings.connectToServiceAt("localhost", 2525, actorSys);
            return GreeterServiceClient.create(settings, mat, ec);
        }
    }
}
