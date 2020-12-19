import React from 'react';
import type { GetServerSideProps } from 'next';
import type { TeamInviteDTO } from '@rebrowse/types';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import { INDEX_PAGE } from 'shared/constants/routes';
import { AcceptTeamInviteInvalidPage } from 'modules/auth/pages/AcceptTeamInviteInvalidPage';
import { AcceptTeamInvitePage } from 'modules/auth/pages/AcceptTeamInvitePage';
import { mapTeamInvite } from '@rebrowse/sdk';

type Props = { invite: TeamInviteDTO } | { invite: null };

const AcceptInvite = ({ invite }: Props) => {
  if (!invite || !invite.valid) {
    return <AcceptTeamInviteInvalidPage expiresAt={invite?.expiresAt} />;
  }

  return <AcceptTeamInvitePage {...mapTeamInvite(invite)} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const token = context.query.token as string | undefined;
    if (!token) {
      requestSpan.log({
        message: `Missing token: redirecting to ${INDEX_PAGE}`,
      });
      return { redirect: { destination: INDEX_PAGE, statusCode: 302 } };
    }

    const invite = await AuthApi.organization.teamInvite
      .retrieve(token, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      })
      .catch((error) => {
        const response = error.response as Response;
        if (response.status === 404) {
          return null;
        }
        throw error;
      });

    return { props: { invite } };
  } finally {
    requestSpan.finish();
  }
};

export default AcceptInvite;
