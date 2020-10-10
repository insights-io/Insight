/* eslint-disable @typescript-eslint/no-var-requires */
const withPWA = require('next-pwa');
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});
const withServiceProxy = require('@insight/service-proxy').default({
  enabled: Boolean(process.env.PROXY),
});

const nextConfig = {
  pwa: {
    dest: 'public',
    disable: true,
  },
};

module.exports = withServiceProxy(withBundleAnalyzer(withPWA(nextConfig)));
