#!/usr/bin/env bash

SIDECAR_TERMINATED_FILE=${SIDECAR_TERMINATED_FILE:="/tmp/pod/main-terminated"}

echo "Trapping ${SIDECAR_TERMINATED_FILE}..."

trap "touch ${SIDECAR_TERMINATED_FILE}" EXIT

sh /usr/app/scripts/flyway.sh migrate
