import { authApiBaseUrl } from 'sdk';
import { REDIRECT_QUERY } from 'shared/constants/routes';

type OAuthIntegration = 'google' | 'github' | 'microsoft';

type OAuthBuilderParams = {
  redirect: string;
};

export const createOAuth2IntegrationHrefBuilder = ({
  redirect,
}: OAuthBuilderParams) => (integration: OAuthIntegration) => {
  return `${authApiBaseUrl}/v1/sso/oauth2/${integration}/signin?${REDIRECT_QUERY}=${encodeURIComponent(
    redirect
  )}`;
};
