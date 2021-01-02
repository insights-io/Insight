import ky from 'ky';
import type { RequestOptions, Input } from 'types';

export const createHttpClient = (defaultOptions: RequestOptions = {}) => {
  const client = ky.extend(defaultOptions);

  return {
    head: (url: Input, options?: RequestOptions) => {
      return client.head(url, options);
    },
    get: (url: Input, options?: RequestOptions) => {
      return client.get(url, options);
    },
    post: (url: Input, options?: RequestOptions) => {
      return client.post(url, options);
    },
    put: (url: Input, options?: RequestOptions) => {
      return client.put(url, options);
    },
    patch: (url: Input, options?: RequestOptions) => {
      return client.patch(url, options);
    },
    delete: (url: Input, options?: RequestOptions) => {
      return client.delete(url, options);
    },
  };
};

export type HttpClient = ReturnType<typeof createHttpClient>;
