import React from 'react';
import NetworkTab from 'modules/sessions/components/NetworkTab';
import { BrowserXhrEventDTO } from '@rebrowse/types';

import { useEvents } from './useEvents';

type Props = {
  sessionId: string;
};

const NetworkTabContainer = ({ sessionId }: Props) => {
  const { data } = useEvents(sessionId);

  const loading = data === undefined;
  const events = (data || []) as BrowserXhrEventDTO[];

  return <NetworkTab events={events} loading={loading} />;
};

export default NetworkTabContainer;
