package com.example.helloproxy.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface HelloProxyService extends Service {

    ServiceCall<NotUsed, String> proxyViaHttp(String id);
    ServiceCall<NotUsed, String> proxyViaGrpc(String id);

    default Descriptor descriptor() {
        return named("hello-proxy")
            .withCalls(
                restCall(Method.GET, "/proxy/rest-hello/:id", this::proxyViaHttp),
                restCall(Method.GET, "/proxy/grpc-hello/:id", this::proxyViaGrpc)
            ).withAutoAcl(true);
    }
}

