package com.example.hello.impl

import akka.actor.ActorSystem
import example.myapp.helloworld.grpc.{ AbstractGreeterServiceRouter, HelloReply, HelloRequest }

import scala.concurrent.Future

class HelloGrpcServiceImpl(system: ActorSystem)
  extends AbstractGreeterServiceRouter(system) {
  override def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Hi ${in.name}! (gRPC)"))
}
