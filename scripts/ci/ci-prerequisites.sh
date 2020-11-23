#!/bin/bash

docker rmi node:10 node:12 mcr.microsoft.com/azure-pipelines/node8-typescript:latest
# That is 18 GB
sudo rm -rf /usr/share/dotnet
# That is 1.2 GB
sudo rm -rf /usr/share/swift

sudo apt-get clean

df -h
