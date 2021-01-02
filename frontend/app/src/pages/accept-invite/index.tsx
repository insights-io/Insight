import React from 'react';
import type { GetServerSideProps } from 'next';
import type { TeamInviteDTO } from '@rebrowse/types';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { INDEX_PAGE } from 'shared/constants/routes';
import { AcceptTeamInviteInvalidPage } from 'auth/pages/AcceptTeamInviteInvalidPage';
import { AcceptTeamInvitePage } from 'auth/pages/AcceptTeamInvitePage';
import { client } from 'sdk';

type Props = { invite: TeamInviteDTO } | { invite: null };

const AcceptInvite = ({ invite }: Props) => {
  if (!invite || !invite.valid) {
    return <AcceptTeamInviteInvalidPage expiresAt={invite?.expiresAt} />;
  }

  return <AcceptTeamInvitePage {...invite} />;
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

    const invite = await client.auth.organizations.teamInvite
      .retrieve(token, { headers: prepareCrossServiceHeaders(requestSpan) })
      .then((httpResponse) => httpResponse.data)
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
