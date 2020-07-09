import React from 'react';
import { GetServerSideProps } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { UserDTO } from '@insight/types';
import { startRequestSpan } from 'modules/tracing';
import SessionPage from 'modules/sessions/pages/SessionPage';

type Props = AuthenticatedServerSideProps & {
  sessionId: string;
};

const Session = ({ sessionId }: Props) => {
  return <SessionPage sessionId={sessionId} />;
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const { params } = context;
  const requestSpan = startRequestSpan(context.req);
  try {
    const user = (await authenticated(context, requestSpan)) as UserDTO;

    return { props: { sessionId: params?.id as string, user } };
  } finally {
    requestSpan.finish();
  }
};

export default Session;
