package com.lightbend.lagom.samples.hello.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface HelloService extends Service {

    ServiceCall<NotUsed, String> hello(String id);

    ServiceCall<GreetingMessage, Done> useGreeting(String id);

    ServiceCall<NotUsed, List<UserGreeting>> userGreetings();

    @Override
    default Descriptor descriptor() {
        return named("hello").withCalls(
            pathCall("/api/hello/:id", this::hello),
            pathCall("/api/hello/:id", this::useGreeting),
            pathCall("/api/user-greetings", this::userGreetings)
        )
            .withAutoAcl(true);
    }
}
