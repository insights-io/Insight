import type { Options } from 'ky';

export type RequestOptions = Options & {
  baseURL?: string;
};

export type ClientConfig =
  | string
  | {
      authApiBaseURL: string;
      sessionApiBaseURL: string;
      billingApiBaseURL: string;
    };
