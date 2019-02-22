package com.example.authjwt.impl

import akka.NotUsed
import com.example.authjwt.api.AuthJwtService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer.requireAnyRole
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.lagom.scaladsl.SecuredService

import scala.concurrent.Future

/**
  * Implementation of the AuthJwtService.
  */
class AuthJwtServiceImpl(override val securityConfig: Config) extends AuthJwtService with SecuredService {
  override def headerJwtAuthenticate: ServiceCall[NotUsed, String] = {
    authenticate { profile =>
      // `authenticate` requires a `ServerServiceCall` to access the headers.
      ServerServiceCall { _ =>
        Future.successful(profile.getId)
      }
    }
  }

  override def headerJwtAuthorize: ServiceCall[NotUsed, String] = {
    authorize(requireAnyRole[CommonProfile]("manager"), (profile: CommonProfile) =>
      // `authorize` requires a `ServerServiceCall` to access the headers.
      ServerServiceCall { _: NotUsed =>
        Future.successful(profile.getId)
    })
  }
}
