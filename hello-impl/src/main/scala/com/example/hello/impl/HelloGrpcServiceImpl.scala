package com.example.hello.impl

import akka.stream.Materializer
import example.myapp.helloworld.grpc.{ AbstractGreeterServiceRouter, HelloReply, HelloRequest }

import scala.concurrent.Future

class HelloGrpcServiceImpl(mat: Materializer) extends AbstractGreeterServiceRouter(mat){
  override def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Hi ${in.name}! (gRPC)"))
}
