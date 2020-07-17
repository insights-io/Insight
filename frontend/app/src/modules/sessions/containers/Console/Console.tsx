import React from 'react';
import Console, { ConsoleEventDTO } from 'modules/sessions/components/Console';
import useSWR from 'swr';
import { SessionApi } from 'api';

type Props = {
  sessionId: string;
};

const ConsoleContainer = ({ sessionId }: Props) => {
  const { data } = useSWR(
    `ConsoleContainer.sessions/${sessionId}/events/search`,
    () =>
      SessionApi.events.search(sessionId, {
        searchParams: { 'event.e': ['gte:9', 'lte:10'], limit: 1000 },
      }),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  const loading = data === undefined;
  const events = (data || []) as ConsoleEventDTO[];

  return <Console events={events.slice(0, 50)} loading={loading} />;
};

export default React.memo(ConsoleContainer);
