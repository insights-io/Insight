import { useCallback, useMemo } from 'react';
import { AuthApi } from 'api/auth';
import { mapUser } from '@rebrowse/sdk';
import { UpdateUserPayload } from '@rebrowse/sdk/dist/auth';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { PhoneNumber, UserDTO } from '@rebrowse/types';

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

  const updatePhoneNumber = useCallback(
    (phoneNumber: PhoneNumber | null) => {
      return AuthApi.user.updatePhoneNumber(phoneNumber).then((updatedUser) => {
        setUser(updatedUser);
        return updatedUser;
      });
    },
    [setUser]
  );

  const user = useMemo(() => mapUser(data), [data]);

  return { user, updateUser, updatePhoneNumber, setUser };
};
