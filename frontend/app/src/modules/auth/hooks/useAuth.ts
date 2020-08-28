/* eslint-disable @typescript-eslint/no-non-null-assertion */
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { User } from '@insight/types';
import { mapUser } from '@insight/sdk';
import { UpdateUserPayload } from '@insight/sdk/dist/auth';
import { useCallback } from 'react';

const CACHE_KEY = 'useAuth';

const useAuth = (initialData: User) => {
  const { data: user = initialData, mutate } = useSWR(
    CACHE_KEY,
    () => AuthApi.sso.me().then(mapUser),
    { initialData, refreshInterval: 30000 }
  );

  const updateUserCache = useCallback(
    (next: User) => {
      mutate(next);
    },
    [mutate]
  );

  const updateUser = useCallback(
    (payload: UpdateUserPayload) => {
      return AuthApi.user
        .update(payload)
        .then(mapUser)
        .then((updatedUser) => {
          updateUserCache(updatedUser);
          return updatedUser;
        });
    },
    [updateUserCache]
  );

  return { user, updateUser, updateUserCache };
};

export default useAuth;
