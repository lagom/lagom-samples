package org.example.hello.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

/**
 * The Hello service interface.
 * <p>
 * This describes everything that Lagom needs to know about it.
 */
public interface HelloService extends Service {

  /**
   * Get Alice's greeting via write-side
   * <p>
   * Example: curl http://localhost:9000/api/hello/Alice
   */
  ServiceCall<NotUsed, String> hello(UserId id);


  /**
   * Change Alice's greeting
   * <p>
   * Example:
   * curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
   */
  ServiceCall<GreetingMessage, Done> useGreeting(String id);

  /**
   * Get all user greetings via read-side
   * <p>
   * Example: curl http://localhost:9000/api/user-greetings
   */
  ServiceCall<NotUsed, List<UserGreeting>> userGreetings();

  @Override
  default Descriptor descriptor() {
    return named("hello").withCalls(
        pathCall("/api/hello/:id", this::hello),
        pathCall("/api/hello/:id", this::useGreeting),
        pathCall("/api/user-greetings", this::userGreetings)
    ).withAutoAcl(true)
        .withPathParamSerializer(UserId.class, PathParamSerializers.required("user-id", UserId::deserialize, UserId::serialize))
        ;
  }
}
