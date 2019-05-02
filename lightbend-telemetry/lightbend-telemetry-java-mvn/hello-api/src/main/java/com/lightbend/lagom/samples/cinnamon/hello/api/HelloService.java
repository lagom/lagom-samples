package com.lightbend.lagom.samples.cinnamon.hello.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface HelloService extends Service {

    /**
     * Example: curl http://localhost:9000/api/hello/Alice
     */
    ServiceCall<NotUsed, String> hello(String id);


    /**
     * Example: curl http://localhost:9000/api/hello-proxy/Alice
     */
    ServiceCall<NotUsed, String> helloProxy(String id);

    @Override
    default Descriptor descriptor() {
        return named("hello").withCalls(
                pathCall("/api/hello/:id", this::hello),
                pathCall("/api/hello-proxy/:id", this::helloProxy)
        ).withAutoAcl(true);
    }

}
