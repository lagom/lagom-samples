#!/bin/bash

set -exu

eval $(minikube -p minikube docker-env)

sbt hello-impl/docker:publishLocal
sbt hello-proxy-impl/docker:publishLocal

kubectl apply -f kubernetes/hello-impl.yml
kubectl apply -f kubernetes/hello-proxy-impl.yml

for i in {1..10}
do
  echo "Waiting for pods to get ready..."
  kubectl get pods
  [ `kubectl get pods | grep Running | wc -l` -eq 2 ] && break
  sleep 4
done

if [ $i -eq 10 ]
then
  echo "Pods did not get ready"
  kubectl get pods | tail -n +2 | cut -d " " -f 1 | while read line ; do echo "=== $line ==="; kubectl logs $line ; done
  exit -1
fi

for i in {1..10}
do
  REPLY=`curl --header 'Host: superservice.com' $(sudo -E minikube ip)/proxy/rest-hello/donkey || true`
  [ "$REPLY" = 'Hi donkey!' ] && break
  sleep 4
done  

if [ $i -eq 10 ]
then
  echo "Got reply '$REPLY' instead of 'Hi donkey!'"
  kubectl get pods | tail -2 | cut -d " " -f 1 | while read line ; do echo "=== $line ==="; kubectl logs $line ; done
  exit -1
fi

REPLY=`curl --header 'Host: superservice.com' $(sudo -E minikube ip)/proxy/grpc-hello/donkey`
if [ ! "$REPLY" = 'Hi donkey! (gRPC)' ]
then
  echo "Got reply '$REPLY' instead of 'Hi donkey'"
  kubectl get pods | tail -2 | cut -d " " -f 1 | while read line ; do echo "=== $line ==="; kubectl logs $line ; done
  exit -1
fi
