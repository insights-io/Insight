/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

module.exports = withBundleAnalyzer({
  env: {
    APP_BASE_URL: process.env.APP_BASE_URL,
    HELP_BASE_URL: process.env.HELP_BASE_URL,
    AUTH_API_BASE_URL: process.env.AUTH_API_BASE_URL,
  },
  webpack: (config, _config) => {
    return config;
  },
});
