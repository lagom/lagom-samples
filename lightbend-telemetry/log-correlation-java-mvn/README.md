# This is an example how to use Lightbend Telemetry (Cinnamon) for Log Correlation

### How it works

Cinnamon starting 2.10.3 supports [log correlation](https://downloads.lightbend.com/cinnamon/docs/2.10.3/extensions/mdc.html#log-correlation).


In order to enable it use next configuration:

```
cinnamon.slf4j.mdc {
  log-correlation += automatic-correlation-id

  automatic-correlation-id {
    name = "X_CORRELATION_ID"
  }
}
``` 

How to run:

```
mvn install                  # builds the service and downloads the Cinnamon agent
mvn -pl hello-impl exec:exec # runs the hello service
```

Then, on a separate terminal you can generate traffic using the following `curl` command a few times:


```bash
curl http://localhost:9000/api/hello/World
```

Which produces an output similar to:

```
2018-09-14T08:42:07.437Z [info] com.li... [X_CORRELATION_ID=61c40d60-6323-489e-9a51-c4e8a029961c] - [hello-service] Handling request: World.
2018-09-14T08:42:09.561Z [info] com.li... [X_CORRELATION_ID=f913cce3-f61d-449b-ba81-810e3da87ba8] - [hello-service] Handling request: World.
2018-09-14T08:42:11.608Z [info] com.li... [X_CORRELATION_ID=fc2a0fa5-4fd5-4c59-a399-4f1a27f7cfe1] - [hello-service] Handling request: World.
```

You can see how the `hello-service` includes a correlation id on the logs it produces.

### Testing across boundaries

Next we can generate traffic to the `hello-proxy` endpoint. That receives requests and forwards the traffic to a downstream service  

```bash
curl http://localhost:9000/api/hello-proxy/World
```

Back on the terminal where the Lagom process is running you will see:
```
2018-09-14T08:37:24.324Z [info] com.li... [X_CORRELATION_ID=3bc61118-b754-4220-bba6-f5e8ba2d79f1] - [PROXY] Forwarding request: World
2018-09-14T08:37:24.570Z [info] com.li... [X_CORRELATION_ID=3bc61118-b754-4220-bba6-f5e8ba2d79f1] - [hello-service] Handling request: World.
2018-09-14T08:37:26.717Z [info] com.li... [X_CORRELATION_ID=027241f5-6b18-410d-919e-e17ba13b9657] - [PROXY] Forwarding request: World
2018-09-14T08:37:26.728Z [info] com.li... [X_CORRELATION_ID=027241f5-6b18-410d-919e-e17ba13b9657] - [hello-service] Handling request: World.
```

Each pair of traces above corresponds to one of your `curl` requests. The requests arrives on the `proxy` which 
traces `[PROXY]` and then (using HTTP) creates a downstream call to the actual `hello-service`. Note how the 
correlation id propagates across boundaries.



