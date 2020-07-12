#!/usr/bin/env bash

set -ex
args=()  
args+=('--tag' "$IMAGE")
args+=('--builder' 'qemu')
args+=('--network' 'host')
if [ "$PUSH_IMAGE" = "true" ];
then
  args+=('--platform' 'linux/arm/v7,linux/amd64')
  args+=('-o' 'type=registry')
else
  args+=('--platform' 'linux/amd64')
  args+=('-o' 'type=docker,dest=out')
fi

args+=('-f' "$1" "$BUILD_CONTEXT")
docker buildx build "${args[@]}"

if [ "$PUSH_IMAGE" = "false" ];
then
  docker load -i out
  rm out
fi
