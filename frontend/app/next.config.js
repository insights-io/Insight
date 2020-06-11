/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});
const withServiceProxy = require('@insight/service-proxy').default({
  enabled: (process.env.PROXY || 'false').toLowerCase() === 'true',
  proxies: {
    NEXT_PUBLIC_AUTH_API_BASE_URL: '/api/auth',
    NEXT_PUBLIC_SESSION_API_BASE_URL: '/api/session',
  },
});

const {
  TRY_BASE_URL = 'http://localhost:3002',
  AUTH_API_BASE_URL = 'http://localhost:8080',
  NEXT_PUBLIC_AUTH_API_BASE_URL = 'http://localhost:8080',
  SESSION_API_BASE_URL = 'http://localhost:8082',
  NEXT_PUBLIC_SESSION_API_BASE_URL = 'http://localhost:8082',
} = process.env;

module.exports = withServiceProxy(
  withBundleAnalyzer({
    env: {
      TRY_BASE_URL,
      AUTH_API_BASE_URL,
      NEXT_PUBLIC_AUTH_API_BASE_URL,
      SESSION_API_BASE_URL,
      NEXT_PUBLIC_SESSION_API_BASE_URL,
    },
    webpack: (config, _config) => config,
  })
);
