/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

const {
  AUTH_API_BASE_URL = 'http://localhost:8080',
  NEXT_PUBLIC_AUTH_API_BASE_URL = 'http://localhost:8080',
  NEXT_PUBLIC_APP_BASE_URL = 'http://localhost:3000',
  NEXT_PUBLIC_TRY_BASE_URL = 'http://localhost:3002',
} = process.env;

const env = {
  AUTH_API_BASE_URL,
  NEXT_PUBLIC_AUTH_API_BASE_URL,
  NEXT_PUBLIC_APP_BASE_URL,
  NEXT_PUBLIC_TRY_BASE_URL,
};

const nextConfig = withBundleAnalyzer({
  env,
  webpack: (config, _config) => {
    return config;
  },
});

// eslint-disable-next-line no-console
console.log('Next config', nextConfig);

module.exports = nextConfig;
