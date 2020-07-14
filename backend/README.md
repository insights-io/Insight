# Backend

![.github/workflows/backend:test.yml](https://github.com/insights-io/Insight/workflows/.github/workflows/backend:test.yml/badge.svg)
![.github/workflows/backend:check.yml](https://github.com/insights-io/Insight/workflows/.github/workflows/backend:check.yml/badge.svg)
![.github/workflows/backend:gradle-wrapper-validation.yml](https://github.com/insights-io/Insight/workflows/.github/workflows/backend:gradle-wrapper-validation.yml/badge.svg)

## Local development

Look into [local-test-services](local-test-services/README.md).

## Testing

We are heavy believers in integration tests.
Therefore, we heavily utilize [testcontainers](https://www.testcontainers.org/) for most of our tests.
Because testcontainers work with [Docker](https://www.docker.com/), you need to have Docker installed on your computer for tests to pass.
Moreover, we build containers for multi platform. This is currently not supported in default Docker, and requires [BuildKit](https://docs.docker.com/develop/develop-images/build_enhancements/).
You can enable BuiltKit by exporting these variables:

```sh
export COMPOSE_DOCKER_CLI_BUILD=1
export DOCKER_BUILDKIT=1
```
