import type { DataResponse } from '@rebrowse/types';
import type {
  Input as KyInput,
  Options as KyOptions,
  ResponsePromise as KyResponsePromise,
} from 'ky';

export type { HttpClient } from '../http';

export type Input = KyInput;
export type RequestOptions = KyOptions;
export type ResponsePromise = KyResponsePromise;

export type ExtendedRequestOptions = RequestOptions & {
  baseUrl?: string;
};

export type HttpResponseBase = {
  statusCode: number;
  headers: Headers;
};

export type HttpResponse<TObject> = DataResponse<TObject> & HttpResponseBase;

export type ApiEndpoints = {
  authApiBaseUrl: string;
  sessionApiBaseUrl: string;
  billingApiBaseUrl: string;
};

export type ApiEndpointsConfig = string | ApiEndpoints;
