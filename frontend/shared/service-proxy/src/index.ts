import querystring from 'querystring';
import url from 'url';

import httpProxy from 'http-proxy';
import type { NextApiRequest, NextApiResponse } from 'next';

import { getEnvOverrides, getApiProxy } from './config';

export type ProxyConfiguration = Record<string, string | undefined>;

const { createProxy } = httpProxy;

let proxy: ReturnType<typeof createProxy>;

export const nextProxy = (req: NextApiRequest, res: NextApiResponse) => {
  return new Promise((resolve) => {
    const { slug, ...queryParams } = req.query;
    const [api, ...path] = slug as string[];
    const proxiedApiBaseUrl = getApiProxy(api);

    let proxiedPath = `/${path.join('/')}`;
    if (Object.keys(queryParams).length > 0) {
      proxiedPath = `${proxiedPath}?${querystring.stringify(queryParams)}`;
    }
    const proxiedUrl = `${proxiedApiBaseUrl}${proxiedPath}`;

    const options = url.parse(proxiedUrl);
    const host = options.host as string;
    const protocol = options.protocol as string;
    const originalHost = req.headers.host as string;

    proxy.web(req, res, {
      target: proxiedUrl,
      headers: {
        'X-Forwarded-Host': host,
        'X-Forwarded-Proto': protocol.substring(0, protocol.length - 1),
      },
      ignorePath: true,
      secure: false,
      changeOrigin: true,
      cookieDomainRewrite: originalHost.includes('localhost')
        ? ''
        : originalHost,
    });

    res.on('finish', resolve);
  });
};

type WithServiceProxyConfiguration = {
  enabled: boolean;
};

type NextConfig = {
  env: Record<string, string>;
};

const withServiceProxy = ({ enabled }: WithServiceProxyConfiguration) => {
  return (config: NextConfig) => {
    if (!enabled) {
      return config;
    }

    proxy = createProxy();

    return { ...config, env: getEnvOverrides() };
  };
};

export default withServiceProxy;
