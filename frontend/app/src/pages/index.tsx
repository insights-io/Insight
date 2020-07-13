import React from 'react';
import {
  AuthenticatedServerSideProps,
  authenticated,
  Authenticated,
} from 'modules/auth/middleware/authMiddleware';
import { GetServerSideProps } from 'next';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { SessionApi } from 'api';
import { SessionDTO, UserDTO } from '@insight/types';
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
  const { user, SessionId } = (await authenticated(
    context,
    requestSpan
  )) as Authenticated;
  const sessions = await SessionApi.getSessions({
    baseURL: process.env.SESSION_API_BASE_URL,
    headers: {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${SessionId}`,
    },
  });

  return { props: { user, sessions } };
};

export default Home;
