# Infrastructure

## Development

Development dependencies are managed using [helm](https://github.com/helm/helm).

### Prerequisites

```sh
brew install helm helmfile
helm plugin install https://github.com/databus23/helm-diff
```

### Applying

```sh
helmfile --file k8/dev/helmfile.yaml apply
```
