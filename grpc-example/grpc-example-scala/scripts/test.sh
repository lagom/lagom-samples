#!/bin/bash

set -exu

sbt docker:publishLocal

kubectl apply -f kubernetes/grpcservice.yml
kubectl apply -f kubernetes/httptogrpc.yml

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
  exit -1
fi

for i in {1..10}
do
  REPLY=`curl --header 'Host: superservice.com' $(sudo -E minikube ip)/hello/donkey || true`
  [ "$REPLY" = 'Hello, donkey' ] && break
  sleep 4
done  

if [ $i -eq 10 ]
then
  echo "Got reply '$REPLY' instead of 'Hello, donkey'"
  kubectl get pods | tail -2 | cut -d " " -f 1 | while read line ; do echo "=== $line ==="; kubectl logs $line ; done
  exit -1
fi
