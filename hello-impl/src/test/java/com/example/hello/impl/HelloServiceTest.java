package com.example.hello.impl;

import com.example.hello.api.HelloService;
import com.lightbend.lagom.javadsl.testkit.grpc.AkkaGrpcClientHelpers;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

    @Test
    public void shouldSayHelloUsingALagomClient() throws Exception {
        withServer(defaultSetup(), server -> {
            HelloService service = server.client(HelloService.class);

            String msg = service.hello("Alice").invoke()
                .toCompletableFuture().get(5, SECONDS);
            assertEquals("Hi Alice!", msg);
        });
    }

    @Test
    public void shouldSayHelloUsingGrpc() throws Exception {
        withServer(defaultSetup().withSsl(), server -> {
            AkkaGrpcClientHelpers
                .withGrpcClient(
                    server,
                    GreeterServiceClient::create,
                    serviceClient -> {
                        HelloRequest request =
                            HelloRequest.newBuilder().setName("Steve").build();
                        HelloReply reply = serviceClient
                            .sayHello(request)
                            .toCompletableFuture()
                            .get(5, SECONDS);
                        assertEquals("Hi Steve (gRPC)", reply.getMessage());
                    });
        });
    }
}
