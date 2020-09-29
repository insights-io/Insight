import { mapAuthToken } from '@insight/sdk';
import { AuthTokenDTO } from '@insight/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import useSWRQuery from 'shared/hooks/useSWRQuery';

const CACHE_KEY = 'AuthApi.sso.token.list';

export const useAuthTokens = (initialData: AuthTokenDTO[]) => {
  const { mutate, data = initialData } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.sso.token.list(),
    { initialData }
  );

  const authTokens = useMemo(() => data.map(mapAuthToken), [data]);

  const addAuthToken = useCallback(
    (authToken: AuthTokenDTO) => {
      mutate([...data, authToken]);
    },
    [mutate, data]
  );

  const removeAuthToken = useCallback(
    (token: string) => {
      mutate(data.filter((authToken) => authToken.token !== token));
    },
    [mutate, data]
  );

  return { authTokens, addAuthToken, removeAuthToken };
};
