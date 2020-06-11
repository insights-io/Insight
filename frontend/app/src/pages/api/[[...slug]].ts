import { NextApiRequest, NextApiResponse } from 'next';
import { nextProxy } from '@insight/service-proxy';

export default (
  originalRequest: NextApiRequest,
  originalResponse: NextApiResponse
) => {
  return nextProxy(originalRequest, originalResponse, {
    auth: process.env.AUTH_API_BASE_URL,
    beacon: process.env.BEACON_API_BASE_URL,
    session: process.env.SESSION_API_BASE_URL,
  });
};
