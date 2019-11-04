#!/bin/bash

if [[ "$(basename "${0#-}")" = "$(basename "${BASH_SOURCE[0]}")" ]]; then
  echo "This is a bash source library, not a bash script!" >&2
  exit 1
fi

setTag() {
    ## Assuming `docker images` sorts in reverse chronological order (and probably it'll be pointing to an
    ## empty repository anyway) we list all images, filter by non-latest and filter by name. Finally, take
    ## only the first match (which is the recently built).
    IMAGE_TAG=`docker images  |grep shopping-cart|grep -v latest | awk -F\  '{print $2}'| head -1`
    export IMAGE_TAG
    echo " - - - "
    echo "Built images shopping-cart:$IMAGE_TAG"
    echo "Built images inventory:$IMAGE_TAG"
    echo " - - - "
}

buildSbt() {
    sbt clean docker:publishLocal
}

buildMvn() {
    mvn --batch-mode -DskipTests package docker:build
}

build() {
    SHOPPING_CART_SOURCES=$1
    BUILD_TOOL=$2

    (
        cd "$SHOPPING_CART_SOURCES" || {
            echo "Failed to cd into $SHOPPING_CART_SOURCES"
            exit 1
        }

        if [ "$BUILD_TOOL" == "sbt" ]; then
            buildSbt
        elif [ "$BUILD_TOOL" == "maven" ]; then
            buildMvn
        else
            echo "unknown build tool [$BUILD_TOOL]"
            exit 1
        fi
    )

}
