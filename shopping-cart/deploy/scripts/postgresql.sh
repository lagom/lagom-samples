
echo "Installing Postgres"

## create a new app using postgresql
oc new-app postgresql

## create new admin pwd 
oc create secret generic postgresql-admin-password --from-literal=password="$(openssl rand -base64 24)"
## create user pwd 
oc create secret generic postgres-shopping-cart --from-literal=username=shopping_cart --from-literal=password="$(openssl rand -base64 24)"

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

## Forward PGSQL port to conenct remotely
oc port-forward svc/postgresql 15432:5432 &
echo Sleeping for 10 seconds while port forward is established...
sleep 10

#create-ddl
psql -h localhost -p 15432 -U postgres <<DDL
CREATE DATABASE shopping_cart;
REVOKE CONNECT ON DATABASE shopping_cart FROM PUBLIC;
CREATE USER shopping_cart WITH PASSWORD '$(oc get secret postgres-shopping-cart -o jsonpath='{.data.password}' | base64 --decode)';
GRANT CONNECT ON DATABASE shopping_cart TO shopping_cart;

\connect shopping_cart;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO shopping_cart;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO shopping_cart;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, USAGE ON SEQUENCES TO shopping_cart;

\include schemas/shopping-cart.sql;
DDL

## Close the port-forward
kill %1