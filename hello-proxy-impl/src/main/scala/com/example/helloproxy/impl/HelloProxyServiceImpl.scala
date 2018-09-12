package com.example.helloproxy.impl

import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

/**
  * Implementation of the HelloStreamService.
  */
class HelloProxyServiceImpl(helloService: HelloService) extends HelloProxyService {

  def proxy(id: String) = ServiceCall { _ =>
    val eventualString: Future[String] = helloService.hello(id).invoke()
    eventualString
  }
}
