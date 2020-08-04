import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { SessionDTO, APIErrorDataResponse } from '@insight/types';
import { startRequestSpan, prepareCrossServiceHeaders } from 'modules/tracing';
import SessionPage from 'modules/sessions/pages/SessionPage';
import { SessionApi } from 'api';
import { mapSession } from '@insight/sdk';

type Props = AuthenticatedServerSideProps & {
  sessionId: string;
  session: SessionDTO;
};

const Session = ({ sessionId, session }: Props) => {
  return <SessionPage sessionId={sessionId} session={mapSession(session)} />;
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
    return { props: { sessionId, user: authResponse.user, session } };
  } finally {
    requestSpan.finish();
  }
};

export default Session;
