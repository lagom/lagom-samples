# Akka-gRPC Lagom Quickstart (scala)

This project demonstrates the usage of [akka-grpc](https://github.com/akka/akka-grpc) into Lagom.

## Structure

There are two Lagom services (`hello` and `hello-proxy`) exposing the following HTTP API's:

```
GET /api/hello/:id           # served by hello-service
GET /proxy/rest-hello/:id    # served by hello-proxy-service
```  

While the `hello-service` is always returning hard-coded values the `hello-proxy` will always forward the request downstream to `hello-service`.

So when you invoke:

```
$ curl http://localhost:9000/proxy/rest-hello/Alice
```

```
 curl  -->  service gateway  -->  hello-proxy-service  -->  hello-service
```



