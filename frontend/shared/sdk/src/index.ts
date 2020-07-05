/* eslint-disable lodash/prefer-lodash-typecheck */
import { createAuthClient } from './auth';
import { RequestOptions } from './types';

type ClientConfig =
  | string
  | { authApiBaseURL: string; sessionApiBaseURL: string };

const createClient = (clientConfig: ClientConfig) => {
  let authApiBaseURL: string;
  if (typeof clientConfig === 'string') {
    authApiBaseURL = clientConfig;
  } else {
    authApiBaseURL = clientConfig.authApiBaseURL;
  }

  const auth = createAuthClient(authApiBaseURL);

  return { auth };
};

export { createClient, createAuthClient, RequestOptions };
