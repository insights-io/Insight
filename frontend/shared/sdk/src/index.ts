/* eslint-disable lodash/prefer-lodash-typecheck */
import { createBillingClient } from './billing';
import { createAuthClient } from './auth';
import { createSessionsClient } from './sessions';

export type ClientConfig =
  | string
  | {
      authApiBaseURL: string;
      sessionApiBaseURL: string;
      billingApiBaseURL: string;
    };

export const createClient = (clientConfig: ClientConfig) => {
  let authApiBaseURL: string;
  let sessionApiBaseURL: string;
  let billingApiBaseURL: string;
  if (typeof clientConfig === 'string') {
    authApiBaseURL = clientConfig;
    sessionApiBaseURL = clientConfig;
    billingApiBaseURL = clientConfig;
  } else {
    authApiBaseURL = clientConfig.authApiBaseURL;
    sessionApiBaseURL = clientConfig.sessionApiBaseURL;
    billingApiBaseURL = clientConfig.billingApiBaseURL;
  }

  const auth = createAuthClient(authApiBaseURL);
  const sessions = createSessionsClient(sessionApiBaseURL);
  const billing = createBillingClient(billingApiBaseURL);

  return { auth, sessions, billing };
};

export * from './auth';
export * from './sessions';
export * from './core';
export * from './tracking';
export * from './billing';
