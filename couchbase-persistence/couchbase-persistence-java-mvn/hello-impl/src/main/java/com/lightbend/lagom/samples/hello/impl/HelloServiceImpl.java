package com.lightbend.lagom.samples.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.samples.hello.api.GreetingMessage;
import com.lightbend.lagom.samples.hello.api.HelloService;
import com.lightbend.lagom.samples.hello.api.UserGreeting;
import com.lightbend.lagom.samples.hello.impl.HelloCommand.Hello;
import com.lightbend.lagom.samples.hello.impl.HelloCommand.UseGreetingMessage;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

public class HelloServiceImpl implements HelloService {

    private final Duration askTimeout = Duration.ofSeconds(5);
    private ClusterSharding clusterSharding;
    private GreetingsRepository greetingsRepository;

    @Inject
    public HelloServiceImpl(GreetingsRepository greetingsRepository,
                            ClusterSharding clusterSharding) {
        this.clusterSharding = clusterSharding;
        this.greetingsRepository = greetingsRepository;

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
    public ServiceCall<NotUsed, List<UserGreeting>> userGreetings() {
        return request ->
            greetingsRepository.listUserGreetings();
    }

}
