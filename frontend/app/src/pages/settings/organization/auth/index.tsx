import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsAuthPage } from 'settings/pages/organization/OrganizationSettingsAuthPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import type { SsoSetupDTO } from '@rebrowse/types';
import { client } from 'sdk';

type Props = AuthenticatedServerSideProps & {
  maybeSsoSetup?: SsoSetupDTO;
};

export const OrganizationSettingsAuth = ({
  maybeSsoSetup,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsAuthPage
      maybeSsoSetup={maybeSsoSetup}
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

    const maybeSsoSetup = await client.auth.sso.setups
      .retrieve({
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
      })
      .then((httpResponse) => httpResponse.data)
      .catch((error) => {
        if ((error.response as Response).status === 404) {
          return undefined;
        }
        throw error;
      });

    return {
      props: maybeSsoSetup
        ? {
            user: authResponse.user,
            organization: authResponse.organization,
            maybeSsoSetup,
          }
        : { user: authResponse.user, organization: authResponse.organization },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsAuth;
