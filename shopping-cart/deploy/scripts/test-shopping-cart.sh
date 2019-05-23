#!/bin/bash

##  Usage:
##    ./test-shopping-cart.sh 
##    ./test-shopping-cart.sh <shopping-cart-java|shopping-cart-scala>
##    ./test-shopping-cart.sh <shopping-cart-java|shopping-cart-scala> <sbt|maven>
##
CODE_VARIANT=${1:-shopping-cart-scala}
shift 
BUILD_TOOL=${1:-sbt}

## The ID will be the PR number or `local`
ID=${TRAVIS_PULL_REQUEST:-local}

# Recognize the environment
SCRIPTS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
COMMON_SCRIPTS_DIR=$SCRIPTS_DIR/common
DEPLOY_DIR=$SCRIPTS_DIR/..
## BASE_DIR must point to <git_repo_root>/shopping-cart
BASE_DIR=$DEPLOY_DIR/..
SHOPPING_CART_SOURCES=$BASE_DIR/$CODE_VARIANT

# 0. Setup the NAMESPACE (predates all)
#   e.g. lagom-shopping-cart-scala-sbt-23  (23 is the PR number)
export NAMESPACE=lagom-$CODE_VARIANT-$BUILD_TOOL-$USER-$ID
echo "Testing deployment $NAMESPACE"

# 1. Setup session and load some helping functions
. $COMMON_SCRIPTS_DIR/setupEnv.sh
. $COMMON_SCRIPTS_DIR/clusterLogin.sh

# 2. Load extra tools to manage the deployment
. $COMMON_SCRIPTS_DIR/deployment-tools.sh
useNamespace $NAMESPACE

SHOPPING_CART_HOST=$(oc get route shopping-cart -o jsonpath='{.spec.host}')
INVENTORY_HOST=$(oc get route inventory -o jsonpath='{.spec.host}')

SHOPPING_CART_ID=$(openssl rand -base64 6 | tr -- '+=/' '-_~')
PRODUCT_ID=$(openssl rand -base64 6 | tr -- '+=/' '-_~')

sleep 3 
echo "Touch a shopping-cart [$SHOPPING_CART_ID]"
echo "https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID" 
curl "https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID" || exit 1
echo

echo "Add items on the shopping-cart [$SHOPPING_CART_ID]"
curl -H "Content-Type: application/json" -X POST -d '{"productId": "'$PRODUCT_ID'", "quantity": 2}' \
"https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID"
echo

echo "Check status of the cart [$SHOPPING_CART_ID]"
curl "https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID"
echo 

echo "Checkout the cart [$SHOPPING_CART_ID]"
curl "https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID/checkout" -X POST
echo 

echo "Check status of the cart [$SHOPPING_CART_ID]"
curl "https://$SHOPPING_CART_HOST/shoppingcart/$SHOPPING_CART_ID"
echo 


# So that it can be copied...
# echo
# echo "Await for eventual Inventory update using: "
# echo curl "https://$INVENTORY_HOST/inventory/$PRODUCT_ID"
# echo -n "Waiting for inventory service to process shopping cart message."

# count=0
# while [ $(curl -s "https://$INVENTORY_HOST/inventory/$PRODUCT_ID") != "-2" ]
# do
#     (( count = count + 1 ))
#     if [ $count -gt 30 ]
#     then
#         echo "FAILED."
#         echo "Expected $PRODUCT_ID to have -2 inventory, but got $(curl -s "http://$INVENTORY_HOST/inventory/$PRODUCT_ID")."
#         exit 1
#     fi
#     echo -n "."
#     sleep 2
# done

echo "SUCCESS!!"

