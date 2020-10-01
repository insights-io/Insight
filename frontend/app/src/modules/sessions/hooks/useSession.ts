import { SessionApi } from 'api';
import { mapSession } from '@insight/sdk';
import { useEffect, useMemo } from 'react';
import type { SessionDTO } from '@insight/types';
import useSWRQuery from 'shared/hooks/useSWRQuery';

export const useSession = (sessionId: string, initialData: SessionDTO) => {
  const { data = initialData, mutate } = useSWRQuery(
    'SessionApi.getSession',
    () => SessionApi.getSession(sessionId),
    { initialData }
  );

  useEffect(() => {
    mutate(initialData);
  }, [initialData, mutate]);

  const session = useMemo(() => mapSession(data), [data]);

  return { session };
};
