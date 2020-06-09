# Insight

[![codecov](https://codecov.io/gh/insights-io/insight/branch/master/graph/badge.svg)](https://codecov.io/gh/insights-io/insight)
![gitleaks](https://github.com/insights-io/Insight/workflows/gitleaks/badge.svg)
![misspell](https://github.com/insights-io/Insight/workflows/misspell/badge.svg)

**Monorepo for Insight related services and applications.**

- [frontend](frontend) related code is managed by [Lerna](https://github.com/lerna/lerna) and [Yarn Workspaces](https://yarnpkg.com/lang/en/docs/workspaces/)
- [backend](backend) related code is managed by [Gradle](http://gradle.org/)
- [infrastructure](infrastructure) as a code is managed by [Terraform](https://www.terraform.io/)
- [CI/CD](.github/workflows) is managed using [Github Actions](https://github.com/features/actions)

## Development

**Clone the repo**

```sh
➜ git clone git@github.com:insights-io/Insight.git
```

### Frontend

It is recommended to use VSCode for frontend development.

#### Local development

**Boostrap projects**

```sh
➜ yarn install --frozen-lockfile
```

**Run unit tests**

```sh
➜ yarn test
```

### Backend

It is recommended to use InteliJ IDEA for backend (Java) development.

#### Local development

Make sure you are in the [backend](backend) folder before executing gradle commands.

**Run unit tests**

```sh
➜ ./gradlew test --continue jacocoTestReport
```
