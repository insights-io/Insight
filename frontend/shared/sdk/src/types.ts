import type { Options } from 'ky';

export type RequestOptions = Options & {
  baseURL?: string;
};

export type SearchBean = {
  limit?: number;
  // eslint-disable-next-line camelcase
  sort_by?: string[];
  // eslint-disable-next-line camelcase
  group_by?: string[];
};
