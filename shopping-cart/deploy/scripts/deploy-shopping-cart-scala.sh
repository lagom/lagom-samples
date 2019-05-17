#!/bin/bash

# Recognize the environment
SCRIPTS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
COMMON_SCRIPTS_DIR=$SCRIPTS_DIR/common
DEPLOY_DIR=$SCRIPTS_DIR/..
BASE_DIR=$DEPLOY_DIR/..
SHOPPING_CART_SCALA_DIR=$BASE_DIR/shopping-cart-scala

. $COMMON_SCRIPTS_DIR/installers.sh
installOC

# 0. Setup the NAMESPACE (predates all)
export NAMESPACE=shopping-cart-lagom-scala

# 1. Setup session and load some helping functions
. $COMMON_SCRIPTS_DIR/setupEnv.sh
. $COMMON_SCRIPTS_DIR/clusterLogin.sh

# 2. Load extra tools to manage the deployment
. $COMMON_SCRIPTS_DIR/deployment-tools.sh

# 3. Recreate the NAMESPACE
deleteNamespace $NAMESPACE
createNamespace $NAMESPACE

# 4. Install PG and create a DB and a schema
. $COMMON_SCRIPTS_DIR/postgresql.sh
## TODO parameterize PG functions
installPostgres
createDatabase $SHOPPING_CART_SCALA_DIR/schemas/shopping-cart.sql


# 5. Build the docker images to be deployed
buildSbt() {
    cd $SHOPPING_CART_SCALA_DIR
    sbt clean docker:publishLocal 


    ## TODO: This hack to discover the IMAGE_TAG is very brittle and doesn't work when there are local changes.
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

buildSbt
#buildMaven

# 6. Push images
dockerLogin $DOCKER_REGISTRY
pushImage $DOCKER_REPOSITORY "shopping-cart" $IMAGE_TAG
pushImage $DOCKER_REPOSITORY "inventory" $IMAGE_TAG

# 7. Deploy application
setupRbac $NAMESPACE ../specs/common/rbac.yaml
deploy shopping-cart ../specs/shopping-cart.yaml
deploy inventory ../specs/inventory.yaml

# 8. Test application
waitForApp app=shopping-cart 3
waitForApp app=inventory 1

buildRoute shopping-cart
buildRoute inventory
