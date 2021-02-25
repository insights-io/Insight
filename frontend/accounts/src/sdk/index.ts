import {
  createHttpClient,
  signupResource as createSignupResource,
  trackingResource as createTrackingResource,
  passwordResource as createPasswordResource,
  accountsResource as createAccountsResource,
  mfaSetupResource as createMfaSetupResource,
  userResource as createUserResource,
  RequestOptions,
} from '@rebrowse/sdk';

export const authApiBaseUrl = (process.env.NEXT_PUBLIC_AUTH_API_BASE_URL ||
  process.env.NEXT_PUBLIC_REBROWSE_API_BASE_URL) as string;

export const httpClient = createHttpClient();

export const client = {
  users: createUserResource(httpClient, authApiBaseUrl),
  tracking: createTrackingResource(httpClient),
  signup: createSignupResource(httpClient, authApiBaseUrl),
  password: createPasswordResource(httpClient, authApiBaseUrl),
  accounts: createAccountsResource(httpClient, authApiBaseUrl),
  mfa: {
    setup: createMfaSetupResource(httpClient, authApiBaseUrl),
  },
};

export const INCLUDE_CREDENTIALS: Pick<RequestOptions, 'credentials'> = {
  credentials: 'include',
};
