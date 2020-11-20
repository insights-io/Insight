import React from 'react';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import { SessionPage } from 'modules/sessions/pages/SessionPage';
import { SessionApi } from 'api';
import type { SessionDTO, APIErrorDataResponse } from '@rebrowse/types';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';

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

    const session = await SessionApi.getSession(sessionId, {
      baseURL: process.env.SESSION_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    }).catch(async (error) => {
      const errorDTO: APIErrorDataResponse = await error.response.json();
      if (errorDTO.error.statusCode === 404) {
        context.res.writeHead(302, { Location: '/' });
        context.res.end();
      }
      throw error;
    });
    return {
      props: {
        sessionId,
        user: authResponse.user,
        organization: authResponse.organization,
        session,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default Session;
