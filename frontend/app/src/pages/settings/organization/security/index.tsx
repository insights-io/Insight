import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsSecurityPage } from 'modules/settings/pages/organization/OrganizationSettingsSecurityPage';
import type {
  APIErrorDataResponse,
  OrganizationDTO,
  OrganizationPasswordPolicyDTO,
} from '@insight/types';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
  passwordPolicy?: OrganizationPasswordPolicyDTO;
};

export const OrganizationSettingsSecurityAndPrivacy = ({
  user,
  organization,
  passwordPolicy,
}: Props) => {
  return (
    <OrganizationSettingsSecurityPage
      user={user}
      organization={organization}
      passwordPolicy={passwordPolicy}
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

    const passwordPolicy = await AuthApi.organization.passwordPolicy
      .retrieve({
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        if (errorDTO.error.statusCode === 404) {
          return undefined;
        }
        throw error;
      });

    const props: Props = {
      user: authResponse.user,
      organization: authResponse.organization,
    };
    if (passwordPolicy) {
      props.passwordPolicy = passwordPolicy;
    }

    return { props };
  } finally {
    requestSpan.finish();
  }
};
export default OrganizationSettingsSecurityAndPrivacy;
