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