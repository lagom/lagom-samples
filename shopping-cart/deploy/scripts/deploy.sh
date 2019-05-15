#!/bin/bash


# Recognize the environment
SCRIPTS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
DEPLOY_DIR=$SCRIPTS_DIR/..
BASE_DIR=$DEPLOY_DIR/..
SHOPPING_CART_SCALA__DIR=$BASE_DIR/shopping-cart-scala


# Load some helping functions
. $SCRIPTS_DIR/waitForApp.sh

# Setup env vars for deployment
export OPENSHIFT_SERVER=centralpark2.lightbend.com
# OPENSHIFT_PROJECT and NAMESPACE will be used indistinctly in this scripts
export OPENSHIFT_PROJECT=shopping-cart-lagom
export NAMESPACE=$OPENSHIFT_PROJECT
export IMAGE_SHOPPING_CART=shopping-cart
export IMAGE_INVENTORY=inventory

export DOCKER_REGISTRY_SERVER=docker-registry-default.centralpark2.lightbend.com
export DOCKER_REGISTRY=$DOCKER_REGISTRY_SERVER/$OPENSHIFT_PROJECT


echo "Attempting login to Openshift cluster (this will fail on PR builds)"
if [ -z ${CP2_PLAY_PASSWORD+x} ]; then echo "CP2_PLAY_PASSWORD is unset."; else echo "CP2_PLAY_PASSWORD is available."; fi
oc login https://$OPENSHIFT_SERVER --username=play-team --password=$CP2_PLAY_PASSWORD  || exit 1


oc delete project $OPENSHIFT_PROJECT

build() {
        cd $SHOPPING_CART_SCALA__DIR
        sbt docker:publishLocal 

        ## once the images are published, we can inspect the generated files to discover the tag
        ## The following works by looking for a file named something like `target/scala-2.12/shopping-cart-scala_2.12-353-0e34faa0.pom`
        ## It then reverses the string: mop.0aaf43e0-353-21.2_alacs-trac-gnippohs/21.2-alacs/tegrat
        ## Cuts the few bits using '.' and '-' and reverses again: "353-0e34faa0"
        export IMAGE_TAG=`find target/scala-2.12/sho* | grep pom | grep shopping-cart | rev | cut -d'-' -f1,2 | cut -d'.' -f2|rev`
        cd $SCRIPTS_DIR
}

build

echo "Built image $IMAGE_SHOPPING_CART:$IMAGE_TAG"


oc new-project $OPENSHIFT_PROJECT

## Must not try to create a PGsql app too son after creating the project in line 22. Damn OC
sleep 5
. postgresql.sh

oc whoami -t | docker login -u unused --password-stdin $DOCKER_REGISTRY_SERVER

pushImage() {
    REGISTRY=$1
    shift
    NAME=$1
    shift
    TAG=$1
    shift

    echo "Tagging image: $NAME:$TAG $DOCKER_REGISTRY/$NAME:$TAG"
    echo "Tagging image: $NAME:latest $DOCKER_REGISTRY/$NAME:latest"
    docker tag $NAME:$TAG $REGISTRY/$NAME:$TAG
    docker tag $NAME:$TAG $REGISTRY/$NAME:latest
    docker push $REGISTRY/$NAME:$TAG
    docker push $REGISTRY/$NAME:latest
}

pushImage $DOCKER_REGISTRY "shopping-cart" $IMAGE_TAG


## final setups before deploying
# a secret for the shopping-cart app 
oc create secret generic shopping-cart-application-secret --from-literal=secret="$(openssl rand -base64 48)"
# tuning openshift's image lookup
oc set image-lookup shopping-cart

## DEPLOYOLO!
sed -e "s/myproject/shopping-cart-lagom/g" ../specs/shopping-cart.yaml | oc apply -f - 
