import React from 'react';
import NetworkTab from 'sessions/components/NetworkTab';
import type { BrowserXhrEventDTO } from '@rebrowse/types';

import { useEvents } from './useEvents';

type Props = {
  sessionId: string;
};

export const NetworkTabContainer = React.memo(({ sessionId }: Props) => {
  const { data } = useEvents(sessionId);

  const loading = data === undefined;
  const events = (data || []) as BrowserXhrEventDTO[];

  return <NetworkTab events={events} loading={loading} />;
});

export default NetworkTabContainer;
