import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { AuthApi } from 'api';
import type { TeamInviteDTO } from '@rebrowse/types';
import { OrganizationSettingsMemberInvitesPage } from 'settings/pages/organization/OrganizationSettingsMemberInvitesPage';

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

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const teamInvitesPromise = AuthApi.organization.teamInvite
      .list({
        baseURL: process.env.AUTH_API_BASE_URL,
        search: { limit: 20, sortBy: ['+createdAt'] },
        headers,
      })
      .then((httpResponse) => httpResponse.data);

    const inviteCountPromise = AuthApi.organization.teamInvite
      .count({ baseURL: process.env.AUTH_API_BASE_URL, headers })
      .then((httpResponse) => httpResponse.data.count);

    const [teamInvites, inviteCount] = await Promise.all([
      teamInvitesPromise,
      inviteCountPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        teamInvites,
        inviteCount,
        organization: authResponse.organization,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsMembers;
