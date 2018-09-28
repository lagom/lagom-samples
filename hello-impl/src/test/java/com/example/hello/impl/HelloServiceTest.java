package com.example.hello.impl;

import akka.grpc.GrpcClientSettings;
import com.example.hello.api.HelloService;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

    @Test
    public void shouldSayHelloUsingALagomClient() throws Exception {
        withServer(defaultSetup(), server -> {
            HelloService service = server.client(HelloService.class);

            String msg = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals("Hi Alice!", msg);
        });
    }

    @Test
    public void shouldSayHelloUsingGrpc() throws Exception {
        withServer(defaultSetup().withSsl(), server -> {
            int sslPort = server.portSsl().get();

            GrpcClientSettings settings =
                GrpcClientSettings
                    .connectToServiceAt("127.0.0.1", sslPort, server.system())
                    .withSSLContext(server.sslContext().get())
                    .withOverrideAuthority("localhost");
            GreeterServiceClient serviceClient = null;
            try {

                serviceClient = GreeterServiceClient.create(settings, server.materializer(), server.system().dispatcher());

                HelloReply reply = serviceClient.sayHello(HelloRequest.newBuilder().setName("Steve").build()).toCompletableFuture().get(5, SECONDS);
                assertEquals("Hi Steve (gRPC)", reply.getMessage());
            } finally {
                if (serviceClient != null) {
                    serviceClient.close();
                }
            }
        });
    }

}
