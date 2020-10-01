import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import SessionDetails from 'modules/sessions/components/SessionDetails.tsx';
import { useUser } from 'shared/hooks/useUser';
import { useSession } from 'modules/sessions/hooks/useSession';
import type { SessionDTO, UserDTO } from '@insight/types';

type Props = {
  sessionId: string;
  session: SessionDTO;
  user: UserDTO;
};

export const SessionPage = ({
  user: initialUser,
  sessionId,
  session: initialSession,
}: Props) => {
  const { session } = useSession(sessionId, initialSession);
  const { user } = useUser(initialUser);

  return (
    <AppLayout user={user}>
      <SessionDetails session={session} />
    </AppLayout>
  );
};
