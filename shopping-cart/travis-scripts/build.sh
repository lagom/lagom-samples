#! /bin/bash

# Travis configured to setup a postgres db 
# we only need to setup the schema, user and password
psql -c "CREATE DATABASE shopping_cart;" -U postgres
psql -c "CREATE USER shopping_cart WITH PASSWORD 'shopping_cart';" -U postgres

SHOPPING_CART_SOURCES=$1
BUILD_TOOL=$2

if [ "$BUILD_TOOL" == "sbt" ]; then
    bin/runSbtJob $SHOPPING_CART_SOURCES test 
elif [ "$BUILD_TOOL" == "maven" ]; then
    mvn -f $SHOPPING_CART_SOURCES test 
else
    echo "unknown build tool [$BUILD_TOOL]"
    exit 1
fi
