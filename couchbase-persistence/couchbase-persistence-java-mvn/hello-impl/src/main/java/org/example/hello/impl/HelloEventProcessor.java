package org.example.hello.impl;

import akka.Done;
import akka.stream.alpakka.couchbase.javadsl.CouchbaseSession;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.couchbase.CouchbaseReadSide;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

//#couchbase-begin
public class HelloEventProcessor extends ReadSideProcessor<HelloEvent> {

  static String DOC_ID = "users-actual-greetings";

  CouchbaseReadSide couchbaseReadSide;

  @Inject
  public HelloEventProcessor(CouchbaseReadSide couchbaseReadSide) {
    this.couchbaseReadSide = couchbaseReadSide;
  }

  @Override
  public ReadSideHandler<HelloEvent> buildHandler() {
    return couchbaseReadSide.<HelloEvent>builder("all-greetings")
        .setGlobalPrepare(this::globalPrepare)
        .setEventHandler(HelloEvent.GreetingMessageChanged.class, this::processGreetingMessageChanged)
        .build();
  }

  private CompletionStage<Done> globalPrepare(CouchbaseSession session) {
    return
        session.get(DOC_ID).thenComposeAsync(doc -> {
          if (doc.isPresent()) {
            return CompletableFuture.completedFuture(Done.getInstance());
          }
          return session.insert(JsonDocument.create(DOC_ID, JsonObject.empty()))
              .thenApply(ignore -> Done.getInstance());
        });
  }

  private CompletionStage<Done> processGreetingMessageChanged(CouchbaseSession session, HelloEvent.GreetingMessageChanged evt) {
    return session.get(DOC_ID).thenComposeAsync(docOpt -> {
      JsonDocument doc = docOpt.get(); // document should've been created in globalPrepare

      doc.content().put(evt.name, evt.message);

      return session.upsert(doc);
    }).thenApply(ignore -> Done.getInstance());
  }

  @Override
  public PSequence<AggregateEventTag<HelloEvent>> aggregateTags() {
    return HelloEvent.TAG.allTags();
  }
}
//#couchbase-end
