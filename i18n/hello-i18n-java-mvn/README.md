# Internationalized Service

This recipe demonstrates how to create a stateless service in Lagom for Java that uses Play's [Internationalization Support](https://www.playframework.com/documentation/2.6.x/JavaI18N).

## Implementation details

The `hello-i18n-impl/src/main/resources` directory contains configuration:

- [`application.conf`](hello-i18n-impl/src/main/resources/application.conf) defines the `play.i18n.langs` property with supported locale codes
- [`messages`](hello-i18n-impl/src/main/resources/messages) contains the default message keys
- [`messages.de-DE`](hello-i18n-impl/src/main/resources/messages.de-DE) and [`messages.es-ES`](hello-i18n-impl/src/main/resources/messages.es-ES) show how to specify messages for alternate locales

The implementation is in [`HelloServiceImpl`](hello-i18n-impl/src/main/java/com/lightbend/lagom/recipes/i18n/hello/impl/HelloServiceImpl.java):

- A [`MessagesApi`](https://www.playframework.com/documentation/2.6.x/api/java/play/i18n/MessagesApi.html) instance is injected into the constructor.
- The `helloWithLang` service call uses [`MessagesApi.preferred(Collection<Lang>)`](https://www.playframework.com/documentation/2.6.x/api/java/play/i18n/MessagesApi.html#preferred-java.util.Collection-) with a singleton collection containing the specified `Lang`. This uses Play's fallback logic to select the closest matching `Messages` instance.
- The `hello` service call uses [service call composition](https://www.lagomframework.com/documentation/1.3.x/java/ServiceImplementation.html#Service-call-composition) and [`PlayServiceCall`](https://www.lagomframework.com/documentation/current/java/api/com/lightbend/lagom/javadsl/server/PlayServiceCall.html) to wrap a service call with a Play `EssentialAction` so that it can obtain the Play `RequestHeader` object and pass that to [`MessagesApi.preferred(Http.RequestHeader)`](https://www.playframework.com/documentation/2.6.x/api/java/play/i18n/MessagesApi.html#preferred-play.mvc.Http.RequestHeader-).
- Both version call through to a private helper method that looks up the message string, passing the `id` service call parameter as an argument to the [`MessageFormat`](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html).

## Testing the recipe

You can test this recipe using 2 separate terminals.

On one terminal start the service:

```
mvn lagom:runAll
```

On a separate terminal, use `curl` to request messages in various languages. There are two ways to do it, by passing a locale parameter in the URL, or by specifying the 'Accept-Language' header.

First, with the URL parameter:

```
$ curl http://localhost:9000/api/hello/en/Alice && echo
Hello, Alice!
$ curl http://localhost:9000/api/hello/es/Alice && echo
¡Hola, Alice!
$ curl http://localhost:9000/api/hello/de-DE/Alice && echo
Hallo, Alice!
$ curl http://localhost:9000/api/hello/fr-FR/Alice && echo # missing, falls back to en
Hello, Alice!
```

Alternatively with the 'Accept-Language' header:

```
$ curl http://localhost:9000/api/hello/Alice && echo # default, uses en
Hello, Alice!
$ curl -H 'Accept-Language: es-ES' http://localhost:9000/api/hello/Alice && echo # with a full locale code
¡Hola, Alice!
$ curl -H 'Accept-Language: es' http://localhost:9000/api/hello/Alice && echo # just the language code
¡Hola, Alice!
$ curl -H 'Accept-Language: de-CH' http://localhost:9000/api/hello/Alice && echo # missing, falls back to en
Hello, Alice!
$ curl -H 'Accept-Language: de-CH, de' http://localhost:9000/api/hello/Alice && echo # or you can specify a list to use the first match
Hallo, Alice!
```
