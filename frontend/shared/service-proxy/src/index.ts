/* eslint-disable no-param-reassign */
/* eslint-disable no-console */
import url from 'url';
import http, { IncomingMessage, ServerResponse } from 'http';
import https from 'https';
import querystring from 'querystring';

import { NextApiRequest, NextApiResponse } from 'next';

const NEXT_PUBLIC_SERVICE_PATTERN = /^NEXT_PUBLIC_(.*)_API_BASE_URL$/;

const getServiceKey = (service: string) => {
  return `NEXT_PUBLIC_${service.toUpperCase()}_API_BASE_URL`;
};

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

    console.log(proxiedResponse.headers);

    proxiedResponse.pause();
    originalResponse.writeHead(statusCode, proxiedResponse.headers);
    proxiedResponse.pipe(originalResponse, { end: true });
    proxiedResponse.resume();
  });

  originalRequest.pipe(proxiedRequest, { end: true });
  originalRequest.resume();

  return proxiedRequest;
};

export const nextProxy = (
  originalRequest: NextApiRequest,
  originalResponse: NextApiResponse
) => {
  const { slug, ...queryParams } = originalRequest.query;
  const [service, ...path] = slug as string[];

  const serviceBaseURL = process.env[getServiceKey(service)];
  if (!serviceBaseURL) {
    throw new Error(
      `Could not find proxy configuration for ${service} in ${getServiceKey(
        service
      )}`
    );
  }

  let proxiedPath = `/${path.join('/')}`;
  if (Object.keys(queryParams).length > 0) {
    proxiedPath = `${proxiedPath}?${querystring.stringify(queryParams)}`;
  }
  const proxiedURL = `${serviceBaseURL}${proxiedPath}`;
  console.log(`<== Proxying /api/${service}${proxiedPath} to ${proxiedURL}`);
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

    const env = Object.keys(config.env).reduce((acc, key) => {
      const match = key.match(NEXT_PUBLIC_SERVICE_PATTERN);
      if (match) {
        const [_, serviceMatch] = match;
        const proxiedPath = `/api/${serviceMatch.toLowerCase()}`;
        const proxiedServiceBaseURL = config.env[key];
        console.log(
          `Setting up proxy for ${key} => ${proxiedPath} => ${proxiedServiceBaseURL}`
        );

        return { ...acc, [key]: proxiedPath };
      }

      return { ...acc, [key]: config.env[key] };
    }, {});

    return { ...config, env };
  };
};

export default withServiceProxy;
