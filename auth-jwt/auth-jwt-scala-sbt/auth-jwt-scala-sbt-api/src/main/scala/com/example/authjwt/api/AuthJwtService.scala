package com.example.authjwt.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * Simple service for demonstration authenticate/authorize
  */
trait AuthJwtService extends Service {

  /**
    * Authenticate
    */
  def headerJwtAuthenticate: ServiceCall[NotUsed, String]

  /**
    * Authorize by role 'manager'
    */
  def headerJwtAuthorize: ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("auth-jwt")
      .withCalls(
        pathCall("/authenticate", this.headerJwtAuthenticate),
        pathCall("/authorize", this.headerJwtAuthorize)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
