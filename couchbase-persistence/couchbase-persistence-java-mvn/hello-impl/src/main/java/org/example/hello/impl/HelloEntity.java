package org.example.hello.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.example.hello.impl.HelloCommand.Hello;
import org.example.hello.impl.HelloCommand.UseGreetingMessage;
import org.example.hello.impl.HelloEvent.GreetingMessageChanged;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloEntity extends PersistentEntity<HelloCommand, HelloEvent, HelloState> {

  @Override
  public Behavior initialBehavior(Optional<HelloState> snapshotState) {
    BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(new HelloState("Hello", "")));

    b.setCommandHandler(UseGreetingMessage.class, (cmd, ctx) ->
    {
      List<GreetingMessageChanged> evts = new ArrayList<>();
      evts.add(new GreetingMessageChanged(entityId(), cmd.message));
      return ctx.thenPersistAll(evts,
          () -> ctx.reply(Done.getInstance()));
    });

    b.setEventHandler(GreetingMessageChanged.class,
        evt -> new HelloState(evt.message, "timestamp"));
    b.setReadOnlyCommandHandler(Hello.class,
        (cmd, ctx) -> ctx.reply(state().getMessage() + ", " + cmd.name + "!"));

    return b.build();
  }

}
