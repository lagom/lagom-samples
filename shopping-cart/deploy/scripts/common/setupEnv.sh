#!/bin/bash

# Setup env vars for deployment
export OPENSHIFT_SERVER=centralpark2.lightbend.com
export DOCKER_REGISTRY=docker-registry-default.centralpark2.lightbend.com
export DOCKER_REPOSITORY=$DOCKER_REGISTRY/$NAMESPACE

