/* eslint-disable lodash/prefer-lodash-typecheck */
import { createSessionsClient, mapSession } from './sessions';
import {
  createAuthClient,
  mapOrganization,
  mapTeamInvite,
  mapUser,
} from './auth';
import { RequestOptions } from './types';

type ClientConfig =
  | string
  | { authApiBaseURL: string; sessionApiBaseURL: string };

const createClient = (clientConfig: ClientConfig) => {
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

export {
  createClient,
  createAuthClient,
  createSessionsClient,
  RequestOptions,
  mapSession,
  mapOrganization,
  mapTeamInvite,
  mapUser,
};
