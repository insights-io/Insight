import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'auth/middleware/authMiddleware';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  startRequestSpan,
  prepareCrossServiceHeaders,
} from 'shared/utils/tracing';
import type { SessionDTO } from '@rebrowse/types';
import { SessionsPage } from 'sessions/pages/SessionsPage';
import { client } from 'sdk';

type Props = AuthenticatedServerSideProps & {
  sessions: SessionDTO[];
  sessionCount: number;
};

const Sessions = ({ user, organization, sessions, sessionCount }: Props) => {
  return (
    <SessionsPage
      user={user}
      organization={organization}
      sessions={sessions}
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

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const sessionsPromise = client.recording.sessions
      .list({
        search: { sortBy: ['-createdAt'], limit: 20, ...context.query },
        headers,
      })
      .then((httpResponse) => httpResponse.data);

    // TODO: should probably limit time range
    const sessionCountPromise = client.recording.sessions
      .count({ headers, search: context.query })
      .then((httpResponse) => httpResponse.data.count);

    const [sessions, sessionCount] = await Promise.all([
      sessionsPromise,
      sessionCountPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        organization: authResponse.organization,
        sessions,
        sessionCount,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Sessions;
