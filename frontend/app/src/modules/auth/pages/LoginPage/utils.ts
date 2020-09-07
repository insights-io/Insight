import { authApiBaseURL } from 'api';

type OAuth2Integration = 'google' | 'github' | 'microsoft';

export const createOAuth2IntegrationHrefBuilder = (encodedRedirect: string) => (
  integration: OAuth2Integration
) => {
  return `${authApiBaseURL}/v1/sso/oauth2/${integration}/signin?redirect=${encodedRedirect}`;
};

export const samlIntegrationHrefBuilder = (
  encodedRedirect: string,
  encodedEmail: string
) => {
  return `${authApiBaseURL}/v1/sso/saml/signin?redirect=${encodedRedirect}&email=${encodedEmail}`;
};
