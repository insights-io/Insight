import type { NextApiRequest, NextApiResponse } from 'next';
import { nextProxy } from '@rebrowse/service-proxy';

export default async (req: NextApiRequest, res: NextApiResponse) => {
  return nextProxy(req, res);
};

export const config = {
  api: {
    bodyParser: false,
  },
};
