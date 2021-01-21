import { authApiBaseUrl } from 'sdk';

type OAuthIntegration = 'google' | 'github' | 'microsoft';

type OAuthBuilderParams = {
  absoluteRedirect: string;
};

export const createOAuth2IntegrationHrefBuilder = ({
  absoluteRedirect,
}: OAuthBuilderParams) => (integration: OAuthIntegration) => {
  return `${authApiBaseUrl}/v1/sso/oauth2/${integration}/signin?redirect=${encodeURIComponent(
    absoluteRedirect
  )}`;
};
