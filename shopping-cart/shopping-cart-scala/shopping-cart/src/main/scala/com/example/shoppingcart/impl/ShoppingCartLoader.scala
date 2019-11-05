package com.example.shoppingcart.impl

import com.example.shoppingcart.api.ShoppingCartService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents
import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import scala.concurrent.ExecutionContext

class ShoppingCartLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ShoppingCartApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ShoppingCartApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ShoppingCartService])
}

trait ShoppingCartComponents
    extends LagomServerComponents
    with SlickPersistenceComponents
    with HikariCPComponents
    with AhcWSComponents {

  implicit def executionContext: ExecutionContext

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer =
    serverFor[ShoppingCartService](wire[ShoppingCartServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry =
    ShoppingCartSerializerRegistry

  lazy val reportRepository: ShoppingCartReportRepository =
    wire[ShoppingCartReportRepository]
  readSide.register(wire[ShoppingCartReportProcessor])

  // Initialize the sharding for the ShoppingCart aggregate.
  // See https://doc.akka.io/docs/akka/2.6/typed/cluster-sharding.html
  clusterSharding.init(
    Entity(ShoppingCart.typeKey) { entityContext =>
      ShoppingCart(entityContext)
    }
  )
}

abstract class ShoppingCartApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with ShoppingCartComponents
  with LagomKafkaComponents {

}
