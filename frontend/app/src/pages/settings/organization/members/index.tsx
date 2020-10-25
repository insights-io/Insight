import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsMembersPage } from 'modules/settings/pages/organization/OrganizationSettingsMembersPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import type { TeamInviteDTO, UserDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  members: UserDTO[];
  memberCount: number;
  teamInvites: TeamInviteDTO[];
};

export const OrganizationSettingsMembers = ({
  members,
  memberCount,
  teamInvites,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsMembersPage
      pageTab="Members"
      members={members}
      memberCount={memberCount}
      teamInvites={teamInvites}
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

    const membersPromise = AuthApi.organization.members({
      baseURL: process.env.AUTH_API_BASE_URL,
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

    const teamInvitesPromise = AuthApi.organization.teamInvite.list({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [members, memberCount, teamInvites] = await Promise.all([
      membersPromise,
      memberCountPromise,
      teamInvitesPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        memberCount,
        members,
        teamInvites,
        organization: authResponse.organization,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsMembers;
