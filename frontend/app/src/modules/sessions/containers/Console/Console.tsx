import React from 'react';
import Console, { ConsoleEventDTO } from 'modules/sessions/components/Console';
import useSWR from 'swr';
import { SessionApi } from 'api';
import { StyleObject } from 'styletron-react';

type Props = {
  sessionId: string;
  style?: StyleObject;
};

const ConsoleContainer = ({ sessionId, style }: Props) => {
  const { data } = useSWR(
    `sessions/${sessionId}/events/search`,
    () => SessionApi.events.get(sessionId),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  const loading = data === undefined;
  const events = (data || []) as ConsoleEventDTO[];

  return (
    <Console events={events.slice(0, 10)} style={style} loading={loading} />
  );
};

export default ConsoleContainer;
