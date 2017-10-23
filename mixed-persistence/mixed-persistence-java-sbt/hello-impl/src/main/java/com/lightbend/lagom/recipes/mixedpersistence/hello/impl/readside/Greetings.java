package com.lightbend.lagom.recipes.mixedpersistence.hello.impl.readside;

import com.google.common.collect.ImmutableMap;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaReadSide;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaSession;
import com.lightbend.lagom.recipes.mixedpersistence.hello.api.UserGreeting;
import com.lightbend.lagom.recipes.mixedpersistence.hello.impl.entity.HelloEvent;
import com.lightbend.lagom.recipes.mixedpersistence.hello.impl.entity.HelloEvent.GreetingMessageChanged;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Singleton
public class Greetings {
    private static final String SELECT_ALL_QUERY =
            // JPA entities are mutable and cannot be safely shared across threads.
            // The "SELECT NEW" syntax is used to return immutable result objects.
            "SELECT NEW com.lightbend.lagom.recipes.mixedpersistence.hello.api.UserGreeting(g.id, g.message)" +
                    " FROM UserGreetingRecord g";

    // JpaSession provides an asynchronous, non-blocking API to
    // perform JPA actions in Slick's database execution context.
    private final JpaSession jpaSession;

    @Inject
    Greetings(JpaSession jpaSession, ReadSide readSide) {
        this.jpaSession = jpaSession;

        // This registers an event processor with Lagom.
        // Event processors are used to update the read-side
        // database with changes made to persistent entities.
        readSide.register(UserGreetingRecordWriter.class);
    }

    /**
     * Asynchronously queries the read-side database for a list of all greetings that have been set for any HelloEntity.
     *
     * @return a {@link CompletionStage} that completes with a list of all stored greetings
     */
    public CompletionStage<PSequence<UserGreeting>> all() {
        return jpaSession
                .withTransaction(this::selectAllUserGreetings)
                .thenApply(TreePVector::from);
    }

    private List<UserGreeting> selectAllUserGreetings(EntityManager entityManager) {
        return entityManager
                .createQuery(SELECT_ALL_QUERY, UserGreeting.class)
                .getResultList();
    }

    /**
     * Event processor that handles {@link GreetingMessageChanged} events
     * by writing {@link UserGreetingRecord} rows to the read-side database table.
     */
    static class UserGreetingRecordWriter extends ReadSideProcessor<HelloEvent> {
        private final JpaReadSide jpaReadSide;

        @Inject
        UserGreetingRecordWriter(JpaReadSide jpaReadSide) {
            this.jpaReadSide = jpaReadSide;
        }

        @Override
        public ReadSideHandler<HelloEvent> buildHandler() {
            return jpaReadSide.<HelloEvent>builder("UserGreetingRecordWriter")
                    .setGlobalPrepare(entityManager -> createSchema())
                    .setEventHandler(GreetingMessageChanged.class, this::processGreetingMessageChanged)
                    .build();
        }

        private void createSchema() {
            // This is a convenience for creating the read-side table in development mode.
            // It relies on a Hibernate-specific property to provide idempotent schema updates.
            Persistence.generateSchema("default",
                    ImmutableMap.of("hibernate.hbm2ddl.auto", "update")
            );
        }

        private void processGreetingMessageChanged(EntityManager entityManager, GreetingMessageChanged greetingMessageChanged) {
            UserGreetingRecord record = entityManager.find(UserGreetingRecord.class, greetingMessageChanged.getName());
            if (record == null) {
                record = new UserGreetingRecord();
                record.setId(greetingMessageChanged.getName());
                entityManager.persist(record);
            }
            record.setMessage(greetingMessageChanged.getMessage());
        }

        @Override
        public PSequence<AggregateEventTag<HelloEvent>> aggregateTags() {
            return HelloEvent.TAG.allTags();
        }
    }
}
