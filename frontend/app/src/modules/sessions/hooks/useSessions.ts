import useSWR from 'swr';
import SessionApi from 'api/session';
import { useMemo } from 'react';
import { Session } from '@insight/types';
import { mapSession } from '@insight/sdk';

const CACHE_KEY = 'SessionApi.getSessions';
const EMPTY_LIST: Session[] = [];

const useSessions = (initialData?: Session[]) => {
  const { data } = useSWR(
    CACHE_KEY,
    () =>
      SessionApi.getSessions().then((sessions) =>
        sessions.data.map(mapSession)
      ),
    {
      initialData,
    }
  );

  const loading = useMemo(() => data === undefined, [data]);

  return { data: data || EMPTY_LIST, loading };
};

export default useSessions;
