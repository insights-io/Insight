import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsSecurityPage } from 'settings/pages/organization/OrganizationSettingsSecurityPage';
import type {
  OrganizationDTO,
  OrganizationPasswordPolicyDTO,
} from '@rebrowse/types';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { client } from 'sdk';

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

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const passwordPolicy = await client.auth.organizations.passwordPolicy
      .retrieve({ headers })
      .then((httpResponse) => httpResponse.data)
      .catch((error) => {
        const response = error.response as Response;
        if (response.status === 404) {
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
