package org.example.hello.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.couchbase.CouchbasePersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import org.example.hello.api.HelloService
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
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[HelloService](wire[HelloServiceImpl])
  override lazy val jsonSerializerRegistry = HelloSerializerRegistry
  persistentEntityRegistry.register(wire[HelloEntity])
  lazy val repo = wire[GreetingsRepository]
  readSide.register(wire[HelloEventProcessor])
}
