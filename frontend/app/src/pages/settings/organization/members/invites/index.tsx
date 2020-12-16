import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import type { TeamInviteDTO } from '@rebrowse/types';
import { OrganizationSettingsMemberInvitesPage } from 'modules/settings/pages/organization/OrganizationSettingsMemberInvitesPage';

type Props = AuthenticatedServerSideProps & {
  teamInvites: TeamInviteDTO[];
  inviteCount: number;
};

export const OrganizationSettingsMembers = ({
  teamInvites,
  inviteCount,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsMemberInvitesPage
      user={user}
      organization={organization}
      invites={teamInvites}
      inviteCount={inviteCount}
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

    const { user, organization } = authResponse;
    const teamInvitesPromise = AuthApi.organization.teamInvite.list({
      baseURL: process.env.AUTH_API_BASE_URL,
      search: { limit: 20, sortBy: ['+createdAt'] },
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const inviteCountPromise = AuthApi.organization.teamInvite.count({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [teamInvites, inviteCount] = await Promise.all([
      teamInvitesPromise,
      inviteCountPromise,
    ]);

    return { props: { user, teamInvites, inviteCount, organization } };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsMembers;
