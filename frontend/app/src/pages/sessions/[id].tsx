import React from 'react';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import { SessionPage } from 'sessions/pages/SessionPage';
import type { SessionDTO } from '@rebrowse/types';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { client } from 'sdk';

type Props = AuthenticatedServerSideProps & {
  sessionId: string;
  session: SessionDTO;
};

const Session = ({ sessionId, session, user, organization }: Props) => {
  return (
    <SessionPage
      sessionId={sessionId}
      session={session}
      user={user}
      organization={organization}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const { params } = context;
  const requestSpan = startRequestSpan(context.req);
  const sessionId = params?.id as string;

  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    return client.recording.sessions
      .retrieve(sessionId, {
        headers: {
          ...prepareCrossServiceHeaders(requestSpan),
          cookie: `SessionId=${authResponse.SessionId}`,
        },
      })
      .then((httpResponse) => ({
        props: {
          sessionId,
          user: authResponse.user,
          organization: authResponse.organization,
          session: httpResponse.data,
        },
      }))
      .catch((error) => {
        const response = error.response as Response;
        if (response.status !== 404) {
          throw error;
        }

        return { redirect: { destination: SESSIONS_PAGE, statusCode: 302 } };
      });
  } finally {
    requestSpan.finish();
  }
};

export default Session;
