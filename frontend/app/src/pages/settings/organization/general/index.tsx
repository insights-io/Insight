import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsGeneralPage } from 'modules/settings/pages/organization/OrganizationSettingsGeneralPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import type { OrganizationDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
};

export const OrganizationSettingsGeneral = ({ organization }: Props) => {
  return <OrganizationSettingsGeneralPage organization={organization} />;
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

    const organization = await AuthApi.organization.get({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    return { props: { user: authResponse.user, organization } };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsGeneral;
