package com.example.authjwt.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

/**
 * Simple service for demonstration authenticate/authorize
 */
public interface AuthJwtService extends Service {

    /**
     * Authenticate
     */
    ServiceCall<NotUsed, String> headerJwtAuthenticate();

    /**
     * Authorize by role 'manager'
     */
    ServiceCall<NotUsed, String> headerJwtAuthorize();

    @Override
    default Descriptor descriptor() {
        return named("auth-jwt")
                .withCalls(
                        pathCall("/authenticate", this::headerJwtAuthenticate),
                        pathCall("/authorize", this::headerJwtAuthorize)
                )
                .withAutoAcl(true);
    }
}
