/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.recipes.corsjava.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceAcl;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface CorsjavaService extends Service {

    /**
     * Example: curl http://localhost:9000/api/hello/Alice
     */
    ServiceCall<NotUsed, String> hello(String id);

    @Override
    default Descriptor descriptor() {
        return named("corsjava").withCalls(
            pathCall("/api/hello/:id", this::hello)
        ).withAutoAcl(
            true
        ).withServiceAcls(
            ServiceAcl.methodAndPath(Method.OPTIONS, "/api/hello/.*")
        );
    }

}
