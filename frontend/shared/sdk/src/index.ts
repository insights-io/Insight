/* eslint-disable lodash/prefer-lodash-typecheck */
import { createInsightAuthClient } from './auth';
import { InsightRequestOptions } from './types';

type CreateClientConfig =
  | string
  | { authApiBaseURL: string; sessionApiBaseURL: string };

const createInsightClient = (clientConfig: CreateClientConfig) => {
  let authApiBaseURL: string;
  if (typeof clientConfig === 'string') {
    authApiBaseURL = clientConfig;
  } else {
    authApiBaseURL = clientConfig.authApiBaseURL;
  }

  const auth = createInsightAuthClient(authApiBaseURL);

  return { auth };
};

export { createInsightClient, createInsightAuthClient, InsightRequestOptions };
