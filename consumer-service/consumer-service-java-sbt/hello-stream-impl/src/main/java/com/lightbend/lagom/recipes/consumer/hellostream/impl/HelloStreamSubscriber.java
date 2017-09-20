/*
 * 
 */
package com.lightbend.lagom.recipes.consumer.hellostream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.recipes.consumer.hello.api.HelloEvent;
import com.lightbend.lagom.recipes.consumer.hello.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the HelloService event stream.
 */
public class HelloStreamSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public HelloStreamSubscriber(HelloService helloService, HelloStreamRepository repository) {
        // Create a subscriber
        helloService.helloEvents().subscribe()
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<HelloEvent>create().mapAsync(1, event -> {
                            log.info("Received event: [{}]", event);

                            if (event instanceof HelloEvent.GreetingMessageChanged) {
                                HelloEvent.GreetingMessageChanged messageChanged = (HelloEvent.GreetingMessageChanged) event;
                                // Update the message
                                return repository.updateMessage(messageChanged.getName(), messageChanged.getMessage());

                            } else {
                                // Ignore all other events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );

    }
}
