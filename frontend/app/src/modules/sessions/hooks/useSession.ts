import { Session } from '@insight/types';
import useSWR from 'swr';
import { SessionApi } from 'api';
import { mapSession } from '@insight/sdk';
import { useEffect } from 'react';

const useSession = (sessionId: string, initialData: Session) => {
  const { data, mutate } = useSWR(
    'SessionApi.getSession',
    () =>
      SessionApi.getSession(sessionId).then((session) => mapSession(session)),
    { initialData }
  );

  useEffect(() => {
    mutate(initialData);
  }, [initialData]);

  return { session: data || initialData };
};

export default useSession;
