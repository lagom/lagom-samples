# Mixed Persistence Service

This recipe demonstrates how to create a service in Lagom for Scala that uses Cassandra for write-side persistence and JDBC for a read-side view.

## Implementation details

TODO

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
