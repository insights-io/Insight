import ky from 'ky-universal';

import { RequestOptions } from '../core';

export const getBoostrapScript = (url: string, options?: RequestOptions) => {
  return ky.get(url, options).text();
};
