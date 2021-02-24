import { trackingResource } from '../tracking';
import {
  ssoSetupResource,
  authTokensResource,
  ssoSessionResource,
  userResource,
  signupResource,
  passwordResource,
  organizationsResource,
  mfaChallengeResource,
  mfaSetupResource,
  accountsResource,
} from '../auth';
import type {
  ApiEndpointsConfig,
  ApiEndpoints,
  RequestOptions,
} from '../types';
import { subscriptionResource, invoicesResource } from '../billing';
import {
  eventsResource,
  pageVisitResource,
  sessionsResource,
} from '../sessions';

import { createHttpClient } from './client';

const getApiEndpoints = (
  apiEndpointsConfig: ApiEndpointsConfig
): ApiEndpoints => {
  if (typeof apiEndpointsConfig !== 'string') {
    return apiEndpointsConfig;
  }

  return {
    authApiBaseUrl: apiEndpointsConfig,
    sessionApiBaseUrl: apiEndpointsConfig,
    billingApiBaseUrl: apiEndpointsConfig,
  };
};

export const createRebrowseHttpClient = (
  apiEndpointsConfig: ApiEndpointsConfig,
  defaultOptions: RequestOptions = {}
) => {
  const client = createHttpClient(defaultOptions);

  const {
    sessionApiBaseUrl,
    billingApiBaseUrl,
    authApiBaseUrl,
  } = getApiEndpoints(apiEndpointsConfig);

  const tracking = trackingResource(client);

  /* SessionApi resources */
  const sessions = sessionsResource(client, sessionApiBaseUrl);
  const events = eventsResource(client, sessionApiBaseUrl);
  const pageVisits = pageVisitResource(client, sessionApiBaseUrl);

  /* BillingApi resources */
  const subscriptions = subscriptionResource(client, billingApiBaseUrl);
  const invoices = invoicesResource(client, billingApiBaseUrl);

  /* AuthApi resources */
  const accounts = accountsResource(client, authApiBaseUrl);
  const ssoSetups = ssoSetupResource(client, authApiBaseUrl);
  const ssoSessions = ssoSessionResource(client, authApiBaseUrl);
  const authTokens = authTokensResource(client, authApiBaseUrl);
  const users = userResource(client, authApiBaseUrl);
  const signup = signupResource(client, authApiBaseUrl);
  const password = passwordResource(client, authApiBaseUrl);
  const organizations = organizationsResource(client, authApiBaseUrl);
  const mfaChallenge = mfaChallengeResource(client, authApiBaseUrl);
  const mfaSetup = mfaSetupResource(client, authApiBaseUrl);

  return {
    tracking,
    recording: {
      sessions,
      pageVisits,
      events,
    },
    billing: {
      subscriptions,
      invoices,
    },
    auth: {
      accounts,
      signup,
      password,
      users,
      organizations,
      mfa: {
        challenge: mfaChallenge,
        setup: mfaSetup,
      },
      sso: {
        setups: ssoSetups,
        sessions: ssoSessions,
      },
      tokens: authTokens,
    },
  };
};
