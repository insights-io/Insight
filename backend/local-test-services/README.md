# Local test services

This is a collection of utilities and best practices for a local development environment.

## Kubernetes (minikube)

We are using [minikube](https://minikube.sigs.k8s.io/) to run Kubernetes locally.
This helps us test our services in Kubernetes environment before they are pushed into the Cloud.

### (One time) setup 

```shell script
./bootstrap.sh # This will install & setup environment for local k8s
```

### Point your shell to minikube's docker-deamon

```shell script
eval $(minikube -p minikube docker-env)
``` 

### Deploying a service (example)

```shell script
docker build . -f auth-service/docker/Dockerfile.jvm -t eu.gcr.io/insight/auth-service
kustomize build auth-service/k8/overlays/development | kubectl apply -f -
```