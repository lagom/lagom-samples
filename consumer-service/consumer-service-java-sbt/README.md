# Consumer Service

This recipe demonstrates how to create a service in Lagom that only consumes from other services and does not provide any service calls of its own.

It is similar to the default "hello" application created by the Lagom Java template, but the `HelloStreamService` interface and implementation have been removed.

Instead, the `hello-stream-impl` project only subscribes to events in Kafka and logs each one to the console.

## Implementation details

The key change is in [`HelloStreamModule`](hello-stream-impl/src/main/java/com/example/hellostream/impl/HelloStreamModule.java).

Ordinarily, the service module binds a service interface and implementation:

```java
bindService(HelloStreamService.class, HelloStreamServiceImpl.class);
```

In a consumer service, this is changed to call `bindServiceInfo` with the name of the service:

```java
bindServiceInfo(ServiceInfo.of("hello-stream"));
```

The service name should be unique, and is used in a few ways:

- To construct the Kafka consumer group ID
- To identify the service when running in development mode
- Production tooling can read the service name to generate deployment configuration

## Testing the recipe

You can test this recipe using 2 separate terminals.

On one terminal start the service:

```
sbt runAll
```

On a separate terminal, use `curl` to trigger some events in `HelloService`:

```
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
curl -H "Content-Type: application/json" -X POST -d '{"message": "Good morning"}' http://localhost:9000/api/hello/Bob
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Carol
curl -H "Content-Type: application/json" -X POST -d '{"message": "Howdy"}' http://localhost:9000/api/hello/David
```

After a few seconds, `HelloStreamSubscriber` will output some log messages indicating that the events have been received:

```
16:30:22.919 [info] com.lightbend.lagom.recipes.consumer.hellostream.impl.HelloStreamSubscriber [] - Received event: [HelloEvent.GreetingMessageChanged(name=Alice, message=Hi)]
16:30:25.915 [info] com.lightbend.lagom.recipes.consumer.hellostream.impl.HelloStreamSubscriber [] - Received event: [HelloEvent.GreetingMessageChanged(name=Bob, message=Good morning)]
16:30:28.876 [info] com.lightbend.lagom.recipes.consumer.hellostream.impl.HelloStreamSubscriber [] - Received event: [HelloEvent.GreetingMessageChanged(name=Carol, message=Hi)]
16:30:31.827 [info] com.lightbend.lagom.recipes.consumer.hellostream.impl.HelloStreamSubscriber [] - Received event: [HelloEvent.GreetingMessageChanged(name=David, message=Howdy)]
```

## More resources

This is described in more detail in the Lagom documentation:

- [Service Metadata](https://www.lagomframework.com/documentation/current/java/ServiceInfo.html)
- [Binding a service client](https://www.lagomframework.com/documentation/current/java/ServiceClients.html#Binding-a-service-client)
