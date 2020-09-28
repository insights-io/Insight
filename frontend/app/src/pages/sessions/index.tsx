import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { SessionApi } from 'api';
import { SessionDTO } from '@insight/types';
import { mapSession } from '@insight/sdk';
import SessionsPage from 'modules/sessions/pages/SessionsPage';

type Props = AuthenticatedServerSideProps & {
  sessions: SessionDTO[];
  sessionCount: number;
};

const Sessions = ({
  user: initialUser,
  sessions: initialSessions,
  sessionCount,
}: Props) => {
  return (
    <SessionsPage
      user={initialUser}
      sessions={initialSessions.map(mapSession)}
      sessionCount={sessionCount}
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

    const sessionsPromise = await SessionApi.getSessions({
      baseURL: process.env.SESSION_API_BASE_URL,
      search: { sort_by: ['-created_at'], limit: 20 },
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const sessionCountPromise = SessionApi.count({
      baseURL: process.env.SESSION_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    }).then((dataResponse) => dataResponse.count);

    const [sessions, sessionCount] = await Promise.all([
      sessionsPromise,
      sessionCountPromise,
    ]);

    return { props: { user: authResponse.user, sessions, sessionCount } };
  } finally {
    requestSpan.finish();
  }
};

export default Sessions;
