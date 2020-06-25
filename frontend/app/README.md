# app

## Testing with locally build tracking script

To test app with locally, locally built tracking script has to be served somewhere. There is a pre-prepared command that can do this for you:

```sh
yarn workspace @insight/browser-tracking serve
```

If you also want to use the locally build bootstrap script, you can do the following:

```sh
yarn workspace @insight/browser-bootstrap serve
BOOTSTRAP_SCRIPT=http://localhost:5001/local.insight.js yarn dev
```
