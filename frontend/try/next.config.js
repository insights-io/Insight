/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});
const withServiceProxy = require('@insight/service-proxy').default({
  enabled: (process.env.PROXY || 'false').toLowerCase() === 'true',
});

const {
  APP_BASE_URL = 'http://localhost:3000',
  HELP_BASE_URL = 'http://localhost:3003',
  NEXT_PUBLIC_AUTH_API_BASE_URL = 'http://localhost:8080',
  BOOTSTRAP_SCRIPT = 'https://d1l87tz7sw1x04.cloudfront.net/b/local.insight.js',
} = process.env;

const env = {
  APP_BASE_URL,
  HELP_BASE_URL,
  NEXT_PUBLIC_AUTH_API_BASE_URL,
  BOOTSTRAP_SCRIPT,
};

console.log('Try environment:', env);

module.exports = withServiceProxy(
  withBundleAnalyzer({
    env,
    webpack: (config, _config) => config,
    experimental: { optionalCatchAll: true },
  })
);
