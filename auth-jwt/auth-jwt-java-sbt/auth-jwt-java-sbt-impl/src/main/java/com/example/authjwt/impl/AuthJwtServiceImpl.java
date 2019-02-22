package com.example.authjwt.impl;

import akka.NotUsed;
import com.example.authjwt.api.AuthJwtService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pac4j.core.config.Config;
import org.pac4j.lagom.javadsl.SecuredService;

import javax.inject.Inject;

import static org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer.requireAnyRole;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the AuthJwtService.
 */
public class AuthJwtServiceImpl implements AuthJwtService, SecuredService {

    private final Config securityConfig;

    @Inject
    public AuthJwtServiceImpl(Config securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public Config getSecurityConfig() {
        return securityConfig;
    }

    @Override
    public ServiceCall<NotUsed, String> headerJwtAuthenticate() {
        return authenticate(profile ->
                request -> completedFuture(profile.getId())
        );
    }

    @Override
    public ServiceCall<NotUsed, String> headerJwtAuthorize() {
        return authorize(requireAnyRole("manager"), profile ->
                request -> completedFuture(profile.getId())
        );
    }
}
