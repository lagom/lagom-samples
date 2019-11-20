package com.lightbend.lagom.samples.hello.impl;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

public class HelloAggregateTest {
    private static final String inmemConfig =
        "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n";

    private static final String snapshotConfig =
        "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\" \n"
            + "akka.persistence.snapshot-store.local.dir = \"target/snapshot-"
            + UUID.randomUUID().toString()
            + "\" \n";

    private static final String config = inmemConfig + snapshotConfig;

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource(config);

    @Test
    public void testHello() {
        String id = "Alice";
        ActorRef<HelloCommand> ref =
            testKit.spawn(
                HelloAggregate.create(
                    // Unit testing the Aggregate requires an EntityContext but starting
                    // a complete Akka Cluster or sharding the actors is not requried.
                    // The actorRef to the shard can be null as it won't be used.
                    new EntityContext(HelloAggregate.ENTITY_TYPE_KEY, id,  null)
                )
            );

        TestProbe<HelloCommand.Greeting> probe =
            testKit.createTestProbe(HelloCommand.Greeting.class);
        ref.tell(new HelloCommand.Hello(id,probe.getRef()));
        probe.expectMessage(new HelloCommand.Greeting("Hello, Alice!"));
    }

    @Test
    public void testUpdateGreeting() {
        String id = "Alice";
        ActorRef<HelloCommand> ref =
            testKit.spawn(
                HelloAggregate.create(
                    // Unit testing the Aggregate requires an EntityContext but starting
                    // a complete Akka Cluster or sharding the actors is not requried.
                    // The actorRef to the shard can be null as it won't be used.
                    new EntityContext(HelloAggregate.ENTITY_TYPE_KEY, id,  null)
                )
            );

        TestProbe<HelloCommand.Confirmation> probe1 =
            testKit.createTestProbe(HelloCommand.Confirmation.class);
        ref.tell(new HelloCommand.UseGreetingMessage("Hi", probe1.getRef()));
        probe1.expectMessage(new HelloCommand.Accepted());

        TestProbe<HelloCommand.Greeting> probe2 =
            testKit.createTestProbe(HelloCommand.Greeting.class);
        ref.tell(new HelloCommand.Hello(id,probe2.getRef()));
        probe2.expectMessage(new HelloCommand.Greeting("Hi, Alice!"));
    }
}
