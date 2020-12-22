import React from 'react';
import { AppLayout } from 'shared/components/AppLayout';
import { SessionDetails } from 'sessions/components/SessionDetails';
import { useUser } from 'shared/hooks/useUser';
import { useSession } from 'sessions/hooks/useSession/useSession';
import type { OrganizationDTO, SessionDTO, UserDTO } from '@rebrowse/types';
import { useOrganization } from 'shared/hooks/useOrganization';
import { useStyletron } from 'baseui';
import { DeveloperTools } from 'sessions/containers/DeveloperTools';
import { SpacedBetween } from '@rebrowse/elements';
import Link from 'next/link';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { BackButton } from 'shared/components/BackButton';
import { Block } from 'baseui/block';

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
  const [_css, theme] = useStyletron();

  return (
    <AppLayout user={user} organization={organization}>
      <SpacedBetween
        $style={{ border: '1px solid rgb(231, 225, 236)' }}
        backgroundColor={theme.colors.white}
        padding={theme.sizing.scale600}
      >
        <Link href={SESSIONS_PAGE}>
          <a>
            <BackButton label="Back to all sesions" />
          </a>
        </Link>
        <DeveloperTools sessionId={session.id}>
          {(open) => <DeveloperTools.Trigger open={open} />}
        </DeveloperTools>
      </SpacedBetween>

      <Block
        as="section"
        padding={theme.sizing.scale600}
        backgroundColor={theme.colors.white}
      >
        <SessionDetails session={session} />
      </Block>
    </AppLayout>
  );
};
