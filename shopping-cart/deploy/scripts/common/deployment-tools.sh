#! /bin/bash

## Deletes a namespace and sleeps a bit for the deletion to complete.
## This operation will timeout in 20 seconds.
##
## 1. NAMESPACE
deleteNamespace() {
    NAMESPACE=$1
    # Don't do anything if the namespace doesn't already exist
    if [ -z "$(oc get project $NAMESPACE --no-headers=true)" ]
    then
        return
    fi
    oc delete project $NAMESPACE --wait=true
    # Project deletion is async, even with --wait=true
    # See https://bugzilla.redhat.com/show_bug.cgi?id=1700026#c6
    echo "Waiting for $NAMESPACE to be deleted."
    local -i count=0
    while [ -n "$(oc get project $NAMESPACE --no-headers=true)" ]
    do
        sleep 2
        (( count = count + 1 ))
        if [ $count -gt 10 ]
        then
            echo "$NAMESPACE couldn't be deleted."
            exit 1
        fi
    done
    echo "$NAMESPACE was deleted."
}

useNamespace() {
    NAMESPACE=$1
    oc project $NAMESPACE
}

## Creates a namespace. This operation will timeout in 20 seconds.
##
## 1. NAMESPACE
createNamespace() {
    NAMESPACE=$1
    echo "Waiting for $NAMESPACE to be created."
    local -i count=0
    while [ -z "$(oc get project $NAMESPACE --no-headers=true)" ]
    do
        sleep 2
        # aggressively retries to build the project. This is necessary because even if `oc get projects` 
        # returns no results, internally the cluster may still be cleaning up if an `oc delete` happened 
        # recently
        oc new-project $NAMESPACE --v=0
        (( count = count + 1 ))
        if [ $count -gt 10 ]
        then
            echo "$NAMESPACE couldn't be created."
            exit 1
        fi
    done
    echo "$NAMESPACE was created."
    ## Extra sleep to allow the cluster to completely see the changes.
    sleep 5
}

## Login the local client into a docker registry.
## 
## 1. DCKR_REGISTRY
dockerLogin() {
    DCKR_REGISTRY=$1
    oc whoami -t | docker login -u unused --password-stdin $DCKR_REGISTRY
} 
   

# Waits for an application to come up and be ready
# eg waitForApp app=foo 3 2
# app=foo is a label selector
# 3 is the number of replicas to expect to be running
# 2 is the number of containers that are expected to be ready in each pod (defaults to 1)
# Will time out after 300 seconds
waitForApp() {
    SELECTOR=$1
    REPLICAS=$2
    CONTAINERS="1/1"
    if [ $# == 3 ]
    then
        CONTAINERS="$3/$3"
    fi

    local -i count=0
    echo -n "Waiting for $SELECTOR to be provisioned."
    while [ "$(oc get pods -l $SELECTOR -n $NAMESPACE --no-headers=true --ignore-not-found=true | grep Running | grep "$CONTAINERS" | wc -l)" -lt $REPLICAS ]
    do
        sleep 2
        echo -n "."
        (( count = count + 1 ))
        if [ $count -gt 150 ]
        then
            echo " failed."
            echo "$SELECTOR didn't come up after 300 seconds. Attempting to dump diagnostics..."
            # Find any nodes that are in error, and output their logs
            for pod in $(oc get pods --no-headers | grep -v Running | cut -f1 -d" ")
            do
                echo Logs for $pod:
                oc logs --all-containers $pod || :
            done
            exit 1
        fi
    done

    echo " done."
}



## Tags the local $NAME:$TAG image as $NAME:latest and pushes both tags to $REGISTRY.
##  PRE: the docker client is logged in to the remote repository where $REGISTRY is
##  PRE: the $NAME:$TAG image exists locally
##
##  1. REGISTRY
##  2. NAME
##  3. TAG
pushImage() {
    REGISTRY=$1
    NAME=$2
    TAG=$3

    echo "Tagging image: $NAME:$TAG $REGISTRY/$NAME:$TAG"
    echo "Tagging image: $NAME:latest $REGISTRY/$NAME:latest"
    docker tag $NAME:$TAG $REGISTRY/$NAME:$TAG || exit 1
    docker tag $NAME:$TAG $REGISTRY/$NAME:latest || exit 1
    docker push $REGISTRY/$NAME:$TAG || exit 1
    docker push $REGISTRY/$NAME:latest || exit 1
}

## Deploys a Play or Lagom app. Creates a ${SERVICE_NAME}-application-secret (key is `secret`)
## so the application spec YAML can read it and inject into Play's 'play.http.secret.key' via an ENV VAR
##  PRE: `oc` is logged in and using the appropriate NAMESPACE
##
##  1. SERVICE_NAME
##  2. YAML_SOURCE
deploy() {
    SERVICE_NAME=$1
    YAML_SOURCE=$2

    ## final setups before deploying
    # a secret for the app 
    oc create secret generic ${SERVICE_NAME}-application-secret --from-literal=secret="$(openssl rand -base64 48)"
    # tuning openshift's image lookup
    oc set image-lookup $SERVICE_NAME
    ## DEPLOYOLO!
    oc apply -f $YAML_SOURCE || exit 1
}

## Sets RBAC and Roles so services can query the k8s API.
##  PRE: `oc` is logged in and using the appropriate NAMESPACE
##
##  1. NAMESPACE
##  2. YAML_SOURCE
setupRbac() {
    NAMESPACE=$1
    YAML_SOURCE=$2
    sed -e "s/myproject/$NAMESPACE/g" $YAML_SOURCE | oc apply -f - 
}

buildRoute() {
  SERVICE_NAME=$1
  ## Must use `oc create route edge ...` instead of `oc expose service` because 
  ## centralpark2 only allows HTTPS traffic. Using Edge means external HTTPS 
  ## with TLS termination on the edge.
  ##
  ## The hostname is set to '${SERVICE_NAME}-$NAMESPACE.$OPENSHIFT_SERVER', since that's
  ## the convention used by openshift when using `oc expose service`.
  local hostname="${SERVICE_NAME}-$NAMESPACE"
  ## The hostname is truncated after 63 characters, the maximum length for a DNS hostname.
  hostname=${hostname:0:63}
  ## Hostnames can't end with a hyphen, so remove any trailing hyphens
  shopt -s extglob # required to enable the below pattern to match multiple hyphens
  hostname=${hostname%%+(-)}
  oc create route edge --service=$SERVICE_NAME --hostname=${hostname}.$OPENSHIFT_SERVER  || exit 1
}
