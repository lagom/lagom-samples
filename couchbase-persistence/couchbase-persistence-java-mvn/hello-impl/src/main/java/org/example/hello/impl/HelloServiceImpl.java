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
import org.example.hello.api.UserId;
import org.example.hello.impl.HelloCommand.Hello;
import org.example.hello.impl.HelloCommand.UseGreetingMessage;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {

  private final PersistentEntityRegistry persistentEntityRegistry;
  private final CouchbaseSession couchbaseSession;

  @Inject
  public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ReadSide readSide, CouchbaseSession session) {
    this.persistentEntityRegistry = persistentEntityRegistry;
    persistentEntityRegistry.register(HelloEntity.class);
    readSide.register(HelloEventProcessor.class);
    this.couchbaseSession = session;
  }

  @Override
  public ServiceCall<NotUsed, String> hello(UserId id) {
    return request -> {
      // Look up the hello world entity for the given ID.
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id.getValue());
      // Ask the entity the Hello command.
      return ref.ask(new Hello(id.getValue()));
    };
  }

  @Override
  public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
    return request -> {
      // Look up the hello world entity for the given ID.
      PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
      // Tell the entity to use the greeting message specified.
      return ref.ask(new UseGreetingMessage(request.getMessage()));
    };

  }

  @Override
  public ServiceCall<NotUsed, List<UserGreeting>> userGreetings() {
    return request -> couchbaseSession.get(HelloEventProcessor.DOC_ID)
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

}
