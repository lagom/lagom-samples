package com.lightbend.lagom.samples.hello.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.cluster.typed.ClusterShardingTypedComponents
import com.lightbend.lagom.scaladsl.persistence.couchbase.CouchbasePersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import com.lightbend.lagom.samples.hello.api.HelloService
import play.api.libs.ws.ahc.AhcWSComponents

class HelloLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloService])
}

abstract class HelloApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    //#couchbase-begin
    with CouchbasePersistenceComponents
    //#couchbase-end
    with AhcWSComponents
    with ClusterShardingTypedComponents {

  override lazy val lagomServer = serverFor[HelloService](wire[HelloServiceImpl])
  override lazy val jsonSerializerRegistry = HelloSerializerRegistry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(HelloState.typeKey)(
      entityContext => HelloBehavior.create(entityContext)
    )
  )

  lazy val repo = wire[GreetingsRepository]
  readSide.register(wire[HelloEventProcessor])
}
