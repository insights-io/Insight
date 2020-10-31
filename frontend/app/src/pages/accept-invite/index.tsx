import React from 'react';
import type { GetServerSideProps } from 'next';
import type { APIErrorDataResponse, TeamInviteDTO } from '@insight/types';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi } from 'api';
import { INDEX_PAGE } from 'shared/constants/routes';
import { AcceptTeamInviteInvalidPage } from 'modules/auth/pages/AcceptTeamInviteInvalidPage';
import { AcceptTeamInvitePage } from 'modules/auth/pages/AcceptTeamInvitePage';
import { mapTeamInvite } from '@insight/sdk';

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
      context.res.writeHead(302, { Location: INDEX_PAGE });
      context.res.end();
      return { props: { invite: null } };
    }

    const invite = await AuthApi.organization.teamInvite
      .retrieve(token, {
        baseURL: process.env.AUTH_API_BASE_URL,
        headers: prepareCrossServiceHeaders(requestSpan),
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        if (errorDTO.error.statusCode === 404) {
          return null;
        }
        requestSpan.log(errorDTO);
        throw error;
      });

    return { props: { invite } };
  } finally {
    requestSpan.finish();
  }
};

export default AcceptInvite;
