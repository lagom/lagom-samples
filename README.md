# Lagom gRPC Example (Java)

This project demonstrates the usage of [akka-grpc](https://github.com/akka/akka-grpc) into Lagom.

## Running

Using gRPC in Lagom requires adding a Java Agent to the runtime. In order to handle this setting you can start the `sbt` console
using the `ssl-lagom` script provided that takes care of downloading and setting the agent:

```bash
./ssl-lagom
```

The first time you run that command it'll have to download dependencies so it may take longer. Once ready you'll be 
at the `sbt` console. Use the `runAll` command to start the application: 

```
sbt:lagom-java-grpc-example> runAll
```

Once started you should see Lagom's start message:

```
...
[info] Service hello-proxy-impl listening for HTTP on 127.0.0.1:54328
[info] Service hello-proxy-impl listening for HTTPS on 127.0.0.1:65108
[info] Service hello-impl listening for HTTP on 127.0.0.1:65499
[info] Service hello-impl listening for HTTPS on 127.0.0.1:11000
[info] (Services started, press enter to stop and go back to the console...)
```  

On a separate terminal, try the application:

```bash
$ curl http://localhost:9000/proxy/rest-hello/Alice
Hi Alice!
$ curl http://localhost:9000/proxy/grpc-hello/Steve
Hi Steve! (gRPC)
```

(more details on what just happened in following sections)

## Testing the gRPC endpoints 

You can also test the gRPC endpoint directly using [grpcc](https://github.com/njpatel/grpcc). First you will have to prepare the SSL certificates:

```bash
keytool -export -alias playgeneratedCAtrusted -keystore target/dev-mode/generated.keystore  -storepass "" -file trustedCA.crt
openssl x509 -in  trustedCA.crt -out trustedCA.pem -inform DER -outform PEM
``` 

The code above extracts the CA Lagom uses internally when using Lagom's Dev Mode (`sbt runAll`) like we did in previous 
steps. Once the CA certificate is extracted we can use `grpcc` to test the application:

```bash
$   grpcc --proto hello-impl/src/main/protobuf/helloworld.proto \
          --address localhost:11000 \
          --eval 'client.sayHello({name:"Katherine"}, printReply)' \
          --root_cert ./trustedCA.pem
 {
   "message": "Hi Katherine! (gRPC)"
 }
```

## Structure

There are two Lagom services (`hello` and `hello-proxy`) exposing the following HTTP API's:

```
GET /api/hello/:id           # served by hello-service
GET /proxy/rest-hello/:id    # served by hello-proxy-service
GET /proxy/grpc-hello/:id    # served by hello-proxy-service
```  

While the `hello-service` is always returning hard-coded values the `hello-proxy` will always forward the request downstream to `hello-service`.

So when you invoke:

```bash
curl http://localhost:9000/proxy/rest-hello/Alice
```

The following happens

```bash
curl  -(http)->  service gateway  -(http)->  hello-proxy-service  -(http)->  hello-service
```

Alternatively:

```bash
curl http://localhost:9000/proxy/grpc-hello/Alice
```

The following happens

```bash
curl  -(http)->  service gateway  -(http)->  hello-proxy-service  -(gRPC/https)->  hello-service
```

### Testing gRPC

You can test the gRPC endpoint on `hello-impl` using [grpcc](https://github.com/njpatel/grpcc)

```bash
grpcc --proto hello-impl/src/main/protobuf/helloworld.proto --insecure --address 127.0.0.1:11000 --eval 'client.sayHello({name:"Alice"}, printReply)'
```
