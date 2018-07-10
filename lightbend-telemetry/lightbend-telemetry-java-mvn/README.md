# Integrating Lagom with Lightbend Telemetry (Cinnamon)

[Telemetry](https://developer.lightbend.com/docs/cinnamon/current/home.html) (also known as "Cinnamon"), part of Lightbend Enterprise Suite’s Intelligent Monitoring feature set, is a suite of insight tools that provides a view into the workings of our distributed platforms. This view allows developers and operations to respond quickly to problems, track down unexpected behavior and even tune your system. As a result, you can deploy your applications to production with confidence.

Lightbend Telemetry [integrates with Lagom](https://developer.lightbend.com/docs/cinnamon/current/instrumentations/lagom/lagom.html) to provide circuit breaker metrics, HTTP client and server metrics, and request tracing.

This example adapts the [instructions for integrating Telemetry into a Lagom Java service with sbt](https://developer.lightbend.com/docs/cinnamon/current/getting-started/lagom_java.html), showing how to do the same using Maven.

## Prerequisites

The following must be installed for these instructions to work:

* Java 8
* Maven 3.3 or higher
* Bintray credentials

### Bintray credentials

Follow [these instructions](https://www.lightbend.com/product/lightbend-reactive-platform/credentials) to set up your Bintray credentials for Maven.

## Testing the recipe

Lagom has a special development mode for rapid development, and does not fork the JVM when using the `lagom:runAll` or `lagom:run` commands in Maven. A forked JVM is necessary to gain metrics for actors and HTTP calls, since those are provided by the Cinnamon Java Agent. This example uses the [`exec-maven-plugin`](https://www.mojohaus.org/exec-maven-plugin/) to run the Lagom production server, enabling the complete set of metrics to be displayed.

You can test this recipe using 2 separate terminals.

On one terminal build and execute the service:

```
mvn install                  # builds the service and downloads the Cinnamon agent
mvn -pl hello-impl exec:exec # runs the hello service
```

The output should look something like this:

```
[INFO] [06/22/2018 16:21:58.464] [CoreAgent] Cinnamon Agent version 2.8.7
2018-06-22T06:52:01.687Z [info] akka.event.slf4j.Slf4jLogger [] - Slf4jLogger started
2018-06-22T06:52:02.143Z [info] cinnamon.chmetrics.CodaHaleBackend [sourceThread=main, akkaSource=CodaHaleBackend, sourceActorSystem=application, akkaTimestamp=06:52:02.140UTC] - Reporter com.lightbend.cinnamon.chmetrics.reporter.provided.ConsoleReporter started.
2018-06-22T06:52:03.212Z [info] play.api.Play [] - Application started (Prod)
2018-06-22T06:52:03.970Z [info] play.core.server.AkkaHttpServer [] - Listening for HTTP on /0:0:0:0:0:0:0:0:9000
6/22/18 4:22:07 PM =============================================================

-- Gauges ----------------------------------------------------------------------
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.active-threads
             value = 0
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.parallelism
             value = 8
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.pool-size
             value = 3
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.queued-tasks
             value = 0
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.running-threads
             value = 0
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.active-threads
             value = 1
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.pool-size
             value = 1
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.running-threads
             value = 0
```

To try out the `hello-proxy` service call and see metrics for the HTTP endpoints, as well as the `hello` circuit breaker you can either point your browser to `http://localhost:9000/api/hello-proxy/World` or simply run `curl` from the command line like this `curl http://localhost:9000/api/hello-proxy/World`

The output from the server should now also contain metrics like this:

```
-- Gauges ----------------------------------------------------------------------
...
metrics.lagom.circuit-breakers.hello.state
             value = 3
...
-- Histograms ------------------------------------------------------------------
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._api_hello-proxy__id.endpoint-response-time
             count = 1
               min = 481423809
               max = 481423809
              mean = 481423809.00
            stddev = 0.00
            median = 481423809.00
              75% <= 481423809.00
              95% <= 481423809.00
              98% <= 481423809.00
              99% <= 481423809.00
            99.9% <= 481423809.00
...
metrics.lagom.circuit-breakers.hello.latency
             count = 1
               min = 235385490
               max = 235385490
              mean = 235385490.00
            stddev = 0.00
            median = 235385490.00
              75% <= 235385490.00
              95% <= 235385490.00
              98% <= 235385490.00
              99% <= 235385490.00
            99.9% <= 235385490.00
...
```
