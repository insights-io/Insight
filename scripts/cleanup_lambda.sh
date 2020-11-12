#!/bin/bash

CLEANUP_STATUS=
EXPECTED_CLEANUP_STATUS="DONE"

if [ -z "$1" ]
  then
    echo "Please provide lambda function name"
    exit 1
fi

while [ "$CLEANUP_STATUS" != "$EXPECTED_CLEANUP_STATUS" ]
do
  RESULT="$(aws lambda delete-function --function-name $1 2>&1)"

  if [ "$(echo "$RESULT" | grep -c "replicated function")" -eq 1 ]
  then
    echo "$1 is still a replicated function. Sleeping for 30 seconds..."
    sleep 30
  else
    echo "Finished with output: $RESULT"
    CLEANUP_STATUS="$EXPECTED_CLEANUP_STATUS"
  fi
done
