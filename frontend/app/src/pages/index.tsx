import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { SessionApi } from 'api';
import { SessionDTO } from '@insight/types';
import { mapSession, mapUser } from '@insight/sdk';
import HomePage from 'modules/app/pages/HomePage';

type Props = AuthenticatedServerSideProps & {
  sessions: SessionDTO[];
};

const Home = ({ user: initialUser, sessions: initialSessions }: Props) => {
  return (
    <HomePage
      user={mapUser(initialUser)}
      sessions={initialSessions.map(mapSession)}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  const authResponse = await authenticated(context, requestSpan);
  if (!authResponse) {
    return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
  }

  const sessions = await SessionApi.getSessions({
    baseURL: process.env.SESSION_API_BASE_URL,
    headers: {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    },
  });

  return { props: { user: authResponse.user, sessions } };
};

export default Home;
