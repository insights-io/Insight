import React from 'react';
import { AccountSettingsSecurityPage } from 'modules/settings/pages/account/AccountSettingsSecurityPage';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { AuthApi } from 'api';
import type { MfaSetupDTO } from '@rebrowse/types';

type Props = AuthenticatedServerSideProps & {
  mfaSetups: MfaSetupDTO[];
};

export const AccountSettingsSecurity = ({
  user,
  organization,
  mfaSetups,
}: Props) => {
  return (
    <AccountSettingsSecurityPage
      user={user}
      organization={organization}
      mfaSetups={mfaSetups}
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

    const mfaSetups = await AuthApi.mfa.setup.list({
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
        mfaSetups,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default AccountSettingsSecurity;
