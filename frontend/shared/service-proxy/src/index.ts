/* eslint-disable no-param-reassign */
/* eslint-disable no-console */
import url from 'url';
import http, { IncomingMessage, ServerResponse } from 'http';
import https from 'https';
import querystring from 'querystring';

import { NextApiRequest, NextApiResponse } from 'next';

import { getEnvOverrides, getApiProxy } from './config';

type ServerRequest = IncomingMessage & {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  body: any;
};

export type ProxyConfiguration = Record<string, string | undefined>;

export const proxyCookies = (
  cookies: string[],
  originalRequestHost: string | undefined
) => {
  return cookies.map((cookie) =>
    cookie
      .split(';')
      .map((part) => {
        const [key] = part.split('=');
        if (key === 'Domain') {
          const originalHost = (originalRequestHost || '').includes('localhost')
            ? ''
            : originalRequestHost;
          return [key, originalHost].join('=');
        }
        return part;
      })
      .join(';')
  );
};

export const proxy = (
  originalRequest: ServerRequest,
  originalResponse: ServerResponse,
  proxiedURL: string
) => {
  return new Promise((resolve) => {
    const options = url.parse(proxiedURL);
    const httpModule = options.protocol === 'https:' ? https : http;
    const host = options.host as string;

    originalRequest.pause();

    const protocol = options.protocol as string;
    const headers = {
      ...originalRequest.headers,
      'X-Forwarded-Host': host,
      'X-Forwarded-Proto': protocol.substring(0, protocol.length - 1),
      host,
    };

    const proxiedRequest = httpModule.request({
      ...options,
      headers,
      method: originalRequest.method,
      rejectUnauthorized: false, // TODO: revisit
      agent: false,
    });

    // TODO: do we really need to manually write?
    const contentType = originalRequest.headers['content-type'];
    if (contentType === 'application/x-www-form-urlencoded;charset=UTF-8') {
      const body = querystring.stringify(originalRequest.body);
      proxiedRequest.setHeader('Content-Length', Buffer.byteLength(body));
      proxiedRequest.write(body);
    } else if (contentType === 'application/json') {
      const body = JSON.stringify(originalRequest.body);
      proxiedRequest.setHeader('Content-Length', Buffer.byteLength(body));
      proxiedRequest.write(body);
    }

    proxiedRequest.on('response', (proxiedResponse) => {
      proxiedResponse.headers['access-control-allow-origin'] = '*';
      const statusCode = proxiedResponse.statusCode as number;

      const cookies = proxiedResponse.headers['set-cookie'];
      if (cookies) {
        const proxiedCookies = proxyCookies(
          cookies,
          originalRequest.headers.host
        );
        proxiedResponse.headers['set-cookie'] = proxiedCookies;
      }

      console.log(
        '<== Received response for proxied request',
        statusCode,
        proxiedURL
      );

      proxiedResponse.pause();
      originalResponse.writeHead(statusCode, proxiedResponse.headers);
      originalResponse.on('finish', resolve);
      proxiedResponse.pipe(originalResponse, { end: true });
      proxiedResponse.resume();
    });

    originalRequest.pipe(proxiedRequest, { end: true });
    originalRequest.resume();
  });
};

export const nextProxy = (
  originalRequest: NextApiRequest,
  originalResponse: NextApiResponse
) => {
  const { slug, ...queryParams } = originalRequest.query;
  const [api, ...path] = slug as string[];
  const proxiedApiBaseUrl = getApiProxy(api);

  let proxiedPath = `/${path.join('/')}`;
  if (Object.keys(queryParams).length > 0) {
    proxiedPath = `${proxiedPath}?${querystring.stringify(queryParams)}`;
  }
  const proxiedURL = `${proxiedApiBaseUrl}${proxiedPath}`;
  console.log(`<== Proxying /api/${api}${proxiedPath} to ${proxiedURL}`);
  return proxy(originalRequest, originalResponse, proxiedURL);
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

    return { ...config, env: getEnvOverrides() };
  };
};

export default withServiceProxy;
