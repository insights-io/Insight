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

export default (req: NextApiRequest, res: NextApiResponse) => {
  const { slug, ...rest } = req.query;
  const [service, ...path] = slug as string[];
  const proxiedPath = `${path.join('/')}?${querystring.stringify(rest)}`;
  const proxiedApi = `${service}-api`;
  const proxiedURL = IS_PROXIED_LOCALHOST
    ? `http://localhost:${
        LOCALHOST_SERVICE_MAPPINGS[
          service as keyof typeof LOCALHOST_SERVICE_MAPPINGS
        ]
      }/${proxiedPath}`
    : `https://${proxiedApi}.${PROXIED_DOMAIN}/${proxiedPath}`;

  const options = url.parse(proxiedURL);
  const module = options.protocol === 'https:' ? https : http;

  console.log(`<== Proxying ${slug} to ${proxiedURL}`);
  req.pause();

  const protocol = options.protocol as string;
  const headers = {
    ...req.headers,
    'X-Forwarded-Host': options.host as string,
    'X-Forwarded-Proto': protocol.substring(0, protocol.length - 1),
  };

  const connector = module.request(
    { ...options, headers, method: req.method },
    (proxiedResponse) => {
      const statusCode = proxiedResponse.statusCode as number;

      console.log(
        '<== Received res for proxied request',
        statusCode,
        proxiedURL
      );

      proxiedResponse.pause();
      res.writeHead(statusCode, proxiedResponse.headers);
      proxiedResponse.pipe(res, { end: true });
      proxiedResponse.resume();
    }
  );

  if (
    headers['content-type'] ===
    'application/x-www-form-urlencoded;charset=UTF-8'
  ) {
    connector.write(querystring.stringify(req.body));
  }

  req.pipe(connector, { end: true });
  req.resume();
};
