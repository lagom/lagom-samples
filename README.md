<!--- Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com> -->
# Lagom Recipes

[Lagom](https://www.lagomframework.com/) is an [open source](https://github.com/lagom/lagom) framework for building reactive microservice systems in Java or Scala. This repository contains code examples to help you understand how to achieve common goals. In general, code in each folder of this repository tries to answer a question of type "How do I _xyz_ ?". For example: "How do I use RDBMS read-sides with Cassandra write-sides?".

Each example is usually built in two steps (git commits) where the first commit introduces an example project and the second commit introduces the minimum changes required to complete the example. For example: in commit [9f8de2f](https://github.com/lagom/lagom-recipes/commit/9f8de2f34f0978aeeb0f50cb261345e24da44caf) a new project is added which is edited in commit [b759a18](https://github.com/lagom/lagom-recipes/commit/b759a1821b235603cf65bd1556b57050b76ca69c). You can browse the repo history to find the changes demonstrating how to solve each specific problem.

## Pre-requisites

To download and run these examples you will need:

1. a [git](https://git-scm.com/downloads) client
2. [sbt](http://www.scala-sbt.org/download.html) or [Maven](https://maven.apache.org/install.html) depending on the example.
3. a [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
4. a code editor (such as [IntelliJ](https://www.jetbrains.com/idea/), [Eclipse](https://www.eclipse.org/downloads/), [Atom](https://atom.io/),... )

## Using these examples

If you want to run a specific example you will have to clone the whole repository and then navigate to a specific application folder. For example:

* `git clone https://github.com/lagom/lagom-recipes.git`
* `cd consumer-service/consumer-service-java-sbt`

Each example includes specific instructions in it's `README.md` file on how to run and exercise the application.

## Contributing examples

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## Complete example index

* How do I enable CORS? (using Lagom's [javadsl](./cors/cors-java/README.md) or [scaladsl](./cors/cors-scala/README.md))
* How do I create a [Subscriber only service](https://www.lagomframework.com/documentation/1.3.x/java/KafkaClient.html#Subscriber-only-Services)? (also referred to as [consumer service](./consumer-service/consumer-service-java-sbt/README.md))
* How do I use RDBMS read-sides with Cassandra write-sides? ([mixed persistence in java](mixed-persistence/mixed-persistence-java-sbt/README.md) or [mixed persistence in scala](mixed-persistence/mixed-persistence-scala-sbt/README.md))
* How to create a stateless service in Lagom for Java that uses [Play's Internationalization Support](i18n/hello-i18n-java-mvn/README.md).
* How do I manipulate Headers and Status Codes and test those cases?([HTTP header handling](./http-header-handling/http-header-handling-java-sbt/README.md))
* How do I handle `multipart/form-data` file uploads? ([Scala example](./file-upload/file-upload-scala-sbt/README.md))
* How do I use a custom message serializer and response header to implement file downloads? ([Scala example](./file-download/file-download-scala-sbt/README.md))
* How do I integrate Lightbend Telemetry (Cinnamon)? ([Java/Maven example](./lightbend-telemetry/lightbend-telemetry-java-mvn/README.md))
* How do I deploy a Lagom Maven application in Kubernetes? ([Java/Maven example](./kubernetes-deployment/hello-kubernetes-java-mvn/README.md))
