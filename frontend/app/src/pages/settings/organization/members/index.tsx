import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsMembersPage } from 'modules/settings/pages/organization/OrganizationSettingsMembersPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import type { UserDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  members: UserDTO[];
  memberCount: number;
};

export const OrganizationSettingsMembers = ({
  members,
  memberCount,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsMembersPage
      user={user}
      organization={organization}
      memberCount={memberCount}
      members={members}
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

    const membersPromise = AuthApi.organization.members({
      baseURL: process.env.AUTH_API_BASE_URL,
      search: { limit: 20, sort_by: ['+created_at'] },
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const memberCountPromise = AuthApi.organization.memberCount({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [members, memberCount] = await Promise.all([
      membersPromise,
      memberCountPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        memberCount,
        members,

        organization: authResponse.organization,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsMembers;
