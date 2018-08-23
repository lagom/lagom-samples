package com.lightbend.lagom.logcorrelation;

import com.google.inject.AbstractModule;

public class LogCorrelationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LogCorrelationFilter.class);
    }
}
