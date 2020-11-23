/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});
const withServiceProxy = require('@rebrowse/service-proxy').default({
  enabled: (process.env.PROXY || 'false').toLowerCase() === 'true',
});

module.exports = withServiceProxy(withBundleAnalyzer({}));
