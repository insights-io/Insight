# try

## Testing with locally build tracking script

```sh
BOOTSTRAP_SCRIPT="file://$(pwd)/../browser/bootstrap/dist/local.insight.js" yarn dev
```

This will inject the locally built bootstrap script which will then load the locally built tracking script. For this to work, tracking script has to be served on `http://localhost:5000`. You can achieve this by running `yarn serve` in `frontend/browser/tracking` directory.
