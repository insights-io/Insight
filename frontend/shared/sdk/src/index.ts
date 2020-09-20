/* eslint-disable lodash/prefer-lodash-typecheck */
import { createAuthClient } from './auth';
import { createSessionsClient } from './sessions';

export type ClientConfig =
  | string
  | { authApiBaseURL: string; sessionApiBaseURL: string };

export const createClient = (clientConfig: ClientConfig) => {
  let authApiBaseURL: string;
  let sessionApiBaseURL: string;
  if (typeof clientConfig === 'string') {
    authApiBaseURL = clientConfig;
    sessionApiBaseURL = clientConfig;
  } else {
    authApiBaseURL = clientConfig.authApiBaseURL;
    sessionApiBaseURL = clientConfig.sessionApiBaseURL;
  }

  const auth = createAuthClient(authApiBaseURL);
  const sessions = createSessionsClient(sessionApiBaseURL);

  return { auth, sessions };
};

export * from './auth';
export * from './sessions';
export * from './core';
export * from './tracking';
