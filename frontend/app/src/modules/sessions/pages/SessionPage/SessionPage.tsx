import React from 'react';
import useSession from 'modules/sessions/hooks/useSession';
import AppLayout from 'modules/app/components/AppLayout';
import SessionDetails from 'modules/sessions/components/SessionDetails.tsx';
import { Session, User } from '@insight/types';

type Props = {
  sessionId: string;
  session: Session;
  user: User;
};

const SessionPage = ({ user, sessionId, session: initialSession }: Props) => {
  const { session } = useSession(sessionId, initialSession);

  return (
    <AppLayout user={user}>
      <SessionDetails session={session} />
    </AppLayout>
  );
};

export default SessionPage;
