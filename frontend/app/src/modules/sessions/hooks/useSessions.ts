import useSWR from 'swr';
import SessionApi from 'api/session';
import { useMemo } from 'react';
import { Session } from '@insight/types';

const CACHE_KEY = 'SessionApi.getSessions';
const EMPTY_LIST: Session[] = [];

const useSessions = () => {
  const { data } = useSWR(CACHE_KEY, () => SessionApi.getSessions());
  const loading = useMemo(() => data === undefined, [data]);

  return { data: data || EMPTY_LIST, loading };
};

export default useSessions;
