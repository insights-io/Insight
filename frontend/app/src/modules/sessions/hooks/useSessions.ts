import SessionApi from 'api/session';
import { useMemo, useState, useCallback, useEffect } from 'react';
import { Session } from '@insight/types';
import { mapSession } from '@insight/sdk';
import { cache } from 'swr';

const EMPTY_LIST: Session[] = [];
const CACHE_KEY = 'useSessions';

export const clearCache = () => {
  return cache.delete(CACHE_KEY);
};

const useSessions = (initialData?: Session[]) => {
  const [data, setData] = useState(
    () => (cache.get(CACHE_KEY) as Session[]) || initialData
  );
  const sessions = useMemo(() => data || EMPTY_LIST, [data]);
  const [fetchingFrom, setFetchingFrom] = useState<number | undefined>(
    undefined
  );

  useEffect(() => {
    cache.set(CACHE_KEY, sessions);
  }, [sessions]);

  const loadMoreItems = useCallback(
    async (startIndex: number, endIndex: number) => {
      if (sessions.length !== startIndex || startIndex === fetchingFrom) {
        return;
      }

      const lastSession = sessions[sessions.length - 1];
      const limit = endIndex - startIndex + 1;
      setFetchingFrom(startIndex);
      const newSessions = await SessionApi.getSessions({
        search: {
          sort_by: ['-created_at'],
          limit,
          created_at: [`lte:${lastSession.createdAt.toISOString()}`],
        },
      }).then((response) => response.map(mapSession));
      setData((prev) => (prev ? [...prev, ...newSessions] : newSessions));
      setFetchingFrom(undefined);
    },
    [setData, sessions, fetchingFrom]
  );

  const isItemLoaded = useCallback(
    (index: number) => {
      return index < sessions.length;
    },
    [sessions]
  );

  const loading = useMemo(() => data === undefined, [data]);

  return { data: sessions, loading, loadMoreItems, isItemLoaded };
};

export default useSessions;
