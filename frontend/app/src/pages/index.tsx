import React from 'react';
import authenticated from 'modules/auth/hoc/authenticated';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { ListItem, ListItemLabel } from 'baseui/list';
import { H5 } from 'baseui/typography';
import { formatDistanceToNow } from 'date-fns';
import { Tag } from 'baseui/tag';
import { ChevronRight } from 'baseui/icon';
import Link from 'next/link';
import { useStyletron } from 'baseui';

const Home = () => {
  const { data } = useSessions();
  const [css, theme] = useStyletron();

  return (
    <AppLayout>
      <H5 margin={['24px', '48px']}>Sessions</H5>
      <ul>
        {data.map((session) => {
          return (
            <Link href={`/sessions/${session.id}`} key={session.id}>
              <a className={css({ color: 'inherit' })}>
                <ListItem
                  overrides={{
                    Root: {
                      style: {
                        ':hover': { background: theme.colors.primary200 },
                      },
                    },
                  }}
                  endEnhancer={() => (
                    <>
                      <Tag closeable={false}>
                        {formatDistanceToNow(session.createdAt, {
                          includeSeconds: true,
                          addSuffix: true,
                        })}
                      </Tag>
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
    </AppLayout>
  );
};

export default authenticated(Home);
