# Try

[![storybook](https://raw.githubusercontent.com/storybookjs/brand/master/badge/badge-storybook.svg)](https://insights-io.github.io/Insight/try/storybook/master/)

Sign up page for Rebrowse. Deployed on a separate domain: https://try.rebrowse.<>

## Storybook

You can find up to date Storybooks [here](https://try.storybook.rebrowse.dev/index.html).

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
