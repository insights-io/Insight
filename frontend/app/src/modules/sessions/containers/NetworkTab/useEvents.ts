import { SessionApi } from 'api';
import { useQuery } from 'shared/hooks/useQuery';

export const cacheKey = (sessionId: string) => {
  return ['NetworkTab', 'sessions', sessionId, 'events', 'search'];
};

const queryFn = (sessionId: string) => {
  return SessionApi.events.search(sessionId, {
    // TODO: pagination
    search: { 'event.e': ['eq:11'], limit: 1000 },
  });
};

export const useEvents = (sessionId: string) => {
  const { data } = useQuery(cacheKey(sessionId), () => queryFn(sessionId), {
    refetchInterval: 5000,
  });

  return { data };
};
