package com.lightbend.lagom.samples.mixedpersistence.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.samples.mixedpersistence.hello.api.GreetingMessage;
import com.lightbend.lagom.samples.mixedpersistence.hello.api.HelloService;
import com.lightbend.lagom.samples.mixedpersistence.hello.api.UserGreeting;
import com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity.HelloAggregate;
import com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity.HelloCommand;
import com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity.HelloCommand.Hello;
import com.lightbend.lagom.samples.mixedpersistence.hello.impl.entity.HelloCommand.UseGreetingMessage;
import com.lightbend.lagom.samples.mixedpersistence.hello.impl.readside.Greetings;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {
    private final Greetings greetings;
    private final Duration askTimeout = Duration.ofSeconds(5);
    private ClusterSharding clusterSharding;

    @Inject
    public HelloServiceImpl(Greetings greetings, ClusterSharding clusterSharding) {
        this.greetings = greetings;
        this.clusterSharding = clusterSharding;
        // register the Aggregate as a sharded entity
        this.clusterSharding.init(
            Entity.of(
                HelloAggregate.ENTITY_TYPE_KEY,
                HelloAggregate::create
            )
        );
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> {
            // Look up the aggregete instance for the given ID.
            EntityRef<HelloCommand> ref = clusterSharding.entityRefFor(HelloAggregate.ENTITY_TYPE_KEY, id);
            // Ask the entity the Hello command.
            return ref.
                <HelloCommand.Greeting>ask(replyTo -> new Hello(id, replyTo), askTimeout)
                .thenApply(greeting -> greeting.message);
        };
    }

    @Override
    public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
        return request -> {
            // Look up the aggregete instance for the given ID.
            EntityRef<HelloCommand> ref = clusterSharding.entityRefFor(HelloAggregate.ENTITY_TYPE_KEY, id);
            // Tell the entity to use the greeting message specified.
            return ref.
                <HelloCommand.Confirmation>ask(replyTo -> new UseGreetingMessage(request.getMessage(), replyTo), askTimeout)
                .thenApply(confirmation -> {
                        if (confirmation instanceof HelloCommand.Accepted) {
                            return Done.getInstance();
                        } else {
                            throw new BadRequest(((HelloCommand.Rejected) confirmation).reason);
                        }
                    }
                );
        };
    }


    @Override
    public ServiceCall<NotUsed, PSequence<UserGreeting>> allGreetings() {
        return request -> greetings.all();
    }
}
