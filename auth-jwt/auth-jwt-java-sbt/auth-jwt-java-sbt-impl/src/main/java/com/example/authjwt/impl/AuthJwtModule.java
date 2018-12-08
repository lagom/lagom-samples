package com.example.authjwt.impl;

import com.example.authjwt.api.AuthJwtService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.nimbusds.jose.JOSEException;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.lagom.jwt.JwtAuthenticatorHelper;

import java.text.ParseException;
import java.util.Collection;

import static org.pac4j.core.context.HttpConstants.AUTHORIZATION_HEADER;
import static org.pac4j.core.context.HttpConstants.BEARER_HEADER_PREFIX;

/**
 * The module that binds the AuthJwtService so that it can be served.
 * See <a href="https://github.com/pac4j/lagom-pac4j/wiki/Security-configuration">Lagom PAC4J docs</a>
 */
public class AuthJwtModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(AuthJwtService.class, AuthJwtServiceImpl.class);
    }

    /**
     * Provider for PAC4J client.
     * See <a href="http://www.pac4j.org/3.4.x/docs/clients.html">PAC4J documentation</a> for more information about PAC4J clients.
     */
    @Provides
    protected HeaderClient provideHeaderJwtClient(com.typesafe.config.Config configuration) throws ParseException, JOSEException {
        HeaderClient headerClient = new HeaderClient();
        headerClient.setHeaderName(AUTHORIZATION_HEADER);
        headerClient.setPrefixHeader(BEARER_HEADER_PREFIX);
        // Strongly recommendation use `JwtAuthenticatorHelper` for initializing `JwtAuthenticator`.
        headerClient.setAuthenticator(JwtAuthenticatorHelper.parse(configuration.getConfig("pac4j.lagom.jwt.authenticator")));
        // Custom AuthorizationGenerator to compute the appropriate roles of the authenticated user profile.
        // Roles are fetched from JWT 'roles' attribute.
        // See more http://www.pac4j.org/3.4.x/docs/clients.html#2-compute-roles-and-permissions
        headerClient.setAuthorizationGenerator((context, profile) -> {
           if (profile.containsAttribute("roles")) {
               profile.addRoles(profile.getAttribute("roles", Collection.class));
           }
           return profile;
        });
        headerClient.setName("jwt_header");
        return headerClient;
    }

    @Provides
    protected Config provideConfig(HeaderClient headerJwtClient) {
        final Config config = new Config(headerJwtClient);
        config.getClients().setDefaultSecurityClients(headerJwtClient.getName());
        return config;
    }
}
