# Lagom Samples

[![Build Status](https://travis-ci.com/lagom/lagom-samples.svg?branch=1.6.x)](https://travis-ci.com/lagom/lagom-samples)

[Lagom](https://www.lagomframework.com/) is an [open source](https://github.com/lagom/lagom) framework for building reactive microservice systems in Java or Scala. 

This repository contains code examples to help you understand how to achieve common goals. In general, code in each folder of this repository tries to answer a question of type "How do I _xyz_ ?". For example: "How do I use RDBMS read-sides with Cassandra write-sides?".

## Samples index

* Shopping Cart: a reference application demoing core Lagom features. It is available in [Java](shopping-cart/shopping-cart-java/README.md) and  [Scala](shopping-cart/shopping-cart-scala/README.md).

* Using gRPC in Lagom ([Java](grpc-example/grpc-example-java/README.md), [Scala](grpc-example/grpc-example-scala/README.md))

* How do I use RDBMS read-sides with Cassandra write-sides? ([mixed persistence in java](mixed-persistence/mixed-persistence-java-sbt/README.md) or [mixed persistence in scala](mixed-persistence/mixed-persistence-scala-sbt/README.md))
* How do I integrate Lightbend Telemetry (Cinnamon)? ([Java/Maven example](lightbend-telemetry/lightbend-telemetry-java-mvn/README.md))
* How do I configure Log correlation (Cinnamon)? ([Java/Maven example](lightbend-telemetry/log-correlation-java-mvn/README.md))
* How do I use Lagom with Couchbase both write-side and read-side? [Java Maven and Scala Sbt](couchbase-persistence/README.md)) (Couchbase Persistence is **NOT** production ready yet)

## Community-driven examples

* How do I authenticate/authorize by JWT? ([Java/Maven example](https://github.com/pac4j/lagom-pac4j-java-demo), [Scala/Sbt example](https://github.com/pac4j/lagom-pac4j-scala-demo))
* How do I generate _OpenAPI/Swagger Specification_ for Lagom service? ([Java/Maven example](https://github.com/taymyr/lagom-samples/blob/master/openapi/java/README.md), [Scala/Sbt example](https://github.com/taymyr/lagom-samples/blob/master/openapi/scala/README.md))
* How do I using Play's JPA API to do CRUD-oriented persistence in a Lagom service? ([Java/SBT example](https://github.com/taymyr/lagom-samples/blob/master/jpa-crud/java-sbt/README.md))
* How do I use MongoDB as read-side in a Lagom service? ([Scala/SBT example](https://github.com/abknanda/mongo-readside-lagom))
* How do I use a Lagom service in JavaScript? ([Scala/SBT example](https://github.com/mliarakos/lagom-scalajs-example))

## Contributing examples

This project follows the [Lagom contributor guidelines](https://github.com/lagom/lagom/blob/master/CONTRIBUTING.md). Please read and follow those when contributing Lagom Samples.

## Pre-requisites

To download and run these examples you will need:

1. a [git](https://git-scm.com/downloads) client
2. [sbt](http://www.scala-sbt.org/download.html) or [Maven](https://maven.apache.org/install.html) depending on the example.
3. a [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
4. a code editor (such as [IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/downloads/), [Atom](https://atom.io/),... )

## Using these examples

If you want to run a specific example you will have to clone the whole repository and then navigate to a specific application folder. For example:

* `git clone https://github.com/lagom/lagom-samples.git`
* `cd mixed-persistence/mixed-persistence-java-sbt`

Each example includes specific instructions in it's `README.md` file on how to run and exercise the application.

## License

To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion by you shall be licensed as above, without any additional terms or conditions.
