#!/usr/bin/env bash

# Travis configured to setup a postgres db 
# we only need to setup the schema, user and password
psql -c "CREATE DATABASE shopping_cart;" -U postgres
psql -c "CREATE USER shopping_cart WITH PASSWORD 'shopping_cart';" -U postgres
