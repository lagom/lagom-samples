package org.example.hello.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.couchbase.scaladsl.CouchbaseSession
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.document.JsonDocument
import com.lightbend.lagom.scaladsl.persistence.{
  AggregateEventTag,
  EventStreamElement,
  ReadSideProcessor
}
import com.lightbend.lagom.scaladsl.persistence.couchbase.CouchbaseReadSide

import scala.concurrent.{ExecutionContext, Future}

//#couchbase-begin
//#couchbase-end
