package com.lightbend.lagom.recipes.i18n.hello.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import play.i18n.Lang;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

/**
 * The Hello service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the Hello.
 */
public interface HelloService extends Service {

    /**
     * Example: curl http://localhost:9000/api/hello/Alice
     * Example: curl -H 'Accept-Language: es-ES' http://localhost:9000/api/hello/Carlos
     */
    ServiceCall<NotUsed, String> hello(String id);

    /**
     * Example: curl http://localhost:9000/api/hello/de/Erik
     */
    ServiceCall<NotUsed, String> helloWithLang(Lang lang, String id);

    @Override
    default Descriptor descriptor() {
        return named("hello")
                .withCalls(
                        pathCall("/api/hello/:id", this::hello),
                        pathCall("/api/hello/:lang/:id", this::helloWithLang)
                )
                .withPathParamSerializer(Lang.class, PathParamSerializers.required("lang", Lang::forCode, Lang::code))
                .withAutoAcl(true);
    }
}
