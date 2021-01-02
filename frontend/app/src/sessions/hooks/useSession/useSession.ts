import { mapSession } from '@rebrowse/sdk';
import { useMemo } from 'react';
import type { SessionDTO } from '@rebrowse/types';
import { useQuery } from 'shared/hooks/useQuery';
import { client } from 'sdk';

export const cacheKey = (id: string) => {
  return ['v1', 'sessions', id];
};

export const useSession = (sessionId: string, initialData: SessionDTO) => {
  const { data = initialData } = useQuery(
    cacheKey(initialData.id),
    () =>
      client.recording.sessions
        .retrieve(sessionId)
        .then((httpResponse) => httpResponse.data),
    { initialData: () => initialData }
  );

  const session = useMemo(() => mapSession(data), [data]);

  return { session };
};
