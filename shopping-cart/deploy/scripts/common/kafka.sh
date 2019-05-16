
if [ $INSTALL_KAFKA_REQUIRES_ADMIN == 1 ]
then
    #login-sysadmin
    oc login -u system:admin
    #login-sysadmin
fi

if [ $INSTALL_STRIMZI == 1 ]
then
    echo Installing Strimzi...

    #install-strimzi
    curl -L https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.8.2/strimzi-cluster-operator-0.8.2.yaml | \
      sed -e "s/myproject/$NAMESPACE/" | oc apply -f - -n $NAMESPACE
    #install-strimzi
fi

if [ $INSTALL_KAFKA == 1 ]
then
    echo Installing Kafka...
    if [ $KAFKA_NODES == 1 ]
    then
        #install-kafka-single
        oc apply -f deploy/kafka-single.yaml -n $NAMESPACE
        #install-kafka-single
    elif [ $KAFKA_NODES == 3 ]
    then
        #install-kafka-multi
        oc apply -f deploy/kafka.yaml -n $NAMESPACE
        #install-kafka-multi
    else
        echo "Kafka nodes must either be 1 or 3"
        exit 1
    fi

    waitForApp strimzi.io/name=strimzi-kafka $KAFKA_NODES 2
fi

if [ $INSTALL_KAFKA_REQUIRES_ADMIN == 1 ]
then
    #login-developer
    oc login -u developer
    #login-developer
fi
