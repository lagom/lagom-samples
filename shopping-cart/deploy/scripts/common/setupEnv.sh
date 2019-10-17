#!/bin/bash

if [[ "$(basename "${0#-}")" = "$(basename "${BASH_SOURCE[0]}")" ]]; then
  echo "This is a bash source library, not a bash script!" >&2
  exit 1
fi

## 1. CODE_VARIANT
## 2. BUILD_TOOL
setupEnv() {
    CODE_VARIANT="$1"
    BUILD_TOOL="$2"

    ## The ID will be the PR number, the branch name on Travis or `local`
    if [[ -n "${TRAVIS_PULL_REQUEST}" && "${TRAVIS_PULL_REQUEST}" != false ]]
    then
        ID="${TRAVIS_PULL_REQUEST}" # Pull request number
    elif [[ -n "${TRAVIS_BRANCH}" ]]
    then
        ID="${TRAVIS_BRANCH//./-}" # Branch name, with dots replaced by hyphens
    else
        ID=local
    fi
    export NAMESPACE=lagom-$CODE_VARIANT-$BUILD_TOOL-$USER-$ID
    ## The NAMESPACE is truncated after 63 characters, the maximum length for an OpenShift project name.
    NAMESPACE=${NAMESPACE:0:63}


    # Setup env vars for deployment
    export OPENSHIFT_SERVER=centralpark2.lightbend.com
    ## Must not use `DOCKER_REGISTRY` as the name of the ENV VAR since it clashes 
    ## with the docker-maven-plugin. Instead, use a simplified name
    export DCKR_REGISTRY=docker-registry-default.centralpark2.lightbend.com
    export DCKR_REPOSITORY=$DCKR_REGISTRY/$NAMESPACE
}
