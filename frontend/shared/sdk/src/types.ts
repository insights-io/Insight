import type { Options } from 'ky';

export type InsightRequestOptions = Options & {
  baseURL?: string;
};
