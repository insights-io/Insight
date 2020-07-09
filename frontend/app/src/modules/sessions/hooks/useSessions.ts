import useSWR from 'swr';
import SessionApi from 'api/session';
import { useMemo } from 'react';
import { Session } from '@insight/types';
import { mapSession } from '@insight/sdk';

const EMPTY_LIST: Session[] = [];

const useSessions = (initialData?: Session[]) => {
  const { data } = useSWR(
    'SessionApi.getSessions',
    () => SessionApi.getSessions().then((sessions) => sessions.map(mapSession)),
    { initialData }
  );

  const loading = useMemo(() => data === undefined, [data]);

  return { data: data || EMPTY_LIST, loading };
};

export default useSessions;
