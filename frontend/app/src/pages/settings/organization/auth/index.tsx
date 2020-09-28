import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsAuthPage } from 'modules/settings/pages/organization/OrganizationSettingsAuthPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import type { SsoSetupDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  maybeSsoSetup?: SsoSetupDTO;
};

export const OrganizationSettingsAuth = ({ maybeSsoSetup }: Props) => {
  return <OrganizationSettingsAuthPage maybeSsoSetup={maybeSsoSetup} />;
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

    const maybeSsoSetup = await AuthApi.sso.setup
      .get({
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
      })
      .catch((error) => {
        if ((error.response as Response).status === 404) {
          return undefined;
        }
        throw error;
      });

    return {
      props: maybeSsoSetup
        ? { user: authResponse.user, maybeSsoSetup }
        : { user: authResponse.user },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsAuth;
