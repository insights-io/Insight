import React from 'react';
import authenticated from 'modules/auth/hoc/authenticated';
import AppLayout from 'modules/app/components/AppLayout';
import useSessions from 'modules/sessions/hooks/useSessions';
import { ListItem, ListItemLabel } from 'baseui/list';
import { H5 } from 'baseui/typography';
import { formatDistanceToNow } from 'date-fns';
import { Tag } from 'baseui/tag';

const Home = () => {
  const { data } = useSessions();

  return (
    <AppLayout>
      <H5 margin={['24px', '48px']}>Sessions</H5>

      <ul>
        {data.map((session) => {
          return (
            <ListItem
              key={session.id}
              endEnhancer={() => (
                <Tag closeable={false}>
                  {formatDistanceToNow(session.createdAt, {
                    includeSeconds: true,
                    addSuffix: true,
                  })}
                </Tag>
              )}
            >
              <ListItemLabel description={session.userAgent}>
                {session.ipAddress}
              </ListItemLabel>
            </ListItem>
          );
        })}
      </ul>
    </AppLayout>
  );
};

export default authenticated(Home);
