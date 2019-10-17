#!/bin/bash

## TODO: fix this --> schemas/shopping-cart.sql is not found
## TODO: parameterize this script
#    secret     postgresql-admin-password
#    secret     postgres-shopping-cart
#    db_name    shopping_cart_database
#    db_user    shopping_cart_user
#    db_script  schemas/shopping-cart.sql


installPostgres() {
  echo "Installing Postgres"

  ## TODO: use `--name=some-name-psql` so we can deploy multiple PG's on a single openshift project
  ## create a new app using postgresql
  oc new-app postgresql

  ## create new admin pwd 
  oc create secret generic postgresql-admin-password --from-literal=password="$(openssl rand -base64 24)"

  ## patch app to use new admin pwd
  oc patch deploymentconfig postgresql --patch '{"spec": {"template": {"spec": {"containers": [
    {"name": "postgresql", "env": [
      {"name": "POSTGRESQL_ADMIN_PASSWORD", "valueFrom":
        {"secretKeyRef": {"name": "postgresql-admin-password", "key": "password"}}
      }
    ]}
  ]}}}}'
  ## Wait for postgresql pods/app to be ready
  waitForApp app=postgresql 1
}


createDatabase() {
  DATABASE_SCHEMA=$1

  ## create user pwd 
  oc create secret generic postgres-shopping-cart --from-literal=username=shopping_cart_user --from-literal=password="$(openssl rand -base64 24)"

  ## Forward PGSQL port to conenct remotely
  oc port-forward svc/postgresql 15432:5432 &
  echo Sleeping for 10 seconds while port forward is established...
  sleep 10

  #create-ddl
  psql -h localhost -p 15432 -U postgres <<DDL
CREATE DATABASE shopping_cart_database;
REVOKE CONNECT ON DATABASE shopping_cart_database FROM PUBLIC;
CREATE USER shopping_cart_user WITH PASSWORD '$(oc get secret postgres-shopping-cart -o jsonpath='{.data.password}' | base64 --decode)';
GRANT CONNECT ON DATABASE shopping_cart_database TO shopping_cart_user;

\connect shopping_cart_database;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO shopping_cart_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO shopping_cart_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, USAGE ON SEQUENCES TO shopping_cart_user;

\include $DATABASE_SCHEMA;
DDL

  ## Close the port-forward
  kill %1
}
