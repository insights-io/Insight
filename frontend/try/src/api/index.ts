import {
  createHttpClient,
  signupResource as createSignupResource,
  trackingResource as createTrackingResource,
} from '@rebrowse/sdk';

export const authApiBaseURL = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const httpClient = createHttpClient();

export const sdk = {
  tracking: createTrackingResource(httpClient),
  signup: createSignupResource(httpClient, authApiBaseURL),
};
