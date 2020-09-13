import { authApiBaseURL } from 'api';

type OAuth2Integration = 'google' | 'github' | 'microsoft';

export const createOAuth2IntegrationHrefBuilder = (
  absoluteRedirect: string
) => (integration: OAuth2Integration) => {
  return `${authApiBaseURL}/v1/sso/oauth2/${integration}/signin?redirect=${encodeURIComponent(
    absoluteRedirect
  )}`;
};

type SsoIntegrationHrefBuilderParams = {
  ssoSignInURI: string;
  email: string;
  absoluteRedirect: string;
};

export const ssoIntegrationHrefBuilder = ({
  ssoSignInURI,
  email,
  absoluteRedirect,
}: SsoIntegrationHrefBuilderParams) => {
  return `${ssoSignInURI}?redirect=${encodeURIComponent(
    absoluteRedirect
  )}&email=${encodeURIComponent(email)}`;
};
