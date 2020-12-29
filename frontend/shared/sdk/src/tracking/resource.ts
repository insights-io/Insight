import ky from 'ky-universal';

import { textResponse } from '../http';
import type { RequestOptions } from '../types';

export const getBoostrapScript = (url: string, options?: RequestOptions) => {
  return textResponse(ky.get(url, options));
};
