package com.example.shoppingcart.impl

import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTagger
import com.lightbend.lagom.scaladsl.persistence.AggregateEventShards
import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import akka.cluster.sharding.typed.scaladsl.EntityContext

object LagomTaggerAdapter {

  
  def apply[Event <: AggregateEvent[Event]](
      entityCtx: EntityContext,
      lagomTagger: AggregateEventTagger[Event]
  ): Event => Set[String] = { evt =>
    val tag =
      lagomTagger match {
        case tagger: AggregateEventTag[_] =>
          tagger.tag
        case shardedTagger: AggregateEventShards[_] =>
          shardedTagger.forEntityId(entityCtx.entityId).tag
      }
    Set(tag)
  }
}
