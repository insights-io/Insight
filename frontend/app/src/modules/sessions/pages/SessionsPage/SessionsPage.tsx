import React, { useMemo, useState } from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import { useStyletron } from 'baseui';
import { RecordingSnippet } from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import type { OrganizationDTO, SessionDTO, UserDTO } from '@insight/types';
import { SessionList } from 'modules/sessions/components/SessionList';
import { SessionSearch } from 'modules/sessions/components/SessionSearch';
import { Block } from 'baseui/block';
import {
  DateRange,
  createDateRange,
} from 'modules/sessions/components/SessionSearch/utils';
import { SessionFilter } from 'modules/sessions/components/SessionSearch/SessionFilters/utils';
import { useUser } from 'shared/hooks/useUser';
import { useSessions } from 'modules/sessions/hooks/useSessions';
import { useOrganization } from 'shared/hooks/useOrganization';
import Head from 'next/head';

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
  const [_css, theme] = useStyletron();
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);
  const hasSessions = initialSessions.length > 0;
  const [filters, setFilters] = useState<SessionFilter[]>([]);
  const [dateRange, setDataRange] = useState<DateRange>(() =>
    createDateRange('all-time')
  );

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
          <Block marginTop={theme.sizing.scale400} height="100%">
            <SessionList
              sessions={sessions}
              count={count}
              loadMoreItems={loadMoreItems}
              isItemLoaded={isItemLoaded}
            />
          </Block>
        </>
      ) : (
        user && (
          <RecordingSnippet
            organizationId={user.organizationId}
            snippetUri={BOOTSTRAP_SCRIPT_URI}
          />
        )
      )}
    </AppLayout>
  );
};
