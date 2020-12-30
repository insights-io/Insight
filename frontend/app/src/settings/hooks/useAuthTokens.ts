import { mapAuthToken } from '@rebrowse/sdk';
import type { AuthTokenDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

export const cacheKey = ['sso', 'token', 'list'];
const queryFn = () => AuthApi.sso.token.list();

export const useAuthTokens = (initialData: AuthTokenDTO[]) => {
  const { data = initialData } = useQuery(cacheKey, queryFn, { initialData });
  const authTokens = useMemo(() => data.map(mapAuthToken), [data]);
  return { authTokens };
};

export const useAuthTokenMutations = () => {
  const queryClient = useQueryClient();

  const { mutateAsync: create } = useMutation(
    () => AuthApi.sso.token.create(),
    {
      onSuccess: (httpResponse) => {
        queryClient.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
          return [...(prev || []), httpResponse.data];
        });
      },
    }
  );

  const { mutateAsync: revoke } = useMutation(
    (token: string) => AuthApi.sso.token.delete(token),
    {
      onSuccess: (_result, token) => {
        queryClient.setQueryData<AuthTokenDTO[]>(cacheKey, (prev) => {
          return (prev || [])?.filter((t) => t.token !== token);
        });
      },
    }
  );

  return { create, revoke };
};
