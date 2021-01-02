import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsMembersPage } from 'settings/pages/organization/OrganizationSettingsMembersPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import type { UserDTO } from '@rebrowse/types';
import { client } from 'sdk';

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

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const membersPromise = client.auth.organizations.members
      .list({ search: { limit: 20, sortBy: ['+createdAt'] }, headers })
      .then((httpResponse) => httpResponse.data);

    const memberCountPromise = client.auth.organizations.members
      .count({ headers })
      .then((httpResponse) => httpResponse.data.count);

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
