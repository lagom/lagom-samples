# CORS recipe for Lagom's Scaladsl

In order to enable CORS on a Lagom service the following steps are required:

1. Include `filters` as a dependency on your `-impl` project. `filters` is a package provided by Play Framework.
2. Mix-in the `CORSComponents` on your `Application`.
3. Also on you `Application`, override the `lazy val httpFilters` (if you aren't overriding it already) and include the `corsFilter` on the sequence of filters. 
4. Finally, add an ACL on your `Service.Descriptor` matching the `OPTIONS` method for the paths you are exposing on your Service Gateway.

## Testing the recipe


You can test this recipe using 2 separate terminals.

On one terminal start the service:

```
sbt runAll
```

On a separate terminal, use `curl` to trigger a pre-flight request:

```
curl -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: origin, x-requested-with" \
        -H "Origin: http://www.some-domain.com"  \
        -X OPTIONS http://localhost:9000/api/hello/123 -v        
```

Note how the request uses the `OPTIONS` method and targets the [Lagom Service Gateway](https://www.lagomframework.com/documentation/1.3.x/scala/ServiceLocator.html) (`localhost:9000`).

## More resources

This topic has been discussed in the Lagom Mailing List [a](https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!msg/lagom-framework/_3Hjvp18NNU/ygu8Pa5wAQAJ) [few](https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!msg/lagom-framework/7YZccqRUS4g/HNMykAiGBAAJ) [times](https://groups.google.com/forum/?utm_medium=email&utm_source=footer#!msg/lagom-framework/3y0wgIMillE/ItT1rPDfBgAJ), if this recipe doesn't resolve your doubts, feel free to ask for help in the community.

You will also find the [Play Framework documentation](https://playframework.com/documentation/2.5.x/CorsFilter) on the topic quite useful. 
