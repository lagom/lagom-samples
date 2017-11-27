package com.lightbend.lagom.recipes.i18n.hello.impl;

import com.lightbend.lagom.recipes.i18n.hello.api.HelloService;
import org.junit.Test;
import play.i18n.Lang;

import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

    @Test
    public void shouldServeLocalizedGreeting() throws Exception {
        withServer(defaultSetup(), server -> {
            HelloService service = server.client(HelloService.class);

            String greetAlice = await(service.hello("Alice").invoke());
            assertEquals("Hello, Alice!", greetAlice); // default greeting

            String greetBob = await(service.hello("Bob").invoke());
            assertEquals("Hello, Bob!", greetBob); // default greeting

            // Exact locale match
            String greetCarlos = await(
                    service.hello("Carlos")
                            .handleRequestHeader(requestHeader -> requestHeader.withHeader("Accept-Language", "es-ES"))
                            .invoke()
            );
            assertEquals("¡Hola, Carlos!", greetCarlos);

            // Language code only
            String greetDieter = await(
                    service.hello("Dieter")
                            .handleRequestHeader(requestHeader -> requestHeader.withHeader("Accept-Language", "de"))
                            .invoke()
            );
            assertEquals("Hallo, Dieter!", greetDieter);

            // Fallback
            String greetElia = await(
                    service.hello("Elia")
                            .handleRequestHeader(requestHeader -> requestHeader.withHeader("Accept-Language", "de-CH, de"))
                            .invoke()
            );
            assertEquals("Hallo, Elia!", greetElia);

            // No match
            String greetFrancine = await(
                    service.hello("Francine")
                            .handleRequestHeader(requestHeader -> requestHeader.withHeader("Accept-Language", "fr-FR, fr"))
                            .invoke()
            );
            assertEquals("Hello, Francine!", greetFrancine);

            // Explicit lang parameter
            String greetGemma = await(service.helloWithLang(new Lang(Locale.GERMAN), "Gemma").invoke());
            assertEquals("Hallo, Gemma!", greetGemma);
        });
    }

    private <T> T await(CompletionStage<T> completionStage)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completionStage.toCompletableFuture().get(5, SECONDS);
    }

}
