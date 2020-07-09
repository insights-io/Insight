import React from 'react';
import useSession from 'modules/sessions/hooks/useSession';
import AppLayout from 'modules/app/components/AppLayout';
import Router from 'next/router';
import SessionDetails from 'modules/sessions/components/SessionDetails.tsx';

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

  return (
    <AppLayout>
      <SessionDetails sessionId={sessionId} />
    </AppLayout>
  );
};

export default SessionPage;
