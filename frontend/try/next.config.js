/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

const {
  APP_BASE_URL = 'http://localhost:3000',
  HELP_BASE_URL = 'http://localhost:3003',
  AUTH_API_BASE_URL,
} = process.env;

module.exports = withBundleAnalyzer({
  env: { APP_BASE_URL, HELP_BASE_URL, AUTH_API_BASE_URL },
  webpack: (config, _config) => config,
});
