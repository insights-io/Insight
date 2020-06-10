/* eslint-disable no-console */
import http from 'http';
import https from 'https';
import url from 'url';
import querystring from 'querystring';

import { NextApiRequest, NextApiResponse } from 'next';

const PROXIED_DOMAIN = process.env.PROXIED_DOMAIN || 'localhost';
const IS_PROXIED_LOCALHOST = PROXIED_DOMAIN === 'localhost';

const LOCALHOST_SERVICE_MAPPINGS = {
  auth: 8080,
} as const;

export default (
  originalRequest: NextApiRequest,
  originalResponse: NextApiResponse
) => {
  const { slug, ...rest } = originalRequest.query;
  const [service, ...path] = slug as string[];
  let proxiedPath = `/${path.join('/')}`;
  if (Object.keys(rest).length > 0) {
    proxiedPath = `${proxiedPath}?${querystring.stringify(rest)}`;
  }

  const proxiedApi = `${service}-api`;
  const proxiedURL = IS_PROXIED_LOCALHOST
    ? `http://localhost:${
        LOCALHOST_SERVICE_MAPPINGS[
          service as keyof typeof LOCALHOST_SERVICE_MAPPINGS
        ]
      }${proxiedPath}`
    : `https://${proxiedApi}.${PROXIED_DOMAIN}${proxiedPath}`;

  const options = url.parse(proxiedURL);
  const httpModule = options.protocol === 'https:' ? https : http;

  originalRequest.pause();
  console.log(`<== Proxying /api/${service}${proxiedPath} to ${proxiedURL}`);

  const protocol = options.protocol as string;
  const headers = {
    ...originalRequest.headers,
    'X-Forwarded-Host': options.host as string,
    'X-Forwarded-Proto': protocol.substring(0, protocol.length - 1),
  };

  const proxiedRequest = httpModule.request({
    ...options,
    headers,
    method: originalRequest.method,
  });

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
    const statusCode = proxiedResponse.statusCode as number;

    console.log(
      '<== Received response for proxied request',
      statusCode,
      proxiedURL
    );

    proxiedResponse.pause();
    originalResponse.writeHead(statusCode, proxiedResponse.headers);
    proxiedResponse.pipe(originalResponse, { end: true });
    proxiedResponse.resume();
  });

  proxiedRequest.on('error', (error) => {
    console.error('Something went wrong', error);
  });

  originalRequest.pipe(proxiedRequest, { end: true });
  originalRequest.resume();
};
