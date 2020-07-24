# Infrastructure

Infrastructure dependencies are managed using [helm](https://github.com/helm/helm).

## Getting started

### Prerequisites

```sh
brew install helm helmfile
helm plugin install https://github.com/databus23/helm-diff
```

### Applying

```sh
helmfile --file k8/development/helmfile.yaml -e production apply
```

## Tooling

##### Analytics

Metabase [[staging]](https://metabase.dev.snuderls.eu/)

##### Kubernetes

Dashboard [[staging]](https://dashboard.dev.snuderls.eu/)

##### Logging

Kibana [[staging]](https://kibana.dev.snuderls.eu/app/kibana/)

##### Monitoring

Grafana [[staging]](https://grafana.dev.snuderls.eu/)
Prometheus [[staging]](https://prometheus.dev.snuderls.eu/)

##### Tracing

Jaeger [[staging]](https://tracing.dev.snuderls.eu/)

##### GitOps

ArgoCD [[staging]](https://argo.dev.snuderls.eu/)
Atlantis [[staging]](https://atlantis.dev.snuderls.eu/)
