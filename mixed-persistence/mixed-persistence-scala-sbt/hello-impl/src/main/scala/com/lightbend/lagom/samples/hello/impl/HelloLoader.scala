package com.lightbend.lagom.samples.hello.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.samples.hello.api.HelloService
import com.lightbend.lagom.samples.hello.impl.readside.{ GreetingsRepository, GreetingsEventProcessor }
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.WriteSideCassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.ReadSideJdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.db.HikariCPComponents
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
    // Mix in the JDBC read-side
    with ReadSideJdbcPersistenceComponents
    // A connection pool is required any time we mix in a JDBC-based persistence Component (like
    // the read-side above)
    with HikariCPComponents
    // Mixing in WriteSideCassandraPersistenceComponents instead of CassandraPersistenceComponents
    // we can control what read-side to mix in
    with WriteSideCassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[HelloService](wire[HelloServiceImpl])

  lazy val eventProcessor = wire[GreetingsEventProcessor]
  lazy val repo = wire[GreetingsRepository]

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = HelloSerializerRegistry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(HelloState.typeKey)(
      entityContext => HelloBehavior.create(entityContext)
    )
  )

}
