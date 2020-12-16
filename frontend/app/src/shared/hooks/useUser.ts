import { useMemo } from 'react';
import { AuthApi } from 'api/auth';
import { mapUser } from '@rebrowse/sdk';
import type { UpdateUserPayload } from '@rebrowse/sdk/dist/auth';
import type { PhoneNumber, UserDTO } from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

const CACHE_KEY = ['AuthApi', 'user', 'me'];

export const useUser = (initialData: UserDTO) => {
  const queryClient = useQueryClient();
  const { data = initialData } = useQuery(CACHE_KEY, () => AuthApi.user.me(), {
    initialData: () => initialData,
  });

  const { mutateAsync: updateUser } = useMutation(
    (payload: UpdateUserPayload) => AuthApi.user.update(payload),
    {
      useErrorBoundary: true,
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const { mutateAsync: updatePhoneNumber } = useMutation(
    (phoneNumber: PhoneNumber | undefined | null) =>
      AuthApi.user.updatePhoneNumber(phoneNumber),
    {
      useErrorBoundary: true,
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const { mutateAsync: verifyPhoneNumber } = useMutation(
    (code: number) => AuthApi.user.phoneNumberVerify(code),
    {
      useErrorBoundary: true,
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const user = useMemo(() => mapUser(data), [data]);

  return { user, updateUser, updatePhoneNumber, verifyPhoneNumber };
};
