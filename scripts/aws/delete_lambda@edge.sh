#!/bin/bash

CLEANUP_STATUS=
EXPECTED_CLEANUP_STATUS="DONE"
RETRY_BACKOFF=30
LAMBDA_NAME="$1"

if [ -z "$LAMBDA_NAME" ]
  then
    echo "Please provide AWS Lambda@Edge function name"
    exit 1
fi

while [ "$CLEANUP_STATUS" != "$EXPECTED_CLEANUP_STATUS" ]
do
  RESULT=$(aws lambda delete-function --function-name "$LAMBDA_NAME" 2>&1)

  if [ "$(echo "$RESULT" | grep -c "replicated function")" -eq 1 ]
  then
    echo "$LAMBDA_NAME is still a replicated function. Sleeping for $RETRY_BACKOFF seconds..."
    sleep "$RETRY_BACKOFF"
  else
    echo "Finished with output: $RESULT"
    CLEANUP_STATUS="$EXPECTED_CLEANUP_STATUS"
  fi
done
