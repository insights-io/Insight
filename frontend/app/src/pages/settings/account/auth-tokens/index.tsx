import React from 'react';
import { AccountSettingsAuthTokensPage } from 'modules/settings/pages/account/AccountSettingsAuthTokensPage';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import type { AuthTokenDTO } from '@insight/types';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';

type Props = AuthenticatedServerSideProps & {
  authTokens: AuthTokenDTO[];
};

export const AccountSettingsAuthTokens = ({ authTokens }: Props) => {
  return <AccountSettingsAuthTokensPage authTokens={authTokens} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    const authTokens = await AuthApi.sso.token.list({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    return { props: { user: authResponse.user, authTokens } };
  } finally {
    requestSpan.finish();
  }
};
export default AccountSettingsAuthTokens;
