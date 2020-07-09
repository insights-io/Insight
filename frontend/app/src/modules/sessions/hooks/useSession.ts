import { Session } from '@insight/types';
import useSWR from 'swr';
import { SessionApi } from 'api';
import { mapSession } from '@insight/sdk';

const useSession = (sessionId: string, initialData: Session) => {
  const { data } = useSWR(
    'SessionApi.getSession',
    () =>
      SessionApi.getSession(sessionId).then((session) => mapSession(session)),
    { initialData }
  );

  return { session: data || initialData };
};

export default useSession;
