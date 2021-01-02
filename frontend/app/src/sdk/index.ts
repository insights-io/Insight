import {
  createRebrowseHttpClient,
  ApiEndpoints,
  RequestOptions,
} from '@rebrowse/sdk';

export const apiEndpoints: ApiEndpoints = {
  authApiBaseUrl: (process.env.AUTH_API_BASE_URL ||
    process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
    process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string,
  sessionApiBaseUrl: (process.env.SESSION_API_BASE_URL ||
    process.env.NEXT_PUBLIC_SESSION_API_BASE_URL ||
    process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string,
  billingApiBaseUrl: (process.env.BILLING_API_BASE_URL ||
    process.env.NEXT_PUBLIC_BILLING_API_BASE_URL ||
    process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string,
};

export const client = createRebrowseHttpClient(apiEndpoints);

export const INCLUDE_CREDENTIALS: Pick<RequestOptions, 'credentials'> = {
  credentials: 'include',
};
