#!/usr/bin/env bash

DATABASE_HOST=${DATABASE_HOST:="localhost"}
JDBC_URL="jdbc:postgresql://${DATABASE_HOST}/${DATABASE_NAME}"

echo "Starting flyway ${JDBC_URL}"

/flyway/flyway -url=${JDBC_URL} -user=${DATABASE_USER} -password=${DATABASE_PASSWORD} -schemas=rec -locations=filesystem:sql -connectRetries=60 $@
