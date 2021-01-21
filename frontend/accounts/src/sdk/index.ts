import {
  createHttpClient,
  signupResource as createSignupResource,
  trackingResource as createTrackingResource,
  passwordResource as createPasswordResource,
} from '@rebrowse/sdk';

export const authApiBaseUrl = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const httpClient = createHttpClient();

export const client = {
  tracking: createTrackingResource(httpClient),
  signup: createSignupResource(httpClient, authApiBaseUrl),
  password: createPasswordResource(httpClient, authApiBaseUrl),
};
