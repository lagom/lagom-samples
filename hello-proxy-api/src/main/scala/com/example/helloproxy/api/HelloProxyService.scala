package com.example.helloproxy.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

trait HelloProxyService extends Service {

  def proxy(id:String): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._

    named("hello-proxy")
      .withCalls(
        restCall(Method.GET, "/proxy/rest-hello/:id", proxy _)
      ).withAutoAcl(true)
  }
}

