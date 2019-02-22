package com.example.authjwt.impl

import java.util

import com.example.authjwt.api.AuthJwtService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants.{AUTHORIZATION_HEADER, BEARER_HEADER_PREFIX}
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.lagom.jwt.JwtAuthenticatorHelper
import play.api.libs.ws.ahc.AhcWSComponents

class AuthJwtLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AuthJwtApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AuthJwtApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AuthJwtService])
}

abstract class AuthJwtApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[AuthJwtService](wire[AuthJwtServiceImpl])

  /**
    * PAC4J client.
    * See <a href="http://www.pac4j.org/3.4.x/docs/clients.html">PAC4J documentation</a> for more information about PAC4J clients.
    */
  lazy val jwtClient: HeaderClient = {
    val headerClient = new HeaderClient
    headerClient.setHeaderName(AUTHORIZATION_HEADER)
    headerClient.setPrefixHeader(BEARER_HEADER_PREFIX)
    // Strongly recommendation use `JwtAuthenticatorHelper` for initializing `JwtAuthenticator`.
    headerClient.setAuthenticator(JwtAuthenticatorHelper.parse(config.getConfig("pac4j.lagom.jwt.authenticator")))
    // Custom AuthorizationGenerator to compute the appropriate roles of the authenticated user profile.
    // Roles are fetched from JWT 'roles' attribute.
    // See more http://www.pac4j.org/3.4.x/docs/clients.html#2-compute-roles-and-permissions
    headerClient.setAuthorizationGenerator((_: WebContext, profile: CommonProfile) => {
      if (profile.containsAttribute("roles")) profile.addRoles(profile.getAttribute("roles", classOf[util.Collection[String]]))
      profile
    })
    headerClient.setName("jwt_header")
    headerClient
  }

  lazy val serviceConfig: Config = {
    val config = new Config(jwtClient)
    config.getClients.setDefaultSecurityClients(jwtClient.getName)
    config
  }

}
