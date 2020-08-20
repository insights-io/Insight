import React, { useState } from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { useStyletron } from 'baseui';
import useAuth from 'modules/auth/hooks/useAuth';
import RecordingSnippet from 'modules/setup/components/RecordingSnippet';
import { BOOTSTRAP_SCRIPT_URI } from 'shared/config';
import { Session, User } from '@insight/types';
import SessionList from 'modules/sessions/containers/SessionList';
import SessionSearch from 'modules/sessions/components/SessionSearch';
import { Block } from 'baseui/block';
import {
  DateRange,
  createDateRange,
} from 'modules/sessions/components/SessionSearch/utils';
import { SessionFilter } from 'modules/sessions/components/SessionSearch/SessionFilters/utils';

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
  const hasSessions = initialSessions.length > 0;
  const [dateRange, setDataRange] = useState<DateRange>(() =>
    createDateRange('all-time')
  );
  const [filters, setFilters] = useState<SessionFilter[]>([]);

  return (
    <AppLayout
      overrides={{
        MainContent: {
          style: {
            background: theme.colors.mono300,
            padding: theme.sizing.scale400,
          },
        },
      }}
    >
      {hasSessions ? (
        <>
          <SessionSearch
            onDateRangeChange={setDataRange}
            setFilters={setFilters}
            filters={filters}
          />
          <Block marginTop={theme.sizing.scale400} height="100%">
            <SessionList
              initialSessions={initialSessions}
              initialSessionCount={sessionCount}
              dateRange={dateRange}
              filters={filters}
            />
          </Block>
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
