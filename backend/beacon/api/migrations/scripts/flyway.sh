#!/usr/bin/env bash

POSTGRES_HOST=${POSTGRES_HOST:="localhost"}
POSTGRES_DB=${POSTGRES_DB:="postgres"}
POSTGRES_USER=${POSTGRES_USER:="postgres"}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:="postgres"}

JDBC_URL="jdbc:postgresql://${POSTGRES_HOST}/${POSTGRES_DB}"

echo "Flyway connecting to ${JDBC_URL} via user ${DATABASE_USER}"

/flyway/flyway -url=${JDBC_URL} -user=${POSTGRES_USER} -password=${POSTGRES_PASSWORD} -schemas=rec -locations=filesystem:sql -connectRetries=60 $@
