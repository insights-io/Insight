import { authApiBaseURL } from 'api';

type OAuth2Integration = 'google' | 'github' | 'microsoft';

export const createOAuth2IntegrationHrefBuilder = (
  encodedDestination: string
) => (integration: OAuth2Integration) => {
  return `${authApiBaseURL}/v1/sso/oauth2/${integration}/signin?dest=${encodedDestination}`;
};

export const samlIntegrationHrefBuilder = (encodedDestination: string) => {
  return `${authApiBaseURL}/v1/sso/saml/signin?dest=${encodedDestination}`;
};
