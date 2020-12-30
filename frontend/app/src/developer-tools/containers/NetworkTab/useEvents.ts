import { TermCondition } from '@rebrowse/sdk';
import { SessionApi } from 'api';
import { useQuery } from 'shared/hooks/useQuery';

export const cacheKey = (sessionId: string) => {
  return [
    'SessionApi',
    'sessions',
    sessionId,
    'events',
    'search',
    'event.e',
    TermCondition.EQ(11),
  ];
};

const queryFn = (sessionId: string) => {
  return SessionApi.events
    .search(sessionId, {
      // TODO: pagination
      search: {
        'event.e': [TermCondition.EQ(11)],
        limit: 1000,
      },
    })
    .then((httpResponse) => httpResponse.data);
};

export const useEvents = (sessionId: string) => {
  const { data } = useQuery(cacheKey(sessionId), () => queryFn(sessionId), {
    refetchInterval: 3000,
  });

  return { data };
};
