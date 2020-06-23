import React from 'react';
import { GetServerSideProps } from 'next';
import useSession from 'modules/sessions/hooks/useSession';
import AppLayout from 'modules/app/components/AppLayout';
import Router from 'next/router';

type Props = {
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

  return <AppLayout>{session.id}</AppLayout>;
};

export const getServerSideProps: GetServerSideProps<Props> = async ({
  params = {},
}) => {
  return { props: { sessionId: params.id as string } };
};

export default SessionPage;
