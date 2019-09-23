package lagom.cluster.sharding.typed

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

trait AkkaTypedClusterComponents {
  def actorSystem: ActorSystem

  lazy val clusterSharding: ClusterSharding = ClusterSharding(actorSystem.toTyped)
}
