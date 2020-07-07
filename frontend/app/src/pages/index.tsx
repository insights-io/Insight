import React from 'react';
import {
  AuthMiddlewareProps,
  getServerSideAuthProps,
} from 'modules/auth/middleware/authMiddleware';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { ListItem, ListItemLabel } from 'baseui/list';
import { H5 } from 'baseui/typography';
import { formatDistanceToNow } from 'date-fns';
import { Tag } from 'baseui/tag';
import { ChevronRight } from 'baseui/icon';
import { useStyletron } from 'baseui';
import Link from 'next/link';
import useAuth from 'modules/auth/hooks/useAuth';
import RecordingSnippet from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import { GetServerSideProps } from 'next';

type Props = AuthMiddlewareProps;

const Home = ({ user: initialUser }: Props) => {
  const { user } = useAuth(initialUser);
  const { data: sessions, loading: loadingSessions } = useSessions();
  const [css, theme] = useStyletron();

  const listItemStyle = {
    ':hover': { background: theme.colors.primary200 },
  };

  return (
    <AppLayout
      overrides={{ MainContent: { style: { padding: theme.sizing.scale600 } } }}
    >
      {loadingSessions ||
        (sessions.length > 0 && (
          <>
            <H5 margin={0}>Sessions</H5>
            <ul className="sessions">
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
                        endEnhancer={() => (
                          <>
                            <Tag closeable={false}>{createdAtText}</Tag>
                            <ChevronRight />
                          </>
                        )}
                      >
                        <ListItemLabel description={session.userAgent}>
                          {session.ipAddress}
                        </ListItemLabel>
                      </ListItem>
                    </a>
                  </Link>
                );
              })}
            </ul>
          </>
        ))}

      {user && (
        <RecordingSnippet
          organizationId={user.organizationId}
          snipetURI={BOOTSTRAP_SCRIPT_URI}
        />
      )}
    </AppLayout>
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getServerSideAuthProps;

export default Home;
