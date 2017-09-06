package com.lightbend.lagom.recipes.corsscala.impl

import com.lightbend.lagom.recipes.corsscala.api.CorsscalaService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class CorsscalaServiceImpl() extends CorsscalaService {

  override def hello(name: String) = ServiceCall { _ =>
    Future.successful(s"Hello $name!")
  }

}
