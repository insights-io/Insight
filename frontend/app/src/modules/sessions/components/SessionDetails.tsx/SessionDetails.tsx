import React, { useMemo } from 'react';
import useSWR from 'swr';
import SessionApi from 'api/session';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { Session } from '@insight/types';

import Console, { ConsoleEventDTO } from '../Console';

type Props = {
  session: Session;
};

const SessionDetails = ({ session }: Props) => {
  const { data = [] } = useSWR(
    `sessions/${session.id}/events/search`,
    () => SessionApi.events.get(session.id),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  const consoleEvents = useMemo(() => {
    return data
      .filter((event) => {
        const eventType = event.e;
        return eventType === 9 || eventType === 10;
      })
      .map((event) => event) as ConsoleEventDTO[];
  }, [data]);

  return (
    <Block display="flex" height="100%">
      <Block
        display="flex"
        flexDirection="column"
        overflow="hidden"
        width="100%"
      >
        <H3>Session {session.id}</H3>
        <div>Device ID: {session.deviceId}</div>
        <div>IP Address: {session.ipAddress}</div>
      </Block>
      <Console style={{ width: '600px' }} events={consoleEvents} />
    </Block>
  );
};

export default SessionDetails;
