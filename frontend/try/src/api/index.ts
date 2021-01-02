import {
  createHttpClient,
  signupResource as createSignupResource,
} from '@rebrowse/sdk';

export const authApiBaseURL = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const sdk = createSignupResource(createHttpClient(), authApiBaseURL);
