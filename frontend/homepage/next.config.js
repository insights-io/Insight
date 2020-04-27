/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

module.exports = withBundleAnalyzer({
  env: {
    APP_BASE_URL: process.env.APP_BASE_URL || 'http://localhost:3000',
    TRY_BASE_URL: process.env.APP_BASE_URL || 'http://localhost:3002',
  },
  webpack: (config, _config) => {
    return config;
  },
});
