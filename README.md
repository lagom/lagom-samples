# Lagom Samples

[Lagom](https://www.lagomframework.com/) is an [open source](https://github.com/lagom/lagom) framework for building reactive microservice systems in Java or Scala. 

This repository contains code examples to help you understand how to achieve common goals. In general, code in each folder of this repository tries to answer a question of type "How do I _xyz_ ?". For example: "How do I use RDBMS read-sides with Cassandra write-sides?".


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

## Contributing examples

This project follows the [Lagom contributor guidelines](https://github.com/lagom/lagom/blob/master/CONTRIBUTING.md). Please read and follow those when contributing Lagom Samples.

## Complete example index

* How do I use RDBMS read-sides with Cassandra write-sides? ([mixed persistence in java](mixed-persistence/mixed-persistence-java-sbt/README.md) or [mixed persistence in scala](mixed-persistence/mixed-persistence-scala-sbt/README.md))
* How do I integrate Lightbend Telemetry (Cinnamon)? ([Java/Maven example](./lightbend-telemetry/lightbend-telemetry-java-mvn/README.md))
* How do I use Lagom with Couchbase both write-side and read-side? [Java Maven and Scala Sbt](./couchbase-persistence/README.md)) (Couchbase Persistence is **NOT** production ready yet)

#### License

<sup>
To the extent possible under law, the author(s) have dedicated all copyright and
related and neighboring rights to this software to the public domain worldwide.
This software is distributed without any warranty.
</sup>

<br>

<sub>
Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion by you shall be licensed as above, without any additional terms
or conditions.
</sub>
