import React from 'react';
import useSWR from 'swr';
import { SessionApi } from 'api';
import NetworkTab from 'modules/sessions/components/NetworkTab';
import { BrowserXhrEventDTO } from '@insight/types';

type Props = {
  sessionId: string;
};

const NetworkTabContainer = ({ sessionId }: Props) => {
  const { data } = useSWR(
    `NetworkTab.sessions/${sessionId}/events/search`,
    () =>
      SessionApi.events.search(sessionId, {
        searchParams: { 'event.e': ['eq:11'], limit: 1000 },
      }),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  const loading = data === undefined;
  const events = (data || []) as BrowserXhrEventDTO[];

  return <NetworkTab events={events} loading={loading} />;
};

export default NetworkTabContainer;
