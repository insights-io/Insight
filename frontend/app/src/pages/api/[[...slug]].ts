import { NextApiRequest, NextApiResponse } from 'next';
import { nextProxy } from '@insight/service-proxy';

export default (
  originalRequest: NextApiRequest,
  originalResponse: NextApiResponse
) => {
  return nextProxy(originalRequest, originalResponse);
};
