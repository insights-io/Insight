import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { useStyletron } from 'baseui';
import useAuth from 'modules/auth/hooks/useAuth';
import RecordingSnippet from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import { Session, User } from '@insight/types';
import SessionList from 'modules/sessions/containers/SessionList';

type Props = {
  user: User;
  sessions: Session[];
  sessionCount: number;
};

const HomePage = ({
  user: initialUser,
  sessions: initialSessions,
  sessionCount,
}: Props) => {
  const [_css, theme] = useStyletron();
  const { user } = useAuth(initialUser);
  const { data: sessions, loading: loadingSessions } = useSessions(
    initialSessions
  );
  const hasSessions = loadingSessions || sessions.length > 0;

  return (
    <AppLayout
      overrides={{
        MainContent: { style: { background: theme.colors.mono300 } },
      }}
    >
      {hasSessions ? (
        <SessionList
          initialSessions={initialSessions}
          sessionCount={sessionCount}
        />
      ) : (
        user && (
          <RecordingSnippet
            organizationId={user.organizationId}
            snipetURI={BOOTSTRAP_SCRIPT_URI}
          />
        )
      )}
    </AppLayout>
  );
};

export default HomePage;
