# Accounts

[![storybook](https://raw.githubusercontent.com/storybookjs/brand/master/badge/badge-storybook.svg)](https://accounts.storybook.rebrowse.dev/index.html)

Rebrowse accounts application.

## Deployments

Staging: https://accounts.rebrowse.dev/

## Storybook

You can find up to date Storybook [here](https://accounts.storybook.rebrowse.dev/index.html).

## Testing with locally build tracking script

To test app with locally, locally built tracking script has to be served somewhere. There is a pre-prepared command that can do this for you:

```sh
yarn workspace @rebrowse/browser-tracking serve
```

If you also want to use the locally build bootstrap script, you can do the following:

```sh
yarn workspace @rebrowse/browser-bootstrap serve
BOOTSTRAP_SCRIPT=http://localhost:5001/localhost.rebrowse.js yarn dev
```

## Environment

Environment variables are managed using `.env` files.
Please refer to [documentation](https://nextjs.org/docs/basic-features/environment-variables) for more information.

### Production

In production `.env.production` is mounted as a volume with environment properly configured.
