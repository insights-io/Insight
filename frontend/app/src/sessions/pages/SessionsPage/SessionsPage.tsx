import React, { useMemo, useState } from 'react';
import { AppLayout } from 'shared/components/AppLayout';
import { useStyletron } from 'baseui';
import { RecordingSnippet } from 'sessions/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import type { OrganizationDTO, SessionDTO, UserDTO } from '@rebrowse/types';
import { SessionList } from 'sessions/components/SessionList';
import { SessionSearch } from 'sessions/components/SessionSearch';
import { Block } from 'baseui/block';
import {
  DateRange,
  createDateRange,
} from 'sessions/components/SessionSearch/utils';
import { parseQueryFilters } from 'sessions/components/SessionSearch/SessionFilters/utils';
import { useUser } from 'shared/hooks/useUser';
import { useSessions } from 'sessions/hooks/useSessions';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';
import { useRouter } from 'next/router';

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
  sessions: SessionDTO[];
  sessionCount: number;
};

export const SessionsPage = ({
  user: initialUser,
  organization: initialOrganization,
  sessions: initialSessions,
  sessionCount: initialSessionCount,
}: Props) => {
  const { query } = useRouter();

  const [_css, theme] = useStyletron();
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const [filters, setFilters] = useState(() => parseQueryFilters(query));
  const [dateRange, setDataRange] = useState<DateRange>(() =>
    createDateRange('all-time')
  );
  const hasSessions = initialSessionCount > 0;
  const options = useMemo(() => ({ dateRange, filters }), [dateRange, filters]);
  const { sessions, count, loadMoreItems, isItemLoaded } = useSessions(
    initialSessions,
    initialSessionCount,
    options
  );

  return (
    <AppLayout
      user={user}
      organization={organization}
      overrides={{
        MainContent: {
          style: {
            background: theme.colors.mono300,
            padding: theme.sizing.scale400,
          },
        },
      }}
    >
      <Head>
        <title>Sessions</title>
      </Head>

      {hasSessions ? (
        <>
          <SessionSearch
            onDateRangeChange={setDataRange}
            setFilters={setFilters}
            filters={filters}
          />
          <Block
            marginTop={theme.sizing.scale400}
            height="100%"
            className="sessions"
          >
            <SessionList
              sessions={sessions}
              count={count}
              loadMoreItems={loadMoreItems}
              isItemLoaded={isItemLoaded}
            />
          </Block>
        </>
      ) : (
        <RecordingSnippet
          organizationId={user.organizationId}
          snippetUri={BOOTSTRAP_SCRIPT_URI}
        />
      )}
    </AppLayout>
  );
};
