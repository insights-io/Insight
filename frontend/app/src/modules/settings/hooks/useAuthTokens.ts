import { mapAuthToken } from '@rebrowse/sdk';
import { AuthTokenDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { useMemo } from 'react';
import { useMutation, useQuery, useQueryCache } from 'shared/hooks/useQuery';

export const cacheKey = ['sso', 'token', 'list'];

export const useAuthTokens = (initialData: AuthTokenDTO[]) => {
  const { data = initialData } = useQuery(
    cacheKey,
    () => AuthApi.sso.token.list(),
    { initialData }
  );

  const authTokens = useMemo(() => data.map(mapAuthToken), [data]);

  return { authTokens };
};

export const useAuthTokenMutations = () => {
  const queryCache = useQueryCache();

  const [create] = useMutation(() => AuthApi.sso.token.create(), {
    throwOnError: true,
    onSuccess: (authToken) => {
      queryCache.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
        return [...(prev || []), authToken];
      });
    },
  });

  const [revoke] = useMutation(
    (token: string) => AuthApi.sso.token.delete(token),
    {
      throwOnError: true,
      onSuccess: (_result, token) => {
        queryCache.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
          return (prev || [])?.filter((t) => t.token !== token);
        });
      },
    }
  );

  return { create, revoke };
};
