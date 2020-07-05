import type { Options } from 'ky';

export type RequestOptions = Options & {
  baseURL?: string;
};
