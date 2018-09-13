package com.example.helloproxy.impl

import com.example.hello.api.HelloService
import com.example.helloproxy.api.HelloProxyService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import example.myapp.helloworld.grpc.{ GreeterServiceClient, HelloRequest }

import scala.concurrent.{ ExecutionContext, Future }

/**
  * Implementation of the HelloStreamService.
  */
class HelloProxyServiceImpl(helloService: HelloService, greeterClient: GreeterServiceClient)(implicit exCtx: ExecutionContext) extends HelloProxyService {

  def proxyViaHttp(id: String) = ServiceCall { _ =>
    val eventualString: Future[String] = helloService.hello(id).invoke()
    eventualString
  }

  def proxyViaGrpc(id: String) = ServiceCall { _ =>
    val eventualString: Future[String] = greeterClient.sayHello(HelloRequest(id)).map(_.message)
    eventualString
  }
}
