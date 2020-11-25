# Lagom gRPC Example (Java)

[Lagom](https://www.lagomframework.com/) is an open source framework (built on [Akka](https://akka.io/) and [Play](https://www.playframework.com/)) for developing reactive microservice systems in Java or Scala.
[Akka gRPC](https://developer.lightbend.com/docs/akka-grpc/current/overview.html) and [Play gRPC](https://developer.lightbend.com/docs/play-grpc/current/) are toolkits for building streaming gRPC servers and clients on top of Akka Streams and Play.

This Guide will show you how to use Akka & Play gRPC as an alternate RPC library to communicate two microservices developed using Lagom.

## Downloading the example

The Lagom gRPC Example is in the [Lagom Samples GitHub repository](https://github.com/lagom/lagom-samples) that you can clone locally:

```bash
git clone https://github.com/lagom/lagom-samples.git
cd grpc-example/grpc-example-java
```

## Running the example

You can run it like any Lagom application.

In Maven,

```bash
mvn lagom:runAll
```

In sbt,

```bash
sbt runAll
```

The `runAll` command starts Lagom in development mode. Once all the services are started you will see Lagom's start message:

```bash
...
[INFO] Service hello-impl listening for HTTP on 127.0.0.1:11000
[INFO] Service hello-proxy-impl listening for HTTP on 127.0.0.1:54328
[info] (Services started, press enter to stop and go back to the console...)
```

As soon as you see the message `[info] (Services started, press enter to stop and go back to the console...)` you
can proceed. On a separate terminal, try the application:

```bash
$ curl http://localhost:9000/proxy/rest-hello/Alice
Hi Alice!
$ curl http://localhost:9000/proxy/grpc-hello/Steve
Hi Steve! (gRPC)
```

## Application Structure

This application is built with two Lagom services (`hello` and `hello-proxy`) exposing the following endpoints:

```bash
GET /proxy/rest-hello/:id    # served by hello-proxy-service (HTTP-JSON)
GET /proxy/grpc-hello/:id    # served by hello-proxy-service (HTTP-JSON)
GET /api/hello/:id           # served by hello-service (HTTP-JSON)
```

And also:

```bash
/helloworld.GreetingsService/sayHello   # served by hello-service (gRPC)
```

We want to show how to use gRPC for service communication, so in this guide the services are
as simple as possible, with no other features. While the `hello-service` always returns hard-coded
values the `hello-proxy` always forwards the request downstream to `hello-service`.

![Application Structure](./application-structure.png)

So when you invoke:

```bash
curl http://localhost:9000/proxy/rest-hello/Alice
```

The following happens:

```bash
curl  --(http)-->  service gateway  --(http)-->  hello-proxy-service  --(http)-->  hello-service
```

Alternatively:

```bash
curl http://localhost:9000/proxy/grpc-hello/Alice
```

The following happens

```bash
curl  --(http)-->  service gateway  --(http)-->  hello-proxy-service  --(gRPC/https)-->  hello-service
```

## Testing the gRPC endpoints

The gRPC endpoints are not accessible via the Lagom Service Gateway so it's only possible to consume them from
another Lagom service or pointing a client directly to the `HTTP/2` port of the Lagom Service. Earlier we
saw that Lagom informs of the following bindings:

```bash
...
[INFO] Service hello-impl listening for HTTP on 127.0.0.1:11000
[INFO] Service hello-proxy-impl listening for HTTP on 127.0.0.1:54328
[info] (Services started, press enter to stop and go back to the console...)
```

You can test the gRPC endpoint using [gRPCurl](https://github.com/fullstorydev/grpcurl).
Note that for simplicity, this sample is disabling TLS, therefore it's possbile to call the `HTTP/2` endpoint without using https.

```bash
$   grpcurl --proto hello-impl/src/main/protobuf/helloworld.proto \
          -d '{"name": "Katherine" }' \
          -plaintext 127.0.0.1:11000   \
          helloworld.GreeterService.SayHello
 {
   "message": "Hi Katherine! (gRPC)"
 }
```

The command above:

1. Uses the gRPC description on `hello-impl/src/main/protobuf/helloworld.proto`
1. Connects to the `hello-impl` service at `127.0.0.1:11000` using plaintext over `http`.
1. Sends a gRPC call `helloworld.GreeterService.SayHello` with `{"name": "Katherine" }` payload.

## References

- [Akka gRPC](https://developer.lightbend.com/docs/akka-grpc/current/)
- [Play gRPC](https://developer.lightbend.com/docs/play-grpc/current/)
