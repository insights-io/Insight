import type { DataResponse } from '@rebrowse/types';
import type { Options } from 'ky';

export type RequestOptions = Options & {
  baseURL?: string;
};

export type HttpResponseBase = {
  statusCode: number;
  headers: Headers;
};

export type HttpResponse<TObject> = DataResponse<TObject> & HttpResponseBase;

export type ClientConfig =
  | string
  | {
      authApiBaseURL: string;
      sessionApiBaseURL: string;
      billingApiBaseURL: string;
    };
