import React, { useMemo } from 'react';
import useSWR from 'swr';
import SessionApi from 'api/session';
import { H3 } from 'baseui/typography';
import { Block } from 'baseui/block';

import Console, { LogEvent } from '../Console';

type Props = {
  sessionId: string;
};

const SessionDetails = ({ sessionId }: Props) => {
  const { data = [] } = useSWR(`sessions/${sessionId}/events/search`, () =>
    SessionApi.getEvents(sessionId)
  );

  const logEvents = useMemo(() => {
    return data
      .filter((event) => event.event.e === '9')
      .map((event) => event.event) as LogEvent[];
  }, [data]);

  return (
    <Block display="flex" height="100%">
      <Block
        display="flex"
        flexDirection="column"
        overflow="hidden"
        width="100%"
      >
        <H3>Session {sessionId}</H3>
        <div>TODO some contennt</div>
      </Block>
      <Console style={{ width: '600px' }} events={logEvents} />
    </Block>
  );
};

export default SessionDetails;
