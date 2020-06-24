import React from 'react';
import useSWR from 'swr';
import SessionApi from 'api/session';
import { H5, H3 } from 'baseui/typography';

type Props = {
  sessionId: string;
};

const SessionDetails = ({ sessionId }: Props) => {
  const { data = [] } = useSWR(`sessions/${sessionId}/events/search`, () =>
    SessionApi.getEvents(sessionId)
  );

  return (
    <>
      <H3>Session {sessionId}</H3>
      {data.map((event) => {
        return <div id={JSON.stringify(event)}>{JSON.stringify(event)}</div>;
      })}
    </>
  );
};

export default SessionDetails;
