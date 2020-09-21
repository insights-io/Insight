/* eslint-disable no-param-reassign */
/* eslint-disable @typescript-eslint/no-var-requires */
const withPWA = require('next-pwa');
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});
const withServiceProxy = require('@insight/service-proxy').default({
  enabled: (process.env.PROXY || 'false').toLowerCase() === 'true',
});

const {
  NEXT_PUBLIC_TRY_BASE_URL = 'http://localhost:3002',
  AUTH_API_BASE_URL = 'http://localhost:8080',
  NEXT_PUBLIC_AUTH_API_BASE_URL = 'http://localhost:8080',
  SESSION_API_BASE_URL = 'http://localhost:8082',
  NEXT_PUBLIC_SESSION_API_BASE_URL = 'http://localhost:8082',
  NEXT_PUBLIC_BILLING_API_BASE_URL = 'http://localhost:8083',
  BOOTSTRAP_SCRIPT = 'https://static.dev.snuderls.eu/b/local.insight.js',
  JAEGER_AGENT_HOST = 'localhost',
  JAEGER_AGENT_PORT = '6832',
} = process.env;

const env = {
  NEXT_PUBLIC_TRY_BASE_URL,
  AUTH_API_BASE_URL,
  NEXT_PUBLIC_AUTH_API_BASE_URL,
  SESSION_API_BASE_URL,
  NEXT_PUBLIC_SESSION_API_BASE_URL,
  NEXT_PUBLIC_BILLING_API_BASE_URL,
  BOOTSTRAP_SCRIPT,
  JAEGER_AGENT_HOST,
  JAEGER_AGENT_PORT,
};

const nextConfig = {
  env,
  webpack: (config, _config) => config,
  pwa: {
    dest: 'public',
    disable: true,
  },
};

// eslint-disable-next-line no-console
console.log('Next config', nextConfig);

module.exports = withServiceProxy(withBundleAnalyzer(withPWA(nextConfig)));
