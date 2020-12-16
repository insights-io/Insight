import ky from 'ky-universal';

import type { RequestOptions } from '../types';

export const getBoostrapScript = (url: string, options?: RequestOptions) => {
  return ky.get(url, options).text();
};
