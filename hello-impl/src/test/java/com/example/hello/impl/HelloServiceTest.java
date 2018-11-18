package com.example.hello.impl;

import akka.grpc.GrpcClientSettings;
import akka.grpc.javadsl.AkkaGrpcClient;
import akka.japi.function.Function3;
import akka.japi.function.Procedure;
import akka.stream.Materializer;
import com.example.hello.api.HelloService;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import org.junit.Test;
import scala.concurrent.ExecutionContext;

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
            withGrpcClient(
                server,
                GreeterServiceClient::create
                , serviceClient -> {
                    HelloReply reply = serviceClient.sayHello(HelloRequest.newBuilder().setName("Steve").build()).toCompletableFuture().get(5, SECONDS);
                    assertEquals("Hi Steve (gRPC)", reply.getMessage());
                });
        });
    }

    private <T extends AkkaGrpcClient> void withGrpcClient(
        ServiceTest.TestServer server,
        Function3<GrpcClientSettings, Materializer, ExecutionContext, T> clientFactory,
        Procedure<T> block
    ) {
        int sslPort = server.portSsl().get();

        GrpcClientSettings settings =
            GrpcClientSettings
                .connectToServiceAt("127.0.0.1", sslPort, server.system())
                .withSSLContext(server.clientSslContext().get())
                .withOverrideAuthority("localhost");
        T grpcClient = null;
        try {
            grpcClient = clientFactory.apply(settings, server.materializer(), server.system().dispatcher());
            block.apply(grpcClient);
        } catch (Exception e) {
        } finally {
            if (grpcClient != null) {
                grpcClient.close();
            }

        }
    }
}
