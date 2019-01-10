package org.example.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.alpakka.couchbase.javadsl.CouchbaseSession;
import com.couchbase.client.java.document.json.JsonObject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import org.example.hello.api.GreetingMessage;
import org.example.hello.api.HelloService;
import org.example.hello.api.UserGreeting;
import org.example.hello.impl.HelloCommand.Hello;
import org.example.hello.impl.HelloCommand.UseGreetingMessage;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelloServiceImpl implements HelloService {

  private final PersistentEntityRegistry persistentEntityRegistry;
  private GreetingsRepository greetingsRepository;

  @Inject
  public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry, GreetingsRepository greetingsRepository) {
    this.persistentEntityRegistry = persistentEntityRegistry;
    persistentEntityRegistry.register(HelloEntity.class);
    this.greetingsRepository = greetingsRepository;
  }

  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return request -> {
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
      return ref.ask(new Hello(id));
    };
  }

  @Override
  public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
    return request -> {
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
      return ref.ask(new UseGreetingMessage(request.getMessage()));
    };
  }

  @Override
  public ServiceCall<NotUsed, List<UserGreeting>> userGreetings() {
    return request ->
        greetingsRepository.listUserGreetings();
  }

}
