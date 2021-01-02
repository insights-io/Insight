import type { ApiEndpointsConfig, ApiEndpoints, RequestOptions } from 'types';
import { subscriptionResource, invoicesResource } from 'billing';
import { eventsResource, pageVisitResource, sessionsResource } from 'sessions';
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
} from 'auth';
import { trackingResource } from 'tracking';

import { createHttpClient } from './client';

const getApiEndpoints = (
  apiEndpointsConfig: ApiEndpointsConfig
): ApiEndpoints => {
  // eslint-disable-next-line lodash/prefer-lodash-typecheck
  if (typeof apiEndpointsConfig !== 'string') {
    return apiEndpointsConfig;
  }

  return {
    authApiBaseUrl: apiEndpointsConfig,
    sessionApiBaseUrl: apiEndpointsConfig,
    billingApiBaseUrl: apiEndpointsConfig,
  };
};

export const Rebrowse = {
  VERSION: 1,
} as const;

export const createRebrowseHttpClient = (
  apiEndpointsConfig: ApiEndpointsConfig,
  { headers, ...defaultOptions }: RequestOptions = {}
) => {
  const client = createHttpClient({
    headers: {
      'User-Agent': `Rebrowse/v1 JavascriptBinding/${Rebrowse.VERSION}`,
      ...headers,
    },
    ...defaultOptions,
  });

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

export const createRebrowseBrowserClient = (
  apiEndpointsConfig: ApiEndpointsConfig,
  requestOptions: Omit<RequestOptions, 'credentials'> = {}
) => {
  return createRebrowseHttpClient(apiEndpointsConfig, {
    credentials: 'include',
    ...requestOptions,
  });
};
