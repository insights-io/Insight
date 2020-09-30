import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
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
  teamInvites: TeamInviteDTO[];
};

export const OrganizationSettingsMembers = ({
  members,
  teamInvites,
  user,
}: Props) => {
  return (
    <OrganizationSettingsMembersPage
      members={members}
      teamInvites={teamInvites}
      user={user}
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

    const teamInvitesPromise = AuthApi.organization.teamInvite.list({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [members, teamInvites] = await Promise.all([
      membersPromise,
      teamInvitesPromise,
    ]);

    return { props: { user: authResponse.user, members, teamInvites } };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsMembers;
