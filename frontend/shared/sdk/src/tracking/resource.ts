import type { RequestOptions, Input } from 'types';

import { textResponse, HttpClient } from '../http';

export const trackingResource = (client: HttpClient) => {
  return {
    retrieveBoostrapScript: (url: Input, options?: RequestOptions) => {
      return textResponse(client.get(url, options));
    },
  };
};
