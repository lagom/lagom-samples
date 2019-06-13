#!/bin/bash

## The ID will be the PR number, the branch name on Travis or `local`
if [[ -n "${TRAVIS_PULL_REQUEST}" ]]
then
    ID="${TRAVIS_PULL_REQUEST}" # Pull request number
elif [[ -n "${TRAVIS_BRANCH}" ]]
then
    ID="${TRAVIS_BRANCH//./-}" # Branch name, with dots replaced by hyphens
else
    ID=local
fi
export NAMESPACE=lagom-$CODE_VARIANT-$BUILD_TOOL-$USER-$ID

# Setup env vars for deployment
export OPENSHIFT_SERVER=centralpark2.lightbend.com
## Must not use `DOCKER_REGISTRY` as the name of the ENV VAR since it clashes 
## with the docker-maven-plugin. Instead, use a simplified name
export DCKR_REGISTRY=docker-registry-default.centralpark2.lightbend.com
export DCKR_REPOSITORY=$DCKR_REGISTRY/$NAMESPACE

