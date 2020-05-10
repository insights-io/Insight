# Local development

## Kubernetes (minikube)

We are using [minikube](https://minikube.sigs.k8s.io/) to run Kubernetes locally.
This helps us test our services in Kubernetes environment before they are pushed into Cloud.

### (One time) setup

```shell script
brew install minikube
minikube start
minikube status
minikube addons enable metrics-server
```

### Deploy a service (example)

```shell script
eval $(minikube docker-env)
docker build . -f auth-service/Dockerfile.jvm -t eu.gcr.io/insight/auth-service
kubectl apply -f auth-service/manifests.yaml
```

### Prometheus

https://github.com/coreos/kube-prometheus

```shell script
git clone https://github.com/coreos/kube-prometheus
cd kube-prometheus
# Create the namespace and CRDs, and then wait for them to be available before creating the remaining resources
kubectl create -f manifests/setup
until kubectl get servicemonitors --all-namespaces ; do date; sleep 1; echo ""; done
kubectl create -f manifests/
```
