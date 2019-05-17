#!/bin/bash

setTag() {
    ## Assuming `docker images` sorts in reverse chronological order (and probably it'll be pointing to an 
    ## empty repository anyway) we list all images, filter by non-latest and filter by name. Finally, take 
    ## only the first match (which is the recently built).
    export IMAGE_TAG=`docker images  |grep shopping-cart|grep -v latest | awk -F\  '{print $2}'| head -1`
    echo " - - - "
    echo "Built images shopping-cart:$IMAGE_TAG"
    echo "Built images inventory:$IMAGE_TAG"
    echo " - - - "
}

buildSbt() {
    sbt clean docker:publishLocal 
}
buildMvn() {
    mvn package docker:build
}

build() {
    SHOPPING_CART_SOURCES=$1
    BUILD_TOOL=$2

    if [ "$BUILD_TOOL" == "sbt" ]
    then
        (
            cd $SHOPPING_CART_SOURCES
            buildSbt $SHOPPING_CART_SOURCES
        )
    elif [ "$BUILD_TOOL" == "maven" ]
    then
        (
            cd $SHOPPING_CART_SOURCES
            buildMvn $SHOPPING_CART_SOURCES
        )
    else
        echo "unknown build tool [$BUILD_TOOL]"
        exit 1
    fi
}