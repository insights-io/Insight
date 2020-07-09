import React, { useMemo } from 'react';
import useSWR from 'swr';
import SessionApi from 'api/session';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { BrowserLogEventDTO, Session } from '@insight/types';

import Console from '../Console';

type Props = {
  session: Session;
};

const SessionDetails = ({ session }: Props) => {
  const { data = [] } = useSWR(
    `sessions/${session.id}/events/search`,
    () => SessionApi.events.get(session.id),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  const logEvents = useMemo(() => {
    return data
      .filter((event) => event.e === 9)
      .map((event) => event) as BrowserLogEventDTO[];
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
      <Console style={{ width: '600px' }} events={logEvents} />
    </Block>
  );
};

export default SessionDetails;
