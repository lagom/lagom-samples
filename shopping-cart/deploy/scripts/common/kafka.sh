#!/bin/bash

if [[ "$(basename "${0#-}")" = "$(basename "${BASH_SOURCE[0]}")" ]]; then
  echo "This is a bash source library, not a bash script!" >&2
  exit 1
fi

## Installs Kafka using Strimzi.
## Assumes Strimzi is installed and the user can access/use it
##
##
installKafka() {
    NAMESPACE=$1
    SPEC=$2
    KAFKA_NODES=3
    oc apply -f $SPEC -n $NAMESPACE
    waitForApp strimzi.io/name=strimzi-kafka $KAFKA_NODES 2
}
