import React from 'react';
import useSession from 'modules/sessions/hooks/useSession';
import AppLayout from 'modules/app/components/AppLayout';
import SessionDetails from 'modules/sessions/components/SessionDetails.tsx';
import { Session } from '@insight/types';

type Props = {
  sessionId: string;
  session: Session;
};

const SessionPage = ({ sessionId, session: initialSession }: Props) => {
  const { session } = useSession(sessionId, initialSession);

  return (
    <AppLayout>
      <SessionDetails session={session} />
    </AppLayout>
  );
};

export default SessionPage;
