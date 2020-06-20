import useSWR from 'swr';
import SessionApi, { Session } from 'api/session';

const CACHE_KEY = 'SessionApi.getSessions';
const EMPTY_LIST: Session[] = [];

const useSessions = () => {
  const { data = EMPTY_LIST } = useSWR(CACHE_KEY, SessionApi.getSessions);
  return { data };
};

export default useSessions;
