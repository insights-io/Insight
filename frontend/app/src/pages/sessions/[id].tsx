import React from 'react';
import { GetServerSideProps } from 'next';
import useSession from 'modules/sessions/hooks/useSession';
import AppLayout from 'modules/app/components/AppLayout';
import Router from 'next/router';
import SessionDetails from 'modules/sessions/components/SessionDetails.tsx';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { UserDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  sessionId: string;
};

const SessionPage = ({ sessionId }: Props) => {
  const { loading, session } = useSession(sessionId);
  if (loading) {
    return null;
  }

  if (!session) {
    Router.replace('/');
    return null;
  }

  return (
    <AppLayout>
      <SessionDetails sessionId={sessionId} />
    </AppLayout>
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const { params } = context;
  const user = (await authenticated(context)) as UserDTO;
  return { props: { sessionId: params?.id as string, user } };
};

export default SessionPage;
