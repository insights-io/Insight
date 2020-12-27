import React from 'react';
import { AccountSettingsAuthTokensPage } from 'settings/pages/account/AccountSettingsAuthTokensPage';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import type { AuthTokenDTO } from '@rebrowse/types';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { AuthApi } from 'api';

type Props = AuthenticatedServerSideProps & {
  authTokens: AuthTokenDTO[];
};

export const AccountSettingsAuthTokens = ({
  authTokens,
  user,
  organization,
}: Props) => {
  return (
    <AccountSettingsAuthTokensPage
      authTokens={authTokens}
      user={user}
      organization={organization}
    />
  );
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

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        authTokens,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default AccountSettingsAuthTokens;
