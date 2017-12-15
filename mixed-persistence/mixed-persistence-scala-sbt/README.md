# Mixed Persistence Service

This recipe demonstrates how to create a service in Lagom for Scala that uses Cassandra for write-side persistence and JDBC for a read-side view.

## Implementation details

This recipe introduces a key change to your Service Loader compared to a [regular JDBC persistence](https://www.lagomframework.com/documentation/1.3.x/scala/PersistentEntityRDBMS.html#Application-Loader). Instead of mixing in `JdbcPersistenceComponents` we mix in `ReadSideJdbcPersistenceComponents` and then we mix in `WriteSideCassandraPersistenceComponents`. Due to a [bug](https://github.com/lagom/lagom/issues/1099) in Lagom the ordering in which you mix in those two traits is relevant and only if you mix in `ReadSideJdbcPersistenceComponents` before `WriteSideCassandraPersistenceComponents` you will be able to use this combination.

This recipe uses an in-mem H2 database to build a JDBC read-side that keeps the latest greeting each person set up. This requires using H2's ["MERGE"](http://www.h2database.com/html/grammar.html#merge) feature which is equivalent to "insert or update" in other RDBMSs.

Other than those two details this recipe simply builds a read side.     

## Testing the recipe

You can test this recipe using three separate terminals.

In the first terminal start the service:

```
sbt runAll
```

In the second terminal, use `watch curl` to query the read-side:

```
watch curl http://localhost:9000/api/greetings
```

Finally, in a third terminal, use `curl` to cause changes on the persistent entities. These changes will propagate cause events the read-side will materialize.

```
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}'                http://localhost:9000/api/hello/Alice
curl -H "Content-Type: application/json" -X POST -d '{"message": "Good day"}'          http://localhost:9000/api/hello/Bob
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}'                http://localhost:9000/api/hello/Carol
sleep 5
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi again"}'          http://localhost:9000/api/hello/Alice
sleep 2
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi yet once more"}'  http://localhost:9000/api/hello/Alice
sleep 2
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi there"}'          http://localhost:9000/api/hello/Alice
```

A few seconds after running these requests you'll see the list of greetings on your second terminal (where a permanent request to list all greetings is running). The read side build in this recipe keeps only the last greeting for each name so as you keep changing the greeting to `Alice` the change propagates eventually into the list of greetings.
