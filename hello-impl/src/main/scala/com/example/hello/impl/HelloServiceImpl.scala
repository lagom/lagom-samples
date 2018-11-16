/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.example.hello.impl

import com.example.hello.api.HelloService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl() extends HelloService {

  override def hello(id: String) = ServiceCall { _ =>
    Future.successful(s"Hi $id!")
  }
}
