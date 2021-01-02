import { apiEndpoints } from 'sdk';

type OAuth2Integration = 'google' | 'github' | 'microsoft';

type OAuth2HrefBuilderParams = {
  absoluteRedirect: string;
};

export const createOAuth2IntegrationHrefBuilder = ({
  absoluteRedirect,
}: OAuth2HrefBuilderParams) => (integration: OAuth2Integration) => {
  return `${
    apiEndpoints.authApiBaseUrl
  }/v1/sso/oauth2/${integration}/signin?redirect=${encodeURIComponent(
    absoluteRedirect
  )}`;
};

type SsoIntegrationHrefBuilderParams = OAuth2HrefBuilderParams & {
  ssoSignInURI: string;
  email: string;
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
