import { mapAuthToken } from '@rebrowse/sdk';
import type { AuthTokenDTO } from '@rebrowse/types';
import { useMemo } from 'react';
import { client } from 'sdk';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

export const cacheKey = ['sso', 'token', 'list'];
const queryFn = () =>
  client.auth.tokens.list().then((httpResponse) => httpResponse.data);

export const useAuthTokens = (initialData: AuthTokenDTO[]) => {
  const { data = initialData } = useQuery(cacheKey, queryFn, { initialData });
  const authTokens = useMemo(() => data.map(mapAuthToken), [data]);
  return { authTokens };
};

export const useAuthTokenMutations = () => {
  const queryClient = useQueryClient();

  const { mutateAsync: create } = useMutation(
    () => client.auth.tokens.create(),
    {
      onSuccess: (httpResponse) => {
        queryClient.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
          return [...(prev || []), httpResponse.data];
        });
      },
    }
  );

  const { mutateAsync: revoke } = useMutation(
    (token: string) => client.auth.tokens.delete(token),
    {
      onSuccess: (_result, token) => {
        queryClient.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
          return (prev || []).filter((t) => t.token !== token);
        });
      },
    }
  );

  return { create, revoke };
};
