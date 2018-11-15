/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.example.helloproxy.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }

trait HelloProxyService extends Service {

  def proxyViaHttp(id:String): ServiceCall[NotUsed, String]
  def proxyViaGrpc(id:String): ServiceCall[NotUsed, String]

  override final def descriptor = {
    import Service._

    named("hello-proxy")
      .withCalls(
        restCall(Method.GET, "/proxy/rest-hello/:id", proxyViaHttp _),
        restCall(Method.GET, "/proxy/grpc-hello/:id", proxyViaGrpc _)
      ).withAutoAcl(true)
  }
}

