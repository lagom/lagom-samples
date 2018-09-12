# Akka-gRPC Lagom Quickstart (scala)

This project demonstrates the usage of [akka-grpc](https://github.com/akka/akka-grpc) into Lagom.

## Structure

There are two Lagom services (`hello` and `hello-proxy`) exposing the following HTTP API's:

```
GET /api/hello/:id           # served by hello-service
GET /proxy/rest-hello/:id    # served by hello-proxy-service
GET /proxy/grpc-hello/:id    # served by hello-proxy-service
```  

While the `hello-service` is always returning hard-coded values the `hello-proxy` will always forward the request downstream to `hello-service`.

So when you invoke:

```
$ curl http://localhost:9000/proxy/rest-hello/Alice
```

The following happens

```
 curl  -(http)->  service gateway  -(http)->  hello-proxy-service  -(http)->  hello-service
```

Alternatively:

```
$ curl http://localhost:9000/proxy/grpc-hello/Alice
```

The following happens

```
 curl  -(http)->  service gateway  -(http)->  hello-proxy-service  -(gRPC/https)->  hello-service
```



### Testing gRPC

You can test the gRPC endpoint on `hello-impl` using [grpcc](https://github.com/njpatel/grpcc)

```bash
$ grpcc --proto hello-impl/src/main/protobuf/helloworld.proto --insecure --address 127.0.0.1:11000 --eval 'client.sayHello({name:"Alice"}, printReply)'
```