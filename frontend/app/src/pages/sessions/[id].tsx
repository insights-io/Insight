import React from 'react';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import { SessionPage } from 'modules/sessions/pages/SessionPage';
import { SessionApi } from 'api';
import type { SessionDTO } from '@rebrowse/types';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { SESSIONS_PAGE } from 'shared/constants/routes';

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

    return SessionApi.getSession(sessionId, {
      baseURL: process.env.SESSION_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    })
      .then((session) => ({
        props: {
          sessionId,
          user: authResponse.user,
          organization: authResponse.organization,
          session,
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
