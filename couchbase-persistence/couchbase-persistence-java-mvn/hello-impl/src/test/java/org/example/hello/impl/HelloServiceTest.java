package org.example.hello.impl;

import com.lightbend.lagom.javadsl.persistence.PersistenceModule;
import com.lightbend.lagom.javadsl.persistence.couchbase.CouchbasePersistenceModule;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.example.hello.api.GreetingMessage;
import org.example.hello.api.HelloService;
import org.example.hello.api.UserId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

  private static ServiceTest.TestServer server;

  @BeforeClass
  public static void setUp() {
    //#couchbase-begin
    final ServiceTest.Setup setup = defaultSetup().configureBuilder(b ->
        b.overrides(
            //need to add it manually because lagom disables it when no cassandra or jdbc lagom persistence activated
            new PersistenceModule(),
            //import it manually because Lagom is not aware of this type of persistence
            new CouchbasePersistenceModule()
        ))
        //required for persistence
        .withCluster();
    //#couchbase-end

    server = ServiceTest.startServer(setup);
  }

  @AfterClass
  public static void tearDown() {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  @Test
  public void shouldStorePersonalizedGreeting() throws Exception {
    HelloService service = server.client(HelloService.class);

    service.useGreeting("Alice").invoke(new GreetingMessage("Hi")).toCompletableFuture().get(5, SECONDS);
    String msg2 = service.hello(UserId.deserialize("Alice")).invoke().toCompletableFuture().get(5, SECONDS);
    assertEquals("Hi, Alice!", msg2);

    String msg3 = service.hello(UserId.deserialize("Bob")).invoke().toCompletableFuture().get(5, SECONDS);
    assertEquals("Hello, Bob!", msg3); // default greeting
  }

  @Test
  public void shouldStorePersonalizedGreeting2() throws Exception {
    HelloService service = server.client(HelloService.class);

    service.useGreeting("Alice").invoke(new GreetingMessage("Hi")).toCompletableFuture().get(5, SECONDS);
    String msg2 = service.hello(UserId.deserialize("Alice")).invoke().toCompletableFuture().get(5, SECONDS);
    assertEquals("Hi, Alice!", msg2);

    String msg3 = service.hello(UserId.deserialize("Bob")).invoke().toCompletableFuture().get(5, SECONDS);
    assertEquals("Hello, Bob!", msg3); // default greeting
  }

}
