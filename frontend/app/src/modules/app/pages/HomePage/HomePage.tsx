import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { ListItem, ListItemLabel, ARTWORK_SIZES } from 'baseui/list';
import { H5 } from 'baseui/typography';
import { formatDistanceToNow } from 'date-fns';
import { Tag } from 'baseui/tag';
import { ChevronRight } from 'baseui/icon';
import { useStyletron } from 'baseui';
import Link from 'next/link';
import useAuth from 'modules/auth/hooks/useAuth';
import RecordingSnippet from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import { Session, User } from '@insight/types';
import { FaDesktop } from 'react-icons/fa';

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

  const listItemStyle = {
    ':hover': { background: theme.colors.primary200 },
    borderRadius: theme.sizing.scale100,
  };
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
              const createdAtText = formatDistanceToNow(session.createdAt, {
                includeSeconds: true,
                addSuffix: true,
              });

              return (
                <Link
                  href="/sessions/[id]"
                  as={`sessions/${session.id}`}
                  key={session.id}
                >
                  <a className={css({ color: 'inherit' })}>
                    <ListItem
                      overrides={{ Root: { style: listItemStyle } }}
                      artwork={FaDesktop}
                      artworkSize={ARTWORK_SIZES.SMALL}
                      endEnhancer={() => (
                        <>
                          <Tag
                            closeable={false}
                            overrides={{
                              Root: {
                                style: { ':hover': { cursor: 'pointer' } },
                              },
                            }}
                          >
                            Details
                          </Tag>
                          <ChevronRight />
                        </>
                      )}
                    >
                      <ListItemLabel
                        description={`Unknown location - ${session.ipAddress} - ${createdAtText}`}
                      >
                        {session.userAgent.operatingSystemName} &bull;{' '}
                        {session.userAgent.browserName}
                      </ListItemLabel>
                    </ListItem>
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
