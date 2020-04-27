#!/bin/bash

echo "Instaling minikube ..."
brew install minikube

echo "Starting minikube... "
minikube config set memory 8192
minikube config set cpus 2
minikube start

echo "Enabling addons ..."
echo "Enabling metrics-server ..."
minikube addons enable metrics-server

echo "Instaling kustomize ..."
brew install kustomize
