import type { ClientConfig } from 'types';

import { createBillingClient } from './billing';
import { createAuthClient } from './auth';
import { createSessionsClient } from './sessions';

export const createClient = (clientConfig: ClientConfig) => {
  let authApiBaseURL: string;
  let sessionApiBaseURL: string;
  let billingApiBaseURL: string;

  // eslint-disable-next-line lodash/prefer-lodash-typecheck
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
export * from './types';
export * from './tracking';
export * from './billing';
