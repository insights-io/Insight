#!/bin/bash

DISTRIBUTION_ID="$1"

if [ -z "$DISTRIBUTION_ID" ]
  then
    echo "Please provide AWS Cloudfront distribution ID"
    exit 1
fi

aws cloudfront wait distribution-deployed --id $DISTRIBUTION_ID
ETAG=$(aws cloudfront get-distribution-config --id $DISTRIBUTION_ID | jq -r '.ETag')
aws cloudfront delete-distribution --id $DISTRIBUTION_ID --if-match $ETAG
