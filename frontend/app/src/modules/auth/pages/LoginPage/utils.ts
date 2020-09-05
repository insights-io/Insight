import { authApiBaseURL } from 'api';

type OAuthIntegration = 'google' | 'github' | 'microsoft';

export const createOAuthIntegrationHrefBuilder = (
  encodedDestination: string
) => (integration: OAuthIntegration) => {
  return `${authApiBaseURL}/v1/sso/${integration}/signin?dest=${encodedDestination}`;
};
