#!/bin/bash

##  Usage:
##    ./deploy-shopping-cart.sh
##    ./deploy-shopping-cart.sh <shopping-cart-java|shopping-cart-scala>
##    ./deploy-shopping-cart.sh <shopping-cart-java|shopping-cart-scala> <sbt|maven>
##
CODE_VARIANT=${1:-shopping-cart-scala}
shift 
BUILD_TOOL=${1:-sbt}


# Recognize the environment
SCRIPTS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
COMMON_SCRIPTS_DIR=$SCRIPTS_DIR/common
DEPLOY_DIR=$SCRIPTS_DIR/..
## BASE_DIR must point to <git_repo_root>/shopping-cart
BASE_DIR=$DEPLOY_DIR/..
SHOPPING_CART_SOURCES=$BASE_DIR/$CODE_VARIANT


. $COMMON_SCRIPTS_DIR/installers.sh
installOC

# 1. Setup session and load some helping functions
. $COMMON_SCRIPTS_DIR/setupEnv.sh
setupEnv "$CODE_VARIANT" "$BUILD_TOOL"
echo "Deploying to $NAMESPACE"
. $COMMON_SCRIPTS_DIR/clusterLogin.sh
clusterLogin "$CODE_VARIANT" "$BUILD_TOOL"

# 2. Load extra tools to manage the deployment
. $COMMON_SCRIPTS_DIR/deployment-tools.sh

# 3. Recreate the NAMESPACE
deleteNamespace $NAMESPACE
createNamespace $NAMESPACE
useNamespace $NAMESPACE

# 4.1 Install PG and create a DB and a schema
. $COMMON_SCRIPTS_DIR/postgresql.sh
## TODO parameterize PG functions
installPostgres
createDatabase $SHOPPING_CART_SOURCES/schemas/shopping-cart.sql

# 4.2 Setup Kafka (assumes Strimzi is installed and the `play-team` user can access/use it)
. $COMMON_SCRIPTS_DIR/kafka.sh
installKafka $NAMESPACE $DEPLOY_DIR/specs/common/kafka.yaml

# 5. Build the docker images to be deployed
. $COMMON_SCRIPTS_DIR/builds.sh
build $SHOPPING_CART_SOURCES $BUILD_TOOL
setTag

# 6. Push images
dockerLogin $DCKR_REGISTRY
pushImage $DCKR_REPOSITORY "shopping-cart" $IMAGE_TAG
pushImage $DCKR_REPOSITORY "inventory" $IMAGE_TAG

# 7. Deploy application
setupRbac $NAMESPACE $DEPLOY_DIR/specs/common/rbac.yaml
deploy shopping-cart $DEPLOY_DIR/specs/shopping-cart.yaml
deploy inventory $DEPLOY_DIR/specs/inventory.yaml

# 8. Test application
waitForApp app=shopping-cart 3
waitForApp app=inventory 1

buildRoute shopping-cart
buildRoute inventory
