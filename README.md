# Insight

![.github/workflows/core.yml](https://github.com/Meemaw/Insight/workflows/.github/workflows/core.yml/badge.svg)
[![codecov](https://codecov.io/gh/Meemaw/insight/branch/master/graph/badge.svg)](https://codecov.io/gh/Meemaw/insight)

**Monorepo for Insight related services and applications.**

Frontend related code processes are managed by [Lerna](https://github.com/lerna/lerna) and [Yarn Workspaces](https://yarnpkg.com/lang/en/docs/workspaces/).
Backend related code processes are managed by [Gradle](http://gradle.org/).
Infrastructure as a code is managed by [Terraform](https://www.terraform.io/).
CI/CD is done using [Github Actions](https://github.com/features/actions).

## Development

**Clone the repo**

```sh
➜ git clone https://github.com/Meemaw/insight
```

### Frontend

All code related to frontend is located inside [./frontend](./frontend) folder.

#### Local development

**Boostrap projects**

```sh
➜ npx lerna bootstrap
```

**Run unit tests**

```sh
➜ yarn test
```

### Backend

All code related to backend is located inside [./backend](./backend) folder.

### Infrastructure

All code related to infrastructure is located inside [./infrastructure](./infrastructure) folder.
