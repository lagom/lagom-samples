package org.example.hello.impl;

import akka.Done;
import akka.stream.alpakka.couchbase.javadsl.CouchbaseSession;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.couchbase.CouchbaseReadSide;
import org.example.hello.api.UserGreeting;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

//#couchbase-begin
public class GreetingsRepository {

    private CouchbaseSession couchbaseSession;

    @Inject
    public GreetingsRepository(CouchbaseSession couchbaseSession, ReadSide readSide) {
        this.couchbaseSession = couchbaseSession;
        readSide.register(HelloEventProcessor.class);
    }

    public CompletionStage<List<UserGreeting>> listUserGreetings() {
        return couchbaseSession.get(GreetingsRepository.HelloEventProcessor.DOC_ID)
            .thenApply(docOpt -> {
                if (docOpt.isPresent()) {
                    JsonObject content = docOpt.get().content();
                    return content.getNames().stream().map(
                        name -> new UserGreeting(name, content.getString(name))
                    ).collect(Collectors.toList());
                } else {
                    return Collections.emptyList();
                }
            });
    }

    private static class HelloEventProcessor extends ReadSideProcessor<HelloEvent> {

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
}
//#couchbase-end
