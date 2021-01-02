import { mapSession } from '@rebrowse/sdk';
import { useMemo } from 'react';
import type { SessionDTO } from '@rebrowse/types';
import { useQuery } from 'shared/hooks/useQuery';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

export const cacheKey = (id: string) => {
  return ['v1', 'sessions', id];
};
export const queryFn = (sessionId: string) => {
  return client.recording.sessions
    .retrieve(sessionId, INCLUDE_CREDENTIALS)
    .then((httpResponse) => httpResponse.data);
};

export const useSession = (sessionId: string, initialData: SessionDTO) => {
  const { data = initialData } = useQuery(
    cacheKey(initialData.id),
    () => queryFn(sessionId),
    { initialData: () => initialData }
  );

  const session = useMemo(() => mapSession(data), [data]);

  return { session };
};
