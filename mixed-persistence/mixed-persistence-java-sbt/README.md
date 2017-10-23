# Mixed Persistence Service

This recipe demonstrates how to create a service in Lagom for Java that uses Cassandra for write-side persistence and JPA for a read-side view.

## Implementation details

The key changes are in [`application.conf`](hello-impl/src/main/resources/application.conf) and [`HelloModule`](hello-impl/src/main/java/com/lightbend/lagom/recipes/mixedpersistence/hello/impl/HelloModule.java).

Specifcally, `JdbcPersistenceModule` is explicitly disabled:

```
play.modules.disabled += com.lightbend.lagom.javadsl.persistence.jdbc.JdbcPersistenceModule
```

Then, an explicit binding for the JDBC offset store is added back:

```java
bind(SlickOffsetStore.class).to(JavadslJdbcOffsetStore.class);
```

## Testing the recipe

You can test this recipe using 2 separate terminals.

On one terminal start the service:

```
sbt runAll
```

On a separate terminal, use `curl` to trigger some events in `HelloService`:

```
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
curl -H "Content-Type: application/json" -X POST -d '{"message": "Good day"}' http://localhost:9000/api/hello/Bob
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Carol
curl -H "Content-Type: application/json" -X POST -d '{"message": "Howdy"}' http://localhost:9000/api/hello/David
```

After a few seconds, use `curl` to retrieve a list of all of the stored greetings:

```
curl http://localhost:9000/api/greetings
[{"id":"Alice","message":"Hi"},{"id":"Bob","message":"Good day"},{"id":"Carol","message":"Hi"},{"id":"David","message":"Howdy"}]
```

This is eventually consistent, so it might take a few tries before you see all of the results.
