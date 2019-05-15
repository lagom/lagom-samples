#! /bin/bash

# Waits for an application to come up and be ready
# eg waitForApp app=foo 3 2
# app=foo is a label selector
# 3 is the number of replicas to expect to be running
# 2 is the number of containers that are expected to be ready in each pod (defaults to 1)
# Will time out after 300 seconds
waitForApp() {
    SELECTOR=$1
    REPLICAS=$2
    CONTAINERS="1/1"
    if [ $# == 3 ]
    then
        CONTAINERS="$3/$3"
    fi

    count=0
    echo -n "Waiting for $SELECTOR to be provisioned."
    while [ "$(oc get pods -l $SELECTOR -n $NAMESPACE --no-headers=true --ignore-not-found=true | grep Running | grep "$CONTAINERS" | wc -l)" -lt $REPLICAS ]
    do
        sleep 2
        echo -n "."
        (( count = count + 1 ))
        if [ $count -gt 150 ]
        then
            echo " failed."
            echo "$SELECTOR didn't come up after 300 seconds. Attempting to dump diagnostics..."
            # Find any nodes that are in error, and output their logs
            for pod in $(oc get pods --no-headers | grep -v Running | cut -f1 -d" ")
            do
                echo Logs for $pod:
                oc logs --all-containers $pod || :
            done
            exit 1
        fi
    done

    echo " done."
}
