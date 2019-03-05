package com.example.shoppingcart.impl;

import akka.actor.ActorSystem;
import akka.management.javadsl.AkkaManagement;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import play.Environment;

import javax.inject.Inject;

public class ClusterBootstrapStart {

    @Inject
    public ClusterBootstrapStart(Environment environment, ActorSystem actorSystem) {
        if (environment.isProd()) {
            AkkaManagement.get(actorSystem).start();
            ClusterBootstrap.get(actorSystem).start();
        }
    }
}
