import React from 'react';
import { AppLayout } from 'shared/components/AppLayout';
import { SessionDetails } from 'modules/sessions/components/SessionDetails.tsx';
import { useUser } from 'shared/hooks/useUser';
import { useSession } from 'modules/sessions/hooks/useSession';
import type { OrganizationDTO, SessionDTO, UserDTO } from '@rebrowse/types';
import { useOrganization } from 'shared/hooks/useOrganization';

type Props = {
  sessionId: string;
  session: SessionDTO;
  user: UserDTO;
  organization: OrganizationDTO;
};

export const SessionPage = ({
  user: initialUser,
  sessionId,
  session: initialSession,
  organization: initialOrganization,
}: Props) => {
  const { session } = useSession(sessionId, initialSession);
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <AppLayout user={user} organization={organization}>
      <SessionDetails session={session} />
    </AppLayout>
  );
};
