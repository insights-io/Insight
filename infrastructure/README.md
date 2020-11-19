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

# Or just a specific chart
helmfile --file k8/development/helmfile.yaml -e production -l name=grafana apply
```

## Tooling

##### Analytics

Metabase [[staging]](https://metabase.rebrowse.dev/)

##### Kubernetes

Dashboard [[staging]](https://dashboard.rebrowse.dev/)

##### Logging

Kibana [[staging]](https://kibana.rebrowse.dev/app/kibana/)

##### Monitoring

Grafana [[staging]](https://grafana.rebrowse.dev/)
Prometheus [[staging]](https://prometheus.rebrowse.dev/)

##### Tracing

Jaeger [[staging]](https://tracing.rebrowse.dev/)

##### GitOps

ArgoCD [[staging]](https://argo.rebrowse.dev/)
Atlantis [[staging]](https://atlantis.rebrowse.dev/)
