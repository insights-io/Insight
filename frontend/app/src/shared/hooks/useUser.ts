import { useCallback, useMemo } from 'react';
import { AuthApi } from 'api/auth';
import { mapUser } from '@insight/sdk';
import { UpdateUserPayload } from '@insight/sdk/dist/auth';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { UserDTO } from '@insight/types';

const CACHE_KEY = 'AuthApi.user.me';

export const useUser = (initialData: UserDTO) => {
  const { data = initialData, mutate } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.user.me(),
    { initialData, refreshInterval: 30000 }
  );

  const setUser = useCallback(
    (updatedUser: UserDTO) => {
      mutate(updatedUser);
    },
    [mutate]
  );

  const updateUser = useCallback(
    (payload: UpdateUserPayload) => {
      return AuthApi.user.update(payload).then((updatedUser) => {
        setUser(updatedUser);
        return updatedUser;
      });
    },
    [setUser]
  );

  const user = useMemo(() => mapUser(data), [data]);

  return { user, updateUser, setUser };
};
