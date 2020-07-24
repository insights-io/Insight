import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { H5 } from 'baseui/typography';
import { useStyletron } from 'baseui';
import Link from 'next/link';
import useAuth from 'modules/auth/hooks/useAuth';
import RecordingSnippet from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import { Session, User } from '@insight/types';
import SessionListItem from 'modules/sessions/components/SessionListItem';

type Props = {
  user: User;
  sessions: Session[];
};

const HomePage = ({ user: initialUser, sessions: initialSessions }: Props) => {
  const [css, theme] = useStyletron();
  const { user } = useAuth(initialUser);
  const { data: sessions, loading: loadingSessions } = useSessions(
    initialSessions
  );
  const hasSessions = loadingSessions || sessions.length > 0;

  return (
    <AppLayout
      overrides={{
        MainContent: {
          style: {
            padding: theme.sizing.scale600,
            background: theme.colors.mono300,
          },
        },
      }}
    >
      {hasSessions ? (
        <>
          <H5 margin={0}>Sessions</H5>
          <ul className={css({ paddingLeft: 0, overflow: 'auto' })}>
            {sessions.map((session) => {
              return (
                <Link
                  href="/sessions/[id]"
                  as={`sessions/${session.id}`}
                  key={session.id}
                >
                  <a className={css({ color: 'inherit' })}>
                    <SessionListItem session={session} />
                  </a>
                </Link>
              );
            })}
          </ul>
        </>
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
