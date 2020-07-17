import React from 'react';
import useSWR from 'swr';
import { SessionApi } from 'api';

type Props = {
  sessionId: string;
};

const NetworkTab = ({ sessionId }: Props) => {
  const { data: _data } = useSWR(
    `NetworkTab.sessions/${sessionId}/events/search`,
    () =>
      SessionApi.events.search(sessionId, {
        searchParams: { 'event.e': ['eq:11'], limit: 1000 },
      }),
    { refreshWhenHidden: true, refreshInterval: 5000 }
  );

  return <div>TODO</div>;
};

export default React.memo(NetworkTab);
