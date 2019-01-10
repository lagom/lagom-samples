# Couchbase Lagom Samples

These are simple examples demonstrating how to use Couchbase with Lagom both in Java with Maven and in Scala with Sbt.
These sample apps use PersistentEntity with Couchbase as a backend. Also they implement simple read-side processors that 
consume events and update a couchbase document that can be queried.

> Note: Couchbase specific parts are marked with `#couchbase-begin` and `#couchbase-end` in multiple files.

These recipes assume you are already familiar with Lagom and particularly with the [Lagom Hello World example app](https://www.lagomframework.com/documentation/1.4.x/java/UnderstandHello.html) in which these recipes are based on.


## How to run

In order to run these samples you'll need a Couchbase service. It also requires to pre-create necessary indexes.
Docker image can be used to run Couchbase locally without installation.

The Docker Compose preconfigured settings are available at 
[Akka Persistence Couchbase GitHub](https://github.com/akka/akka-persistence-couchbase/tree/master/docker).

Please see [Akka Persistence Couchbase Documentation](https://doc.akka.io/docs/akka-persistence-couchbase/current) for
further details on how to run Couchbase with Docker Compose and more.

These samples use the next settings (can be adjusted in `application.conf`):

```
hello.couchbase.bucket = "akka"

hello.couchbase.connection {
  nodes = ["localhost"]
  username = "admin"
  password = "admin1"
}
```

If you are using `couchbase-persistence-java-mvn` invoke `mvn lagom:runAll` on a terminal to start up the service.

If you are using `couchbase-persistence-scala-sbt` then invoke `sbt lagom:runAll` instead.

## Testing the service (manually)

Once running, you can manually test the service:

1) Get user's greeting from the write-side

```
$ curl http://localhost:9000/api/hello/Alice

Hello, Alice!
```

2) Change Alice's greeting

```
$ curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice

{ "done" : true }
```

3) Get all users with their current greetings from the read-side

```
$ curl http://localhost:9000/api/user-greetings

[{"user":"Alice","message":"Hi"}]
```

You can also have a look at the automated tests in the folder `hello-impl/src/{java,scala}/tests` and invoke them with the command `mvn clean test` or `sbt test` like you would do with any regular project.
