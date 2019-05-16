#!/bin/bash


# Recognize the environment
SCRIPTS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
COMMON_SCRIPTS_DIR=$SCRIPTS_DIR/common
DEPLOY_DIR=$SCRIPTS_DIR/..
BASE_DIR=$DEPLOY_DIR/..
SHOPPING_CART_SCALA_DIR=$BASE_DIR/shopping-cart-scala


# Load some helping functions
. $COMMON_SCRIPTS_DIR/deployment-tools.sh
. $COMMON_SCRIPTS_DIR/postgresql.sh

# Setup env vars for deployment
export OPENSHIFT_SERVER=centralpark2.lightbend.com
export NAMESPACE=shopping-cart-lagom
export DOCKER_REGISTRY=docker-registry-default.centralpark2.lightbend.com
export DOCKER_REPOSITORY=$DOCKER_REGISTRY/$NAMESPACE


echo "Attempting login to Openshift cluster (this will fail on PR builds)"
if [ -z ${CP2_PLAY_PASSWORD+x} ]; then echo "CP2_PLAY_PASSWORD is unset."; else echo "CP2_PLAY_PASSWORD is available."; fi
oc login https://$OPENSHIFT_SERVER --username=play-team --password=$CP2_PLAY_PASSWORD  || exit 1


deleteNamespace $NAMESPACE
createNamespace $NAMESPACE

build() {
    cd $SHOPPING_CART_SCALA_DIR
    sbt clean docker:publishLocal 

    ## once the images are published, we can inspect the generated files to discover the tag
    ## The following works by looking for a file named something like `target/scala-2.12/shopping-cart-scala_2.12-353-0e34faa0.pom`
    ## It then reverses the string: mop.0aaf43e0-353-21.2_alacs-trac-gnippohs/21.2-alacs/tegrat
    ## Cuts a few bits using '.' and '-' and reverses again: "353-0e34faa0"
    export IMAGE_TAG=`find target/scala-2.12/sho* | grep pom | grep shopping-cart | rev | cut -d'-' -f1,2 | cut -d'.' -f2|rev`
    cd $SCRIPTS_DIR
    echo " - - - "
    echo "Built images shopping-cart:$IMAGE_TAG"
    echo "Built images inventory:$IMAGE_TAG"
    echo " - - - "
}

build

## TODO parameterize these functions
installPostgres
createDatabase $SHOPPING_CART_SCALA_DIR/schemas/shopping-cart.sql


dockerLogin $DOCKER_REGISTRY

pushImage $DOCKER_REPOSITORY "shopping-cart" $IMAGE_TAG
pushImage $DOCKER_REPOSITORY "inventory" $IMAGE_TAG

setupRbac $NAMESPACE ../specs/common/rbac.yaml

deploy shopping-cart ../specs/shopping-cart.yaml
deploy inventory ../specs/inventory.yaml
