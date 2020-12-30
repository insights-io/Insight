import React from 'react';
import type { GetServerSideProps } from 'next';
import type { TeamInviteDTO } from '@rebrowse/types';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { AuthApi } from 'api';
import { INDEX_PAGE } from 'shared/constants/routes';
import { AcceptTeamInviteInvalidPage } from 'auth/pages/AcceptTeamInviteInvalidPage';
import { AcceptTeamInvitePage } from 'auth/pages/AcceptTeamInvitePage';
import { getData, mapTeamInvite } from '@rebrowse/sdk';

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
      .then((httpResponse) => getData(httpResponse))
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
