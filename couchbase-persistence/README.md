Couchbase Lagom Samples
=======================

These are simple examples demonstrating how to use Couchbase with Lagom both in Java with Maven and in Scala with Sbt.
These sample apps use PersistentEntity with Couchbase as a backend. Also they implement simple read-side processors that 
consume events and update a couchbase document that can be queried.

> Note: Couchbase specific parts are marked with `#couchbase-begin` and `#couchbase-end` in multiple files.


What it can do
--------------

1) Get user's greeting from the write-side

```
$ curl http://localhost:9000/api/hello/Alice

Hello, Alice!✔ 
```

2) Change Alice's greeting

```
$ curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice

{ "done" : true }✔ 
```

3) Get all users with their current greetings from the read-side

```
$ curl http://localhost:9000/api/user-greetings

[{"user":"Alice","message":"Hi"}]✔ 
```

How to run
----------

In order to run these samples you'll need a Couchbase service. It also requires to pre-create necessary indexes.
Docker image can be used to run Couchbase locally without installation.

The Docker Compose preconfigured settings are available at 
[Akka Persistence Couchbase GitHub](https://github.com/akka/akka-persistence-couchbase/tree/master/docker).

Please see [Akka Persistence Couchbase Documentation](https://doc.akka.io/docs/akka-persistence-coucbase/current) for
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

hello-service-java
------------------

`mvn lagom:runAll`

hello-service-scala
-------------------

`sbt lagom:runAll`
