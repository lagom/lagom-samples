package com.lightbend.lagom.recipes.cbpanel.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.recipes.cbpanel.api.DemoService;

public class DemoModule extends AbstractModule implements ServiceGuiceSupport {
    
    @Override
    protected void configure() {
        bindService(DemoService.class, DemoServiceImpl.class);
    }
}
