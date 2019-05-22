#!/bin/bash

# Setup env vars for deployment
export OPENSHIFT_SERVER=centralpark2.lightbend.com
## Must not use `DOCKER_REGISTRY` as the name of the ENV VAR since it clashes 
## with the docker-maven-plugin. Instead, use a simplified name
export DCKR_REGISTRY=docker-registry-default.centralpark2.lightbend.com
export DCKR_REPOSITORY=$DCKR_REGISTRY/$NAMESPACE

