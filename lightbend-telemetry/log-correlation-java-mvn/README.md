# This is an example how to use Lightbend Telemetry (Cinnamon) for Log Correlation

How it work:

Cinnamon starting 2.10.2 support auto log correlation generation.

In order to enable it use next configuration:

```
cinnamon.slf4j.mdc {
  correlation-id {
    automatic = on
    name = "X_CORRELATION_ID"
  }
}
``` 

How to run:

```
mvn install                  # builds the service and downloads the Cinnamon agent
mvn -pl hello-impl exec:exec # runs the hello service
```

Test:

```
curl http://localhost:9000/api/hello-proxy/World

2018-08-23T19:17:09.843Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=35cef929-e84c-4b32-8ab7-b9869831aa5a] - helloProxy: World.
2018-08-23T19:17:09.982Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=35cef929-e84c-4b32-8ab7-b9869831aa5a] - hello: World.
2018-08-23T19:17:14.568Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=fd12c9d3-5912-45e8-a7d7-f8eb4ecd38ba] - helloProxy: World.
2018-08-23T19:17:14.574Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=fd12c9d3-5912-45e8-a7d7-f8eb4ecd38ba] - hello: World.
2018-08-23T19:17:26.047Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=b21dd34c-e75c-48e5-a88a-efa1a693b25b] - helloProxy: There.
2018-08-23T19:17:26.051Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=b21dd34c-e75c-48e5-a88a-efa1a693b25b] - hello: There.
```


```
curl http://localhost:9000/api/hello/World

2018-08-23T19:17:58.221Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=e6fde3e0-ab14-400e-8806-c432b66be6af] - hello: World.
2018-08-23T19:18:01.328Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=b8744c4a-79ae-4bcb-aed0-d196fc82ecb1] - hello: World.
2018-08-23T19:18:01.814Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=0168b93b-4fe6-4372-b9af-8b094891a9c5] - hello: World.
2018-08-23T19:18:04.887Z [info] com.lightbend.lagom.recipes.cinnamon.hello.impl.HelloServiceImpl [X_CORRELATION_ID=c1352c06-0521-4b61-bab4-7ab7917017be] - hello: There.
```