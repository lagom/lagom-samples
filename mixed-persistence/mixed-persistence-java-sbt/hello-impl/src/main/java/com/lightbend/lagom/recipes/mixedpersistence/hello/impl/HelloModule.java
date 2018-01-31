package com.lightbend.lagom.recipes.mixedpersistence.hello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.JavadslJdbcOffsetStore;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.JdbcReadSideImpl;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.JdbcSessionImpl;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.SlickProvider;
import com.lightbend.lagom.internal.persistence.jdbc.SlickOffsetStore;
import com.lightbend.lagom.javadsl.persistence.jdbc.JdbcReadSide;
import com.lightbend.lagom.javadsl.persistence.jdbc.JdbcSession;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.recipes.mixedpersistence.hello.api.HelloService;
import com.lightbend.lagom.recipes.mixedpersistence.hello.impl.readside.Greetings;
import com.lightbend.lagom.javadsl.persistence.jdbc.GuiceSlickProvider;
/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(HelloService.class, HelloServiceImpl.class);
        bind(Greetings.class).asEagerSingleton();

        // JdbcPersistenceModule is disabled in application.conf to avoid conflicts with CassandraPersistenceModule.
        // We need to explicitly re-add the SlickOffsetStore binding that is required by the JpaPersistenceModule.
        bind(SlickProvider.class).toProvider(GuiceSlickProvider.class);
        bind(SlickOffsetStore.class).to(JavadslJdbcOffsetStore.class);
        // To use JdbcReadSide instead of JpaReadSide, uncomment these lines:
        // bind(JdbcReadSide.class).to(JdbcReadSideImpl.class);
        // bind(JdbcSession.class).to(JdbcSessionImpl.class);
    }
}
