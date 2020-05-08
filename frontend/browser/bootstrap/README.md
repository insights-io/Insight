# @insight/browser-bootstrap"

![.github/workflows/frontend:browser:bootstrap.yml](https://github.com/Meemaw/Insight/workflows/.github/workflows/frontend:browser:bootstrap.yml/badge.svg)

Browser bootstrap script tag that should be included client side to bootstrap Insight client side.
The sole goal of the [script](src/index.js) is to load the [main tracking script](../tracking/README.md) in the background (asynchronously) to not block website's initial load speed.

## Deployment

Bootstrap script is deployed to AWS S3 bucket and exposed through CDN (AWS Cloudfront).
